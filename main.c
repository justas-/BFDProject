#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <poll.h>
#include <signal.h>

#include "helpers.h"
#include "main.h"

#define BUFSZ 256

localSocket *localSocketDB = NULL;
size_t localSocketDBSize = 0;
remoteSocket *remoteSocketDB = NULL;
size_t remoteSocketDBSize = 0;

int localCommunicationSocket = 0;

static void sighupHandler(int signo)
{
    printf("SIGHUP handled");
}

static void sigintHandler(int signo)
{
    printf("SIGINT handled");
    unlink("net.pid");
    exit(0);
}

void setupSignalHandlers()
{
    if (signal(SIGHUP, sighupHandler) == SIG_ERR) {
        printf("An error occurred while setting a SIGHUP signal handler.\n");
    }

    if (signal(SIGINT, sigintHandler) == SIG_ERR) {
        printf("An error occurred while setting a SIGHUP signal handler.\n");
    }
}

int isListeningSocket(localSocket **localDB, size_t localDBLen, int thisSocket)
{
    for(size_t i=0;i<localDBLen;i++){
        if((*localDB)[i].listenSocket == thisSocket) return 1;
    }
    return 0;
}

int isRemoteSocket(remoteSocket **remoteDB, size_t remoteDBLen, int thisSocket)
{
    for(size_t i=0;i<remoteDBLen;i++){
        if((*remoteDB)[i].talkingSocket == thisSocket) return 1;
    }
    return 0;
}

void *get_in_addr(struct sockaddr *sa)
{
    if(sa->sa_family == AF_INET){
        return &(((struct sockaddr_in*)sa)->sin_addr);
    } else {
        return &(((struct sockaddr_in6*)sa)->sin6_addr);
    }
}

int getConnectedSocket(const char *ipaddr, int port)
{
    struct addrinfo hints, *ai, *p;
    int rv, sock, yes=1;
    char ipstr[INET6_ADDRSTRLEN], service[6];

    memset(&hints, 0, sizeof hints);
    hints.ai_family = AF_INET;
	hints.ai_socktype = SOCK_STREAM;

	if(ipaddr == NULL){
        hints.ai_flags = AI_PASSIVE;
	}

	sprintf(service, "%d", port);

    if ((rv = getaddrinfo(ipaddr, service, &hints, &ai)) != 0) {
		printf("getaddrinfo() error: %s\n", gai_strerror(rv));
		return -1;
	}

    for(p=ai; p != NULL; p=p->ai_next) {
		sock = socket(p->ai_family, p->ai_socktype, p->ai_protocol);
		if (sock < 0){
			continue;
		}

		setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(int));

		if(connect(sock, p->ai_addr, p->ai_addrlen) == -1) {
			close(sock);
			perror("Client: connect");
			continue;
		}

        struct sockaddr_in *ipv4 = (struct sockaddr_in *)p->ai_addr;
        void *addr = &(ipv4->sin_addr);

        inet_ntop(p->ai_family, addr, ipstr, sizeof ipstr);
        printf("getConnectedSocket() connected to: %s:%d\n", ipstr, port);
		break;
	}

	// Socket opening failed
	if (p==NULL){
		printf("getConnectedSock() error: failed to connect\n");
		return -1;
	}

	freeaddrinfo(ai);

	return sock;
}

int ForwardToBFDFSM(int socket)
{
    char buf[256];
    memset(buf, 0, 256);
    int nbytesRx = 0;

    if((nbytesRx = recv(socket, buf, sizeof buf, 0)) <= 0){
        if(nbytesRx == 0){
            printf("Remote socket hungup POLLIN\n");
            return -1;
        } else {
            perror("recv");
        }
    }
    else {
        printf("Got %d bytes from Remote client\n", nbytesRx);
        if(localCommunicationSocket != 0)
            sendall(localCommunicationSocket, buf, nbytesRx);
        else
            printf("Received data, but local BFDFSM is not connected.\n");
    }
    return 0;
}

