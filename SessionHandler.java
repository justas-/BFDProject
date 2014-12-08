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
    List<BFDSession> sessions = new ArrayList();
    
    //Add a new sessions to the database
    public void addSession(BFDSession session){
        sessions.add(session);
    }
    //Get a session from the database
    public BFDSession getSession(int i){
       return sessions.get(i);
    }
    public void updateSession(int i,BFDSession session){
        sessions.set(i, session);
    }
    //Get the complete session database
    public List getSessions(){
        return sessions;
    }
}
