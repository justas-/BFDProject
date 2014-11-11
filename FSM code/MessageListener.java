/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bfd.fsm;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.BitSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is to generate a message listener that listens for incoming BFD or ECHO packets
 * When a BFD message is recieved the sessions to which it belongs is updated with the MessageRecived function
 * @author angelos
 */

public class MessageListener implements Runnable
{
    Thread t;
    String threadName;
    String serverName = "localhost";
    Socket client;
    SessionHandler sh=new SessionHandler();
   
   MessageListener(String name) throws IOException{
       threadName = name;     
       System.out.println("     Creating " +  threadName );
   }
   public void StartListener(Socket tmpclient){
      System.out.println("      Starting a message listener" );
      if (t == null)
      {
         client=tmpclient;
         t = new Thread (this, threadName);
         t.start ();
      }
   }
   
    @Override
    public void run() {
     System.out.println("       Running " +  threadName );
     while(true){
      try {               
            DataInputStream in =new DataInputStream(client.getInputStream());
            byte[] data =new byte [24];
            int[] test=new int[192];
            while(true){
                in.read(data);          
                BitSet bits = new BitSet();
                for (int i = 0; i < (data.length) * 8; i++) {
                    if ((data[(data.length) - i / 8 - 1] & (1 << (i % 8))) > 0) {
                        bits.set(i);
                        test[i]=1;
                    }
                }
                System.out.println("    Data recieved is "+Arrays.toString(test));
                System.out.println("    Message recieved is "+bits);
                BFDmessage msg=new BFDmessage();  
                msg.setBFDmessage(bits);           
                BFDMessageRecieved(msg);
            }
     }  catch (IOException ex) {
            Logger.getLogger(MessageListener.class.getName()).log(Level.SEVERE, null, ex);
        }
     
     }
    }
    
    public void BFDMessageRecieved(BFDmessage msg){
 
        //1.Check if the message is a valid BFD message
        //2.Check if a session exists based on the discriminator values
        //3.Update this session
        ValidityCheck check=new ValidityCheck();
        //If the message is valid continue with finding and 
        //updating the session values
       if(check.ValidityCheck(msg,sh)){
            System.out.println("     Recieved message was valid");
            System.out.println("     There are "+SessionHandler.getSessions().size()+" active sessions");
            System.out.println("     Searching for session with local discr "+convert(msg.getYourDiscriminator(),32)+" and remote discr "+convert(msg.getMyDiscriminator(),32));
            for (int i=0; i<SessionHandler.getSessions().size();i++){
                if ((convert(msg.getYourDiscriminator(),32)==SessionHandler.getSession(i).getLocalDiscr())||(convert(msg.getMyDiscriminator(),32)==SessionHandler.getSession(i).getRemoteDiscr())){                
                    //Session found
                    System.out.println("    Corresponding Session found and ready to be updated");
                    BFDSession tmpSession;
                    tmpSession=SessionHandler.getSession(i);
                    
                    //Set remote Discriminator value
                    tmpSession.setRemoteDiscr(convert(msg.getMyDiscriminator(),32));
                    //Set remote state value
                    tmpSession.setRemoteSessionState(convert(msg.getState(),2));
                    //Set remote demand mode
                    if (msg.getDemand())  tmpSession.setRemoteDemandMode(1);
                    else tmpSession.setRemoteDemandMode(0);
                    //Set remote min rx interval
                    tmpSession.setRemoteMinRxInterval(convert(msg.getRequiredMinRxInterval(),32));
                    //Cease ECHO if required min echo rx interval is zeto
                    if (convert(msg.getRequiredMinEchoRxInterval(),32)==0){
                        //Here stop echo transmission
                    }
                    //Terminate Poll if Poll is on in the local system  and Final has been recieved
                    if(msg.getFinal()){
                        //Check if Poll sequence is enabled and terminate
                    }
                    // UPDATE the timers
                    //Update detection time
                    if (tmpSession.DemandMode==0){
                        tmpSession.setDetectionTime(convert(msg.getDetectMult(),8)*Math.max(tmpSession.getRequiredMinRxInterval(),convert(msg.getDesiredMinTxInterval(),32)));
                        System.out.println("    Detection time set to "+tmpSession.getDetectionTime());
                    }
                    else{
                       tmpSession.setDetectionTime(convert(msg.getDetectMult(),8)*Math.max(tmpSession.getDesiredMinTxInterval(),tmpSession.getRemoteMinRxInterval()));
                    }
                    
                    //The FSM implementaion see page 35 of RFC
                    if (tmpSession.getSessionState()==0){
                        //Discard the packet
                    }
                    if (convert(msg.getState(),2)==0){
                        if (tmpSession.getSessionState()!=1){
                            System.out.println("Session is down");
                            tmpSession.setLocalDiag(3);
                            tmpSession.setSessionState(1);
                        }                   
                    }
                    else {
                        if (tmpSession.getSessionState()==1){
                            if (convert(msg.getState(),2)==1){
                                System.out.println("Session is init");
                                tmpSession.setSessionState(2);
                            }
                            else if(convert(msg.getState(),2)==2){
                                System.out.println("Session is UP");
                                tmpSession.setSessionState(3);
                            }                           
                        }
                        if (tmpSession.getSessionState()==2){
                            if ((convert(msg.getState(),2)==2)||(convert(msg.getState(),2)==3)){
                                System.out.println("Session is UP");
                                tmpSession.setSessionState(3);
                            }
                        }
                        if (tmpSession.getSessionState()==3){
                            if (convert(msg.getState(),2)==1){
                                tmpSession.setLocalDiag(3);
                                System.out.println("Session is down");
                                tmpSession.setSessionState(1);
                            }
                        }
                    }
                    
                    //ADD
                    //Check to see if Demand mode should become active or not
                    
                    //Cease BFD messages if remote demand is 1 local session state is UP 
                    //and remote session state is UP
                    if(tmpSession.getRemoteDemandMode()==1){
                        if(tmpSession.getSessionState()==3){
                            if(tmpSession.getRemoteSessionState()==3){
                                //
                                //CEASE PERIODIC BFD MESSAGES
                                //
                            }
                        }
                    }
                    //Send BFD messages if remote demand mode is 0 or local session sate is not UP or remote session state is not UP
                    if((tmpSession.getRemoteDemandMode()==0)||(tmpSession.getRemoteSessionState()!=3)||(tmpSession.getSessionState()!=3)){
                        //
                        //Local system must send periodic BFD messages
                        //
                    }
                    
                    if (msg.getPoll()){
                        //
                        //Send  a BFD controll packet with P=0 and F=1
                        //           
                }
                    //Update session entry
                    SessionHandler.sessions.set(i, tmpSession);
                    //IF the packet has not been discarded then UPDATE timers
                    SessionHandler.getSession(i).getRecieveTimer().CancelTimer();
                    RecieveTimeOut timer=new RecieveTimeOut();
                    timer.BeginTimer(SessionHandler.getSession(i).getDetectionTime(),SessionHandler.getSession(i).getLocalDiscr(),client);
                    SessionHandler.getSession(i).setRecieveTimer(timer);
            }
        }
       } 
       else System.out.println("   Malformed packet recieved");

    }
    
    //Convert from Bitset to int
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