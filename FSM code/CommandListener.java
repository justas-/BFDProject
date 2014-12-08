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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author angelos
 */
public class CommandListener implements Runnable{

    Thread t;
    String threadName;
    String serverName = "localhost";
    Socket client,messageClient;
    int[] cm1={0,0,0,0,0,0,0,1};
    int[] cm2={0,0,0,0,0,0,1,0};
    int[] cm3={0,0,0,0,0,1,0,0};
    int[] cm4={0,0,0,0,0,1,0,1};
    int[] cm5={0,0,0,0,0,1,1,1};
    CommandListener(String name) throws IOException{
       threadName = name;     
       System.out.println("     Creating " +  threadName );
   }
    
   public void StartListener(Socket tmpclient,Socket tmpMsgClient){
      System.out.println("      Starting a message listener" );
      if (t == null)
      {
         client=tmpclient;
         messageClient=tmpMsgClient;
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
            byte[] data =new byte [5];
            int[] bits=new int[40];
            while(true){
                in.read(data);
                        for (int i = 0; i < (data.length) * 8; i++) {
                             if ((data[(data.length) - i / 8 - 1] & (1 << (i % 8))) > 0) {
                                bits[i]=1;
                             }
                        }
                        System.out.println("    Command recieved from the Admin Console");
                        int[] discr=Arrays.copyOfRange(bits,8,40);
                        //Check if it is a new Session command
                        if (Arrays.toString(Arrays.copyOfRange(bits,0,8)).equals(Arrays.toString(cm1))){
                            System.out.println("    New Session command recieved");
                            System.out.println("    Creating new Session");
                            BFDSession nSession=SessionHandler.CreateNewSession(convert(discr));
                            MessageSender sender=new MessageSender();                          
                            sender.Send(sender.MessageCreator(nSession), messageClient);
                            SendTimeOut tm=new SendTimeOut();
                            tm.BeginTimer(500, convert(discr),messageClient);                            
                            nSession.setSendTimer(tm);
                            SessionHandler.addSession(nSession);
                        }
                        else{
                        //Identify the session to which the command refers to
                            int session;
                            System.out.println("    Searching for Session with Local Discr "+convert(discr));
                            for (int i=0; i<SessionHandler.getSessions().size();i++){
                                if (SessionHandler.getSession(i).getLocalDiscr()==convert(discr)){
                                System.out.println("  Correpsonding Session Found");
                                session=i;
                                }                                                      
                            }
                        }
                        //Identify the command based on the identifier signal
                        if(Arrays.toString(Arrays.copyOfRange(bits,0,8)).equals(Arrays.toString(cm2))){
                            System.out.println("    Admin Down command recieved");
                        }
                        else if(Arrays.toString(Arrays.copyOfRange(bits,0,8)).equals(Arrays.toString(cm3))){
                            System.out.println("    ECHO command recieved");
                        }
                        else if(Arrays.toString(Arrays.copyOfRange(bits,0,8)).equals(Arrays.toString(cm4))){
                            System.out.println("    Function command recieved");
                        }
                        else if(Arrays.toString(Arrays.copyOfRange(bits,0,8)).equals(Arrays.toString(cm5))){
                            System.out.println("    Role command recieved");
                        }
                
                        
            }
     }  catch (IOException ex) {
            Logger.getLogger(MessageListener.class.getName()).log(Level.SEVERE, null, ex);
        }
     
     }
    }
        public static int convert(int[] bits) {
        int value = 0;
        int j=0;
        for (int i = 31; i >=0 ; --i) {
         if (bits[i]==1){            
             value=value+ (int)Math.pow(2, j);                        
         }
         j++;
        }
        return value;
}
    
}
