
package bfd.fsm;

import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
/**
 * This class can be used to generate or reset timers, to check if a message 
 * has expired or not
 *
 * @author angelos
 */
public class SendTimeOut {
    Timer timer = new Timer();
    public Timer BeginTimer(int ms,final int discr,Socket client){
        System.out.println("   Timer Started for "+ms+" ms");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
             System.out.println("Timer Expired");            
             for (int i=0;i<SessionHandler.getSessions().size();i++){
                 if (SessionHandler.getSession(i).getLocalDiscr()==discr){
                    System.out.println("   Sending new Message");
                    MessageSender sender=new MessageSender();
                    sender.Send(sender.MessageCreator(SessionHandler.getSession(i)),client);
                    SendTimeOut timer=new SendTimeOut();
                    SessionHandler.getSession(i).getSendTimer().CancelTimer();         
                    timer.BeginTimer(500,discr,client);
                    SessionHandler.getSession(i).setSendTimer(timer);
                 }
             }
             }
        }, ms);
        return timer;
    }
     public void CancelTimer(){
            System.out.println("   Timer cancelled");
            timer.cancel();
     }    
}
