/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bfd.fsm;


import java.util.BitSet;
import java.util.List;

/**
 * This class can be used to Validate a BFD message after it has arrived to 
 * the client
 *
 * @author angelos
 */
public class ValidityCheck {
    
   public boolean ValidityCheck( BFDmessage msg,SessionHandler sh){
        if (convert(msg.getVersion(),3)!=1){             
            return false;
        }
        if (msg.getAuthenticationPresent()){
            if (convert(msg.getLength(),8)<26){                
                return false;
            }
        }
        else{
             if (convert(msg.getLength(),8)<24){                
                return false;
            }
        }
        //Add step here ENCAPSULATION PROTOCOL
        if (convert(msg.getDetectMult(),8)==0){           
            return false;
        }
        if (msg.getMultipoint()){            
            return false;
        }
        if (convert(msg.getMyDiscriminator(),32)==0){          
           return false;
        }
        if (convert(msg.getYourDiscriminator(),32)!=0){
           List<BFDSession> sessions = SessionHandler.getSessions();
           int flag=0;
            for (BFDSession session : sessions) {
                if (session.getLocalDiscr() == (convert(msg.getYourDiscriminator(),32))) {
                    //Session found
                    flag=1;
                }
            }
           if (flag==0){
               return false;
           }
          
        }
        else{
            if ((convert(msg.getState(),2)!=0)&&(convert(msg.getState(),2)!=1)){               
                return false;
            }
            else {
           List<BFDSession> sessions = SessionHandler.getSessions();
           int flag=0;
            for (BFDSession session : sessions) {
                if (session.getRemoteDiscr() == (convert(msg.getMyDiscriminator(),32))) {
                    //Session found
                    flag=1;
                }
            }
            //Create new Session and add it
            //Update static values
           if (flag==0){
               System.out.println(" New remote BFD Session found");
               System.out.println(" New local  BFD session created to match remote session");
               BFDSession nSession=SessionHandler.CreateNewSession(200);
               nSession.setRemoteDiscr(convert(msg.getMyDiscriminator(),32));                               
               SessionHandler.addSession(nSession);
           }
            }
        }
        if (msg.getAuthenticationPresent()){           
            return false;
        }       
        return true;
   }
    
    public static int convert(BitSet bits,int size) {
        int value = 0;
        for (int i =size-1; i >=0 ; i--) {
         if (bits.get(i)){ 
             value=value+ (int)Math.pow(2, size-i-1);  
         }
        }
        return value;
}
}
