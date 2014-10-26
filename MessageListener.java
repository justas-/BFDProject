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
        //1.Check if a session exists based on the discriminator values
        //2.Update this session
        SessionHandler sh=new SessionHandler();
        for (int i=0; i<sh.getSessions().size();i++){
            if (convert(msg.getYourDiscriminator())==sh.getSession(i).getRemoteDiscr()){
                if (convert(msg.getMyDiscriminator())==sh.getSession(i).getLocalDiscr()){
                    //Session found
                    BFDSession tmpSession;
                    tmpSession=sh.getSession(i);
                    //tmpSession.setRemoteDiscr(); 
                    tmpSession.setRemoteSessionState(convert(msg.getState()));
                    if (msg.getDemand())  tmpSession.setRemoteDemandMode(1);
                    else tmpSession.setRemoteDemandMode(0);
                    tmpSession.setRemoteMinRxInterval(convert(msg.getRequiredMinRxInterval()));
                    if (convert(msg.getRequiredMinEchoRxInterval())==0){
                        //Here stop echo transmission
                    }
                    if(msg.getFinal()){
                        //Check if Poll sequence is enabled and terminate
                    }
                    // UPDATE the timers
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
                    //
                    
                    if(tmpSession.getRemoteDemandMode()==1){
                        if(tmpSession.getSessionState()==3){
                            if(tmpSession.getRemoteSessionState()==3){
                                //
                                //CEASE PERIODIC BFD MESSAGES
                                //
                            }
                        }
                    }
                    
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
                    
                    //
                    //IF the packet has not been discarded then UPDATE timers
                    //
                }
            }
        }
        
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