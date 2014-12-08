/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bfd.fsm;

import java.util.ArrayList;
import java.util.List;

/**
 *This class creates a list of BFD sessions which can be individually accessed 
 * or created through the appropriate functions
 * 
 * @author angelos
 */
public class SessionHandler {
    static List<BFDSession> sessions = new ArrayList();
    
    //Add a new sessions to the database
    static void addSession(BFDSession session){
        sessions.add(session);
    }
    //Get a session from the database
    static BFDSession getSession(int i){
       return sessions.get(i);
    }
    static void updateSession(int i,BFDSession session){
        sessions.set(i, session);
    }
    //Get the complete session database
    static List getSessions(){
        return sessions;
    }
    
    static BFDSession CreateNewSession(int discr){
        BFDSession nSession=new BFDSession();
        nSession.setSessionState(1);
        nSession.setRemoteSessionState(1);
        nSession.setLocalDiscr(discr);
        nSession.setRemoteDiscr(0);
        nSession.setDesiredMinTxInterval(1000);
        nSession.setRequiredMinRxInterval(1000);
        nSession.setRemoteMinRxInterval(1);
        nSession.setDemandMode(0);
        nSession.setRemoteDemandMode(0);
        nSession.setDetectMult(5);
        nSession.setAuthType(0);      
        return nSession;
    }
}
