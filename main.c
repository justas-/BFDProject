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

struct pollfd *handlePollEvents(int rv, struct pollfd *fdArr, size_t *numFd)
{
    printf("Events available in %d socket(s)\n", rv);

    size_t newNumFd = *numFd;
    int newfd, nbytesRx, numRemovals = 0;
    socklen_t addrlen;

    char buf[BUFSZ];
	memset(buf, 0, BUFSZ);

    // TODO: eventsProcessed check
    for(size_t curSock=0;curSock<*numFd;curSock++){
        if(!fdArr[curSock].revents) {
            continue; // No event in this socket
        }
        else if(fdArr[curSock].revents & POLLIN) {
            printf("POLLIN event in socket %zu\n", curSock);

            if(curSock==0){
                // This is listening socket. Accept a connection
                printf("New POLLIN even in local listening socket\n");
                newfd = accept(fdArr[0].fd, NULL, &addrlen);
                if(newfd == -1){
                    perror("accept");
                } else {
                    fdArr[1].fd = newfd;
                    fdArr[1].events = POLLIN;
                }
            }
            else if(curSock == 1){
                // TODO Send to required socket
                if((nbytesRx = recv(fdArr[curSock].fd, buf, sizeof buf, 0)) <= 0){
                    if(nbytesRx == 0){
                        printf("Local DATA socket hungup in POLLIN\n");
                        fdArr[curSock].fd = -1; // Mark for cleanup
                        numRemovals++;
                    } else {
                        perror("recv");
                    }
                } else {
                    // Actual data from client
                    printf("Got %d bytes from local client.\n", nbytesRx);
                    sendall(
                        fdArr[3].fd,//fdArr[2].fd, //fdArr[curSock].fd,
                        buf,
                        nbytesRx
                    );
                    memset(buf, 0, 256);
                }
            }
            else {
                // Non 0th or 1st socket
                if(isListeningSocket(&localSocketDB, localSocketDBSize, fdArr[curSock].fd)){
                    printf("New POLLIN even in listening socket\n");
                    newfd = accept(fdArr[curSock].fd, NULL, &addrlen);
                    if(newfd == -1){
                        perror("accept");
                    }
                    else {
                        if(addFd(&fdArr, *numFd) == 0){
                            printf("Placed new sonnection in socket: %zu\n", newNumFd);
                            fdArr[newNumFd].fd = newfd;
                            fdArr[newNumFd].events = POLLIN;
                            newNumFd++;
                        }
                        else{
                            printf("Failed to increas size for new FD\n");
                        }
                    }
                }
                else {
                    if((nbytesRx = recv(fdArr[curSock].fd, buf, sizeof buf, 0)) <= 0){
                        if(nbytesRx == 0){
                            printf("Socket %zu hungup in POLLIN\n", curSock);
                            fdArr[curSock].fd = -1; // Mark for cleanup
                            numRemovals++;
                        }
                        else {
                            perror("recv");
                        }
                    }
                    else {
                        // Actual data from client
                        printf("Got %d from client on socket %zu\n", nbytesRx, curSock);
                        printf("Sending to client on 1 socket\n");
                        sendall(
                            fdArr[1].fd,//fdArr[2].fd, //fdArr[curSock].fd,
                            buf,
                            nbytesRx);
                        memset(buf, 0, 256);
                    }
                }
            }
        }
        else if (fdArr[curSock].revents & POLLHUP) {
            printf("POLLHUP event in socket %zu\n", curSock);
            fdArr[curSock].fd = -1; // Mark for cleanup
            numRemovals++;
        }
        else if (fdArr[curSock].revents & POLLERR) {
            printf("POLLERR event in socket %zu\n", curSock);
        }
        else if (fdArr[curSock].revents & POLLNVAL) {
            printf("POLLNVAL event in socket %zu\n", curSock);
            fdArr[curSock].fd = -1; // Mark for cleanup
            numRemovals++;
        }
    }

    if(numRemovals > 0){
        fdArr = cleanUpFds(fdArr, &newNumFd);
    }
    *numFd = newNumFd;
    printf("New number of fds: %zu\n", *numFd);

    return fdArr;
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

int setupLocalSockets(struct pollfd **fdArr, size_t *numFd)
{
    const char *listenOn = "0.0.0.0";

    int localListener = getListenSocket(listenOn, 50000);
    if(localListener == -1){
        printf("Failed to start local listener socket!\n");
        return 1;
    }

    *fdArr = realloc(*fdArr, (*numFd+1) * sizeof(struct pollfd));
    if(fdArr == NULL){
        printf("Failed to allocate memory for listening socket.\n");
        return 1;
    }

	(*fdArr)[0].fd = localListener;
	(*fdArr)[0].events = POLLIN;
	(*numFd)++;

	addFd(fdArr, *numFd);
	(*fdArr)[1].fd = 0;
	(*fdArr)[1].events = 0;
	(*numFd)++;

	return 0;
}

int setupListeningSockets(struct pollfd **fdArr, size_t *numFd)
{
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
    setupLocalSockets(&fdArr, &numFd);

//    int sockNum = 0;
//    for(size_t i=0;i<localSocketDBSize;i++){
//        sockNum = getListenSocket(
//            localSocketDB[i].ipAddr,
//            localSocketDB[i].port);
//
//        if(sockNum == -1){
//            printf("Unable to start listening. Continuing.\n");
//            continue;
//        }
//
//        localSocketDB[i].listenSocket = sockNum;
//
//        if(addFd(fdArr, numFd) != NULL){
//            fdArr[numFd].fd = sockNum;
//            fdArr[numFd].events = POLLIN;
//            numFd++;
//        }
//        else{
//            printf("Failed to increas size for new FD\n");
//        }
//    }

	int timeout_msecs = 5000;

	while(1) {
        printf("Waiting for events in %zu sockets\n", numFd);
		int rv = poll(fdArr, numFd, timeout_msecs);

		// Handle errors or timeouts
		if(rv == -1){
			printf("poll() error\n");
		} else if(rv == 0){
			printf("poll() timeout\n");
		} else {
            fdArr = handlePollEvents(rv, fdArr, &numFd);
		}
	}

	return 0;
}
