package bfd.fsm;

/**
 * This class keeps track of the current state of the BFD session by 
 * implementing the FSM described in the BFD rfc document 
 *
 * @author angelos
 */
public class Fsm {
    int state=1;
    
    void Fsm(){
        state=1;
    }
    void RecievedUp(){
        if (state==1) state=1;
        if (state==2) state=3;
        if (state==3) state=3;
    }
    void RecievedDown(){
        if (state==1) state=2;
        if (state==2) state=2;
        if (state==3) state=1;   
    }
    
    void RecievedInit(){
        if (state==1) state=3;
        if (state==2) state=3;
        if (state==3) state=3;        
    }
    
    void RecievedTimer(){
        if (state==1) state=1;
        if (state==2) state=1;
        if (state==3) state=1;        
    }
    
    void RecievedAdminDown(){
        if (state==1) state=0;
        if (state==2) state=0;
        if (state==3) state=0;        
    }
    
    int getCurrentState(){
        return state;
    }
}
