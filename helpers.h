typedef struct listenAddr {
    char ipAddr[40];
    unsigned int port;
    int listenSocket;
} localSocket;

typedef struct connectAddr {
    char ipAddr[40];
    unsigned int port;
    unsigned long ourDisc;
    unsigned long theirDisc;
    int talkingSocket;
} remoteSocket;

int addFd(struct pollfd **fdArr, size_t currentSz);
void removeFd(struct pollfd **fds, size_t numFd, size_t removeThisFd);
void cleanUpFds(struct pollfd **fdArr, size_t *currentSz);
ssize_t sendall(int socket, char *data, size_t dataLen);
int processLocalLine(char **line, size_t lineLen, localSocket **localDB, size_t *localDBLen);
int processRemoteLine(char **line, size_t lineLen, remoteSocket **remoteDB, size_t *remoteDBLen);
void printLocalDB(localSocket **localDB, size_t localDBLen);
void printRemoteDB(remoteSocket **remoteDB, size_t remoteDBLen);
int parseConfigFile(localSocket **localDB, size_t *localLen, remoteSocket **remoteDB, size_t *remoteLen);
void savePID();
void getDiscFromPkg(char *data, unsigned long *myDisc, unsigned long *theirDisc);
void printFdArr(struct pollfd **fdArr, size_t numFd);
