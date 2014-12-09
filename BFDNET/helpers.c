#include <poll.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <unistd.h>

#include "helpers.h"

void printFdArr(struct pollfd **fdArr, size_t numFd)
{
    for(size_t i=0; i<numFd; ++i)
        printf("fdArr[%zu] => %d\n",
            i,
            (*fdArr)[i].fd);
}

void removeFd(struct pollfd **fds, size_t numFd, size_t removeThisFd)
{
    size_t newSz = (numFd-1) * sizeof(struct pollfd);

    if((numFd-1) == removeThisFd){
        printf("Performing edge cleanup\n");
        if((*fds = realloc(*fds, newSz)) != NULL){
            return;
        }
        else{
            printf("Unable to allocate memory in removeFd()\n");
        }
    }
    else {
        printf("Performing non edge cleanup\n");
        struct pollfd *fdnew = malloc(newSz);
        if(fdnew != NULL){
            memcpy(
                fdnew,
                *fds,
                removeThisFd * sizeof(struct pollfd));
            memcpy(
                &fdnew[removeThisFd],
                &(*fds)[removeThisFd+1],
                (numFd-removeThisFd-1) * sizeof(struct pollfd));
            *fds = fdnew;
        }
        else{
            printf("Unable to allocate memory in removeFd()\n");
        }
    }
}

int addFd(struct pollfd **fdArr, size_t currentSz)
{
    *fdArr = realloc(*fdArr, (currentSz+1) * sizeof(struct pollfd));
    if(fdArr != NULL){
        return 0;
    }
    else{
        printf("addFd() failed!\n");
        return 1;
    }
}

void cleanUpFds(struct pollfd **fdArr, size_t *currentSz)
{
    printf("Descriptors cleanup started. Curent Sz:%zu\n", *currentSz);

    size_t workingSize = *currentSz;

    for(size_t i=0;i<workingSize;i++){
        if((*fdArr)[i].fd == -1){
            printf("Cleaning up %zu element\n",i);
            removeFd(fdArr, workingSize, i);
            workingSize--;
            i--; // We need to recheck current element
        }
    }

    *currentSz = workingSize;
}

ssize_t sendall(int socket, char *data, size_t dataLen)
{
    const char *cp = data;
    size_t sentSz=0;

    while (dataLen) {
      sentSz = send(socket, data, dataLen, 0);
      if (sentSz <= 0) {
        perror("send");
        return -1;
      }
      dataLen -= sentSz;
      cp += sentSz;
    }

    return sentSz;
}

int processLocalLine(char **line, size_t lineLen, localSocket **localDB, size_t *localDBLen)
{
    localSocket ls;
    int runNum = 0;
    char *token = NULL;
    char delim[2] = ",";

    token = strtok(*line, delim);

    while(token != NULL){
        if(runNum == 1){
            strcpy(ls.ipAddr, token);
        }
        else if(runNum == 2){
            ls.port = atoi(token);
        }
        runNum++;
        token = strtok(NULL, delim);
    }

    if((*localDB = realloc(*localDB, (*localDBLen+1)* sizeof(struct listenAddr))) != NULL){
        strcpy(
            (*localDB)[*localDBLen].ipAddr,
            ls.ipAddr);
        (*localDB)[*localDBLen].port = ls.port;
        *localDBLen = *localDBLen+1;
    }
    else {
        printf("realloc() failed in processLocalLine()\n");
    }

    return 0;
}

int processRemoteLine(char **line, size_t lineLen, remoteSocket **remoteDB, size_t *remoteDBLen)
{
    remoteSocket rs;
    int runNum = 0;
    char *token = NULL;
    char delim[2] = ",";

    token = strtok(*line, delim);

    while(token != NULL){
        if(runNum == 1){
            char buf[12];
            strcpy(buf, token);
            rs.ourDisc = atol(buf);
        }
        else if(runNum == 2){
            strcpy(rs.ipAddr, token);
        }
        else if(runNum == 3){
            rs.port = atoi(token);
        }
        runNum++;
        token = strtok(NULL, delim);
    }

    if((*remoteDB = realloc(*remoteDB, (*remoteDBLen+1) * sizeof(struct connectAddr))) != NULL){
        strcpy(
            (*remoteDB)[*remoteDBLen].ipAddr,
            rs.ipAddr);
        (*remoteDB)[*remoteDBLen].ourDisc = rs.ourDisc;
        (*remoteDB)[*remoteDBLen].port = rs.port;
        *remoteDBLen = *remoteDBLen+1;
    }
    else {
        printf("realloc() failed in processLocalLine()\n");
    }

    return 0;
}

void printLocalDB(localSocket **localDB, size_t localDBLen)
{
    printf("Local DB size: %zu\n", localDBLen);
    for(size_t i=0; i<localDBLen;i++){
        printf(
            "%zu: IP: %s PORT: %d\n",
            i,
            (*localDB)[i].ipAddr,
            (*localDB)[i].port);
    }
}

void printRemoteDB(remoteSocket **remoteDB, size_t remoteDBLen)
{
    printf("Remote DB size: %zu\n", remoteDBLen);
    for(size_t i=0; i<remoteDBLen;i++){
        printf(
            "%zu: IP: %s PORT: %d OURDISC: %lu THEIRDISC: %lu\n",
            i,
            (*remoteDB)[i].ipAddr,
            (*remoteDB)[i].port,
            (*remoteDB)[i].ourDisc,
            (*remoteDB)[i].theirDisc);
    }
}

int parseConfigFile(localSocket **localDB, size_t *localLen, remoteSocket **remoteDB, size_t *remoteLen)
{
    printf("Parsing config file:\n");

    FILE *fp = fopen("input.dat", "r");
    if(fp == NULL){
        perror("fopen");
        exit(1);
    }

    char *lineData = NULL;
    size_t len = 0;
    ssize_t lineLen;

    while((lineLen = getline(&lineData, &len, fp)) != -1){
        if(lineData[0] == 'L')
            processLocalLine(&lineData, lineLen, localDB, localLen);
        else if(lineData[0] == 'R')
            processRemoteLine(&lineData, lineLen, remoteDB, remoteLen);
        else
            printf("Unrecognized line!\n");
    }

    free(lineData);
    fclose(fp);

    printLocalDB(localDB, *localLen);
    printRemoteDB(remoteDB, *remoteLen);

    return 0;
}

void savePID()
{
    pid_t myPid = getpid();
    FILE *fp = fopen("net.pid", "w");

    if(fp == NULL){
        perror("fopen");
        exit(1);
    }

    fprintf(fp, "%d", myPid);
    fclose(fp);
}

void getDiscFromPkg(char *data, unsigned long *myDisc, unsigned long *theirDisc)
{
    long md = data[4] | (data[5] << 8) | (data[6] << 16) | (data[7] << 24);
    *myDisc = ntohl(md);
    long td = data[8] | (data[9] << 8) | (data[10] << 16) | (data[11] << 24);
    *theirDisc = ntohl(td);
}

int addRemoteEntry(remoteSocket **remDB, size_t *remDbLen)
{
    if((*remDB = realloc(*remDB, (*remDbLen+1) * sizeof(struct connectAddr))) != NULL){
        *remDbLen = *remDbLen+1;
        return 0;
    }
    else {
        printf("realloc() failed in addRemoteEntry()\n");
        return 1;
    }
}
