#ifndef MAIN_H_INCLUDED
#define MAIN_H_INCLUDED

static void sighupHandler(int signo);
static void sigintHandler(int signo);
void setupSignalHandlers();
int isListeningSocket(localSocket **localDB, size_t localDBLen, int thisSocket);
void *get_in_addr(struct sockaddr *sa);
int getConnectedSocket(const char *ipaddr, int port);
void handlePollEvents(int rv, struct pollfd **fdArr, size_t *numFd);
int getListenSocket(const char *ipaddr, int port);
int setupLocalSocket(struct pollfd **fdArr, size_t *numFd);

#endif // MAIN_H_INCLUDED
