package bfd.fsm;

/**
 * Keeps track of a specific BFD session
 *
 * @author angelos
 */
public class BFDSession {
   
    int SessionState,RemoteSessionState,LocalDiscr,RemoteDiscr,LocalDiag,DesiredMinTxInterval,RequiredMinRxInterval,RemoteMinRxInterval,DemandMode,RemoteDemandMode,DetectMult,AuthType;
    int sid,RemoteDetectMult;
    boolean isActive=false;
    
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
    
    int getDetectionTime(){
        if (DemandMode==0){
            if( RequiredMinRxInterval>DesiredMinTxInterval){
              return (RemoteDetectMult*RequiredMinRxInterval);  
            }
            else{
              return (RemoteDetectMult*DesiredMinTxInterval);  
            }
        }
        else {
            if (DesiredMinTxInterval>RemoteMinRxInterval){
                return (DetectMult*DesiredMinTxInterval); 
            }
            else{
                return (DetectMult*RemoteMinRxInterval); 
            }
           
        }
    }
}
