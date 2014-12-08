package bfd.fsm;

/**
 * Keeps track of a specific BFD session
 *
 * @author angelos
 */
public class BFDSession {
   
    int RequiredMinEchoRxInterval,SessionState,RemoteSessionState,LocalDiscr,RemoteDiscr,LocalDiag,DesiredMinTxInterval,RequiredMinRxInterval,RemoteMinRxInterval,DemandMode,RemoteDemandMode,DetectMult,AuthType;
    int sid,RemoteDetectMult,DetectionTime;
    boolean isActive=false;
    SendTimeOut sendTimer=new SendTimeOut();
    RecieveTimeOut recieveTimer=new RecieveTimeOut();
    
    void setSessionId(int id){
        sid=id;
    }
    
    void setIsActive(boolean active){
        isActive=active;
    }
    
    boolean getIsActive(){
        return isActive;
    }
    int getSessionId(){
        return sid;
    }
    void setSessionState(int state){
    SessionState=state;
    }

    int getSessionState(){
    return SessionState;
    }

    void setRemoteSessionState(int state){
    RemoteSessionState=state;
    }
    
    int getRemoteSessionState(){
    return RemoteSessionState;
    }   

    void setRemoteDiscr(int discr){
    RemoteDiscr=discr;
    }
    
    int getRemoteDiscr(){
    return RemoteDiscr;
    }
    void setLocalDiscr(int discr){
    LocalDiscr=discr;    
    }
    
    int getLocalDiscr(){
    return LocalDiscr;
    }
    
    void setLocalDiag(int diag){
        LocalDiag=diag;
    }
    
    int getLocalDiag(){
        return LocalDiag;
    }
    
    void setDesiredMinTxInterval(int interval){
        DesiredMinTxInterval=interval;
    }
    
    int getDesiredMinTxInterval(){
        return DesiredMinTxInterval;
    }
    
    void setRequiredMinRxInterval(int interval){
        RequiredMinRxInterval=interval;
    }
    
    int getRequiredMinRxInterval(){
        return RequiredMinRxInterval;
    }
    void setRequiredMinEchoRxInterval(int interval){
        RequiredMinEchoRxInterval=interval;
    }
    
    int getRequiredMinEchoRxInterval(){
        return RequiredMinEchoRxInterval;
    }
    void setRemoteMinRxInterval(int interval){
        RemoteMinRxInterval=interval;
    }
    
    int getRemoteMinRxInterval(){
        return RemoteMinRxInterval;
    }
    
    void setDemandMode(int mode){
        DemandMode=mode;
    }
    
    int getDemandMode(){
        return DemandMode;
    }
    
    void setRemoteDemandMode(int mode){
        RemoteDemandMode=mode;
    }
    
    int getRemoteDemandMode(){
        return RemoteDemandMode;
    }
    
    void setDetectMult(int mult){
        DetectMult=mult;
    }
    
    int getDetectMult(){
        return DetectMult;
    }
    
    void setRemoteDetectMult(int mult){
        RemoteDetectMult=mult;
    }
    
    int getRemoteDetectMult(){
        return RemoteDetectMult;
    }
    
    void setAuthType(int type){
        AuthType=type;
    }
    
    int getAuthType(){
        return AuthType;
    }
    void setDetectionTime(int time){
        DetectionTime=time;
    }
    int getDetectionTime(){
        return DetectionTime;
    }
    RecieveTimeOut getRecieveTimer(){
        return recieveTimer;
    }
    SendTimeOut getSendTimer(){
        return sendTimer;
    }
    void setRecieveTimer(RecieveTimeOut timer){
        recieveTimer=timer;
    }
    void setSendTimer(SendTimeOut timer){
        sendTimer=timer;
    }    
}
