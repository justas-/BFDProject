/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bfd.fsm;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 *This class can be used to create a Message sender
 *
 * @author angelos
 */
public class MessageSender {
    public void Send(BFDmessage msg,int port){
       //Converting from BitSet to byte array
    byte[] bytes=new byte[msg.getBFDMessage().length()/8+1];
    for (int i=0; i<msg.getBFDMessage().length(); i++) {
        if (msg.getBFDMessage().get(i)){
            bytes[bytes.length-i/8-1] |= 1<<(i%8);
        }
    }
    
    String serverName = "localhost";   
      try
      {
         System.out.println("Connecting to " + serverName
                             + " on port " + port);
         Socket client = new Socket(serverName, port);
         System.out.println("Just connected to "
                      + client.getRemoteSocketAddress());        
         OutputStream outToServer = client.getOutputStream();
         DataOutputStream out =
                       new DataOutputStream(outToServer);
         out.writeInt(bytes.length);
         out.write(bytes);       
         //client.close();
      }catch(IOException e)
      {
      }
   }
    public BFDmessage MessageCreator(BFDSession session){
        BFDmessage msg=new BFDmessage();
        msg.setVersion(1);
        msg.setDiag(session.getLocalDiag());
        msg.setState(session.getSessionState());
        //Insert Poll set here
        //Insert Final set here
        //Insert controll plane set here
        msg.setAuthenticationPresent(0);
        if ((session.getSessionState()==3)&&(session.getRemoteSessionState()==3)) {
            msg.setDemand(1);
        }
        else msg.setDemand(0);
        msg.setMultipoint(0);
        msg.setDetectMult(session.getDetectMult());
        msg.setLength(24);
        msg.setMyDiscriminator(session.getLocalDiscr());
        msg.setYourDiscriminator(session.getRemoteDiscr());
        msg.setDesiredMinTxInterval(session.getDesiredMinTxInterval());
        msg.setRequiredMinRxInterval(session.getRequiredMinRxInterval());
        msg.setRequiredMinEchoRxInterval(session.getRequiredMinEchoRxInterval());
        return msg;
    }
}
