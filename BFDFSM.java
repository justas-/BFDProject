package bfd.fsm;

import java.io.IOException;


/**
 *
 * @author angelos
 */
public class BFDFSM {
    
    //Use this a staring poimt to check the implementation
    public static void main(String[] args) throws IOException {
        
        //Session example
        BFDSession session=new BFDSession();
        SessionHandler sh=new SessionHandler();
        sh.addSession(session);       
        //       
        //Time-out example
       /* TimeOut tm=new TimeOut();
        Timer t=tm.BeginTimer(5000);       
        tm.CancelTimer(t);
        tm.BeginTimer(7000);*/
        
        //Message example       
      /* BFDmessage msg=new BFDmessage();
        msg.setVersion(1);
        msg.setDiag(1);
        msg.setState(1);
        msg.setPoll(1);
        msg.setFinal(1);
        msg.setControlPlaneIndependent(1);
        msg.setAuthenticationPresent(1);
        msg.setDemand(1);
        msg.setMultipoint(1);
        msg.setDetectMult(1);
        msg.setLength(1);
        msg.setMyDiscriminator(1);
        msg.setYourDiscriminator(1);
        msg.setDesiredMinTxInterval(1);
        msg.setRequiredMinRxInterval(1);
        msg.setRequiredMinEchoRxInterval(1);
        */
        /*
        //FSM example
        Fsm fsm=new Fsm();      
        fsm.RecievedDown();
        fsm.RecievedInit();
        */
        
        //Validity Check example
        BFDmessage msg2=new BFDmessage();
        msg2.setVersion(1);
        msg2.setLength(99);
        msg2.setDetectMult(1);
        msg2.setMultipoint(0);
        msg2.setMyDiscriminator(567);
        msg2.setYourDiscriminator(789);
        msg2.setAuthenticationPresent(0);
        //msg2.setRequiredMinEchoRxInterval(156);
              
        //Check Listener creation
        MessageListener listener;
        listener=new MessageListener("thread1",6631);
        listener.StartListener();
        
        //Check message sending
        System.out.println("    Message sent is "+msg2.getBFDMessage());
        MessageSender sender=new MessageSender();
        sender.Send(msg2,6631);
    }   
}