int HandleRemoteData(int socket)
{
    char buf[256];
    memset(buf, 0, 256);
    int nbytesRx = 0;

    if((nbytesRx = recv(socket, buf, sizeof buf, 0)) <= 0){
        if(nbytesRx == 0){
            printf("Remote socket hungup POLLIN\n");
            return -1;
        } else {
            perror("recv");
        }
    }
    else {
        printf("Got %d bytes from Remote connection\n", nbytesRx);

        // TODO:
        //  Take note of their disc
        //  Forward data to local client
    }
    return 0;
}

void handlePollEvents(int rv, struct pollfd **fdArr, size_t *numFd)
{
    printf("Events available in %d socket(s)\n", rv);

    size_t newNumFd = *numFd;
    int newfd=0, numRemovals = 0;
    socklen_t addrlen;

    char buf[BUFSZ];
	memset(buf, 0, BUFSZ);

    for(size_t curSock=0;curSock<*numFd;curSock++){
        if(!(*fdArr)[curSock].revents) {
            continue; // No event in this socket
        }
        else if((*fdArr)[curSock].revents & POLLIN) {
            printf("POLLIN event in socket %zu\n", curSock);

            if(curSock==0){
                // This is local listening socket.
                // Accept a connection and take note of socket
                printf("New POLLIN even in local listening socket\n");
                newfd = accept((*fdArr)[0].fd, NULL, &addrlen);
                if(newfd == -1){
                    perror("accept");
                } else {
                    addFd(fdArr, newNumFd);
                    (*fdArr)[newNumFd].fd = newfd;
                    (*fdArr)[newNumFd].events = POLLIN;
                    newNumFd++;
                    localCommunicationSocket = newfd;
                }
            }
            else {
                // Figure out what socket that has an event
                if((*fdArr)[curSock].fd == localCommunicationSocket){
                    // This is data from BFD State Machine
                    // TODO:
                    //  Receive
                    //  Get Discs
                    //  Check if local known -> send
                    //  Check if remote known -> send
                    //  Else report problem
                }
                else if(isListeningSocket(&localSocketDB, localSocketDBSize, (*fdArr)[curSock].fd)){
                    // This is Connection request from local listeners
                    printf("New POLLIN even in remote listening socket\n");
                    newfd = accept((*fdArr)[curSock].fd, NULL, &addrlen);
                    if(newfd == -1){
                        perror("accept");
                    } else {
                        addFd(fdArr, newNumFd);
                        (*fdArr)[newNumFd].fd = newfd;
                        (*fdArr)[newNumFd].events = POLLIN;
                        newNumFd++;
                    }
                }
                else if(isRemoteSocket(&remoteSocketDB, remoteSocketDBSize, (*fdArr)[curSock].fd)){
                    // This is data from outgoing connection that we made
                    // Receive data and pass to BFD client
                    if(ForwardToBFDFSM((*fdArr)[curSock].fd) == -1){
                        // Socket hungup when we tried to RX data
                        (*fdArr)[curSock].fd = -1;
                        numRemovals++;
                    }
                }
                else {
                    // This is data from connection made to us
                    if(HandleRemoteData((*fdArr)[curSock].fd) == -1){
                        // Remote Socket hungup when we tried to RX data
                        (*fdArr)[curSock].fd = -1;
                        numRemovals++;
                    }
                }
            }
        }
        else if ((*fdArr)[curSock].revents & POLLHUP) {
            printf("POLLHUP event in socket %zu\n", curSock);
            (*fdArr)[curSock].fd = -1; // Mark for cleanup
            numRemovals++;
        }
        else if ((*fdArr)[curSock].revents & POLLERR) {
            printf("POLLERR event in socket %zu\n", curSock);
        }
        else if ((*fdArr)[curSock].revents & POLLNVAL) {
            printf("POLLNVAL event in socket %zu\n", curSock);
            (*fdArr)[curSock].fd = -1; // Mark for cleanup
            numRemovals++;
        }
    }

    if(numRemovals > 0){
        cleanUpFds(fdArr, &newNumFd);
    }
    *numFd = newNumFd;
    printf("New number of fds: %zu\n", *numFd);
}

