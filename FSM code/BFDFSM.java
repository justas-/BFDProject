package bfd.fsm;

import java.io.IOException;
import java.net.Socket;
import java.util.BitSet;
import java.util.Scanner;



/**
 *
 * @author angelos
 */
public class BFDFSM {
    
    //Use this a staring poimt to check the implementation
    public static void main(String[] args) throws IOException {
        
        //Connect with the BFD handler
        System.out.println("    Establishing connection with BFD handler");
        int remotePort=50000;
        System.out.println("    Connecting to server on port " + remotePort);
        
        Socket client ;
        try{
        client=new Socket("localhost", remotePort);
        }catch(Exception ex){
            System.out.println("    BFD handler offline");
            System.out.println("    Clossing...");
            return;
        }
        System.out.println("    Just connected to "
                  + client.getRemoteSocketAddress());
        //Listen for BFD packets
        MessageListener listener;
        listener=new MessageListener("BFD Message Listener");
        listener.StartListener(client);
        
        //Connect with the Admin console
        System.out.println("    Establishing connection with Admin console");
        int remotePort2=50002;
        System.out.println("    Connecting to server on port " + remotePort2);
        Socket client2;
        try{
        client2= new Socket("localhost", remotePort2);
        }catch(Exception ex){
            System.out.println("    Admin console offline");
            System.out.println("    Clossing...");
            return;
        }
        System.out.println("    Just connected to "
                  + client2.getRemoteSocketAddress());
        //Listen for Admin commands
        CommandListener cml=new CommandListener("Command Listener");
        cml.StartListener(client2,client);
        
  /*      //Hardcoded session
        BFDSession session=new BFDSession();
        session.setLocalDiscr(123);
        session.setDemandMode(0);
        session.setIsActive(true);
        session.setRequiredMinEchoRxInterval(500);
        session.setRequiredMinRxInterval(500);
        session.setDesiredMinTxInterval(500);
        session.setSessionState(1);
        SessionHandler.addSession(session);*/
              
        //Dummy message
        BFDmessage msg2=new BFDmessage();
        msg2.setVersion(1);
        msg2.setLength(99);
        msg2.setDetectMult(5);
        msg2.setMultipoint(0);
        msg2.setMyDiscriminator(124);
        msg2.setAuthenticationPresent(0);
        msg2.setPoll(1);
        msg2.setRequiredMinEchoRxInterval(1);
                     
        //Check message and command sending
        //System.out.println("    Message sent is "+msg2.getBFDMessage());
        //MessageSender sender=new MessageSender();
        //sender.Send(msg2,client);
        
        System.out.println("    Sending command to the admin console ");
        CommandSender cms=new CommandSender();
        BitSet cmd=cms.CommandCreator(msg2.getMyDiscriminator(), 0);
        System.out.println(" Command send is "+cmd);
        cms.Send(cmd, client2);       
       //client.close();
    }
}