/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bfd.fsm;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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
    private final ServerSocket serverSocket;
    SessionHandler sh=new SessionHandler();
   
   MessageListener(String name,int port) throws IOException{
       threadName = name;
       serverSocket = new ServerSocket(port);
       serverSocket.setSoTimeout(0);
       System.out.println("Creating " +  threadName );
   }
   public void StartListener(){
   
      System.out.println("Starting a message listener" );
      if (t == null)
      {
         t = new Thread (this, threadName);
         t.start ();
      }
   }
   

    @Override
    public void run() {
     System.out.println("Running " +  threadName );
     while(true){
      try {
            System.out.println("Connecting to " + serverName);
            Socket server;
            server = serverSocket.accept();
            System.out.println("Just connected to "
                      + server.getRemoteSocketAddress());
            DataInputStream in =new DataInputStream(server.getInputStream());
            //When sending and recieving messages send or recieve the length first as an integer
            int length=in.readInt();
                        byte[] data = new byte[length];
            in.readFully(data); 
            BitSet bits = new BitSet();
            for (int i = 0; i < data.length * 8; i++) {
                if ((data[data.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
                    bits.set(i);
                }
            }
            System.out.println("    Message recieved is "+bits);
            BFDmessage msg=new BFDmessage();  
            msg.setBFDmessage(bits);           
            BFDMessageRecieved(msg);
            server.close();
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
        for (int i=0; i<sh.getSessions().size();i++){
            if (convert(msg.getYourDiscriminator())==sh.getSession(i).getRemoteDiscr()){
                if (convert(msg.getMyDiscriminator())==sh.getSession(i).getLocalDiscr()){
                    //Session found
                    BFDSession tmpSession;
                    tmpSession=sh.getSession(i);
                    
                    //Set remote Discriminator value
                    tmpSession.setRemoteDiscr(convert(msg.getMyDiscriminator())); 
                    //Set remote state value
                    tmpSession.setRemoteSessionState(convert(msg.getState()));
                    //Set remote demand mode
                    if (msg.getDemand())  tmpSession.setRemoteDemandMode(1);
                    else tmpSession.setRemoteDemandMode(0);
                    //Set remote min rx interval
                    tmpSession.setRemoteMinRxInterval(convert(msg.getRequiredMinRxInterval()));
                    //Cease ECHO if required min echo rx interval is zeto
                    if (convert(msg.getRequiredMinEchoRxInterval())==0){
                        //Here stop echo transmission
                    }
                    //Terminate Poll if Poll is on in the local system  and Final has been recieved
                    if(msg.getFinal()){
                        //Check if Poll sequence is enabled and terminate
                    }
                    // UPDATE the timers
                    
                    
                    //The FSM implementaion see page 35 of RFC
                    if (tmpSession.getSessionState()==0){
                        //Discard the packet
                    }
                    if (convert(msg.getState())==0){
                        if (tmpSession.getSessionState()!=1){
                            tmpSession.setLocalDiag(3);
                            tmpSession.setSessionState(1);
                        }                   
                    }
                    else {
                        if (tmpSession.getSessionState()==1){
                            if (convert(msg.getState())==1){
                                tmpSession.setSessionState(2);
                            }
                            else if(convert(msg.getState())==2){
                                tmpSession.setSessionState(3);
                            }                           
                        }
                        if (tmpSession.getSessionState()==2){
                            if ((convert(msg.getState())==2)||(convert(msg.getState())==3)){
                                tmpSession.setSessionState(3);
                            }
                        }
                        if (tmpSession.getSessionState()==3){
                            if (convert(msg.getState())==1){
                                tmpSession.setLocalDiag(3);
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
                }
            }
        }
       } 
         //IF the packet has not been discarded then UPDATE timers
    }
    
    //Convert from Bitset to int
    public static int convert(BitSet bits) {
        int value = 0;
        int j=0;
        for (int i = bits.length()-1; i >=0 ; --i) {
         if (bits.get(i)){            
             value=value+ (int)Math.pow(2, j);                        
         }
         j++;
        }
        return value;
}
}