int getListenSocket(const char *ipaddr, int port)
{
    struct addrinfo hints, *ai, *p;
    int rv, sock, yes=1;
    char ipstr[INET6_ADDRSTRLEN], service[6];

    memset(&hints, 0, sizeof hints);
    hints.ai_family = AF_INET;
	hints.ai_socktype = SOCK_STREAM;

	if(ipaddr == NULL){
        hints.ai_flags = AI_PASSIVE;
	}

	sprintf(service, "%d", port);

    if ((rv = getaddrinfo(ipaddr, service, &hints, &ai)) != 0) {
		printf("getaddrinfo() error: %s\n", gai_strerror(rv));
		return -1;
	}

    for(p=ai; p != NULL; p=p->ai_next) {
		sock = socket(p->ai_family, p->ai_socktype, p->ai_protocol);
		if (sock < 0){
			continue;
		}

		setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(int));

		if(bind(sock, p->ai_addr, p->ai_addrlen) < 0) {
			close(sock);
			continue;
		}

        struct sockaddr_in *ipv4 = (struct sockaddr_in *)p->ai_addr;
        void *addr = &(ipv4->sin_addr);

        inet_ntop(p->ai_family, addr, ipstr, sizeof ipstr);
        printf("getSocket() binded on: %s:%d\n", ipstr, port);
		break;
	}

	// Socket opening failed
	if (p==NULL){
		printf("getSock() error: failed to bind\n");
		return -1;
	}

	freeaddrinfo(ai);

	// Start listening
	if(listen(sock, 10) == -1) {
		perror("listen");
		return -1;
	}

	return sock;
}

int setupLocalSocket(struct pollfd **fdArr, size_t *numFd)
{
    const char *listenOn = "0.0.0.0";

    int localListener = getListenSocket(listenOn, 50000);
    if(localListener == -1){
        printf("Failed to start local listener socket!\n");
        return 1;
    }

    if(addFd(fdArr, *numFd) != 0){
        return 1;
    }
    else{
        (*fdArr)[0].fd = localListener;
        (*fdArr)[0].events = POLLIN;
        (*numFd)++;
    }

	return 0;
}

int setupListeningSockets(struct pollfd **fdArr, size_t *numFd)
{
    int sockNum = 0;
    for(size_t i=0;i<localSocketDBSize;++i){
        sockNum = getListenSocket(
            localSocketDB[i].ipAddr,
            localSocketDB[i].port);

        if(sockNum == -1){
            printf("Unable to start listening.\n");
            return 1;
        }

        localSocketDB[i].listenSocket = sockNum;

        if(addFd(fdArr, *numFd) != 0){
            return 1;
        }
        else{
            (*fdArr)[*numFd].fd = sockNum;
            (*fdArr)[*numFd].events = POLLIN;
            (*numFd)++;
        }
    }
    return 0;
}

int main()
{
    parseConfigFile(
        &localSocketDB,
        &localSocketDBSize,
        &remoteSocketDB,
        &remoteSocketDBSize);

    setupSignalHandlers();

    savePID();

    struct pollfd *fdArr = NULL; // Pointer to array of pollfd structs
    size_t numFd = 0; // Number of elements in fdArr array

    if(setupLocalSocket(&fdArr, &numFd) != 0) {
        printf("!12\n");
        return 1;
    }

    if(setupListeningSockets(&fdArr, &numFd) != 0) {
        printf("!23\n");
        return 1;
    }

	int timeout_msecs = 5000;

	while(1) {
        printf("Waiting for events in %zu sockets\n", numFd);
		int rv = poll(fdArr, numFd, timeout_msecs);

		// Handle errors or timeouts
		if(rv == -1){
			printf("poll() error. Exiting.\n");
			return 1;
		}
		else if(rv == 0){
			printf("poll() timeout\n");
		}
		else {
            handlePollEvents(rv, &fdArr, &numFd);
		}
	}

	return 0;
}
