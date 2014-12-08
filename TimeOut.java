
package bfd.fsm;

import java.util.Timer;
import java.util.TimerTask;
/**
 * This class can be used to generate or reset timers, to check if a message 
 * has expired or not
 *
 * @author angelos
 */
public class TimeOut {
    
    public Timer BeginTimer(int ms){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
             System.out.println("Timer Expired");
             }
        }, ms);
        return timer;
    }
     public void CancelTimer(Timer timer){
            timer.cancel();
     }    
}
