/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bfd.fsm;

/**
 *
 * @author angelos
 */
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.BitSet;

public class CommandSender {
    public void Send(BitSet msg,Socket client){
  //Converting from BitSet to byte array
    byte[] bytes=new byte[5];
    for (int i=0; i<msg.length(); i++) {
        if (msg.get(i)){
            bytes[bytes.length-i/8-1] |= 1<<(i%8);
        }
    }      
      try
      {     
         OutputStream outToServer = client.getOutputStream();
         DataOutputStream out = new DataOutputStream(outToServer);
         out.write(bytes); 
      }catch(IOException e)
      {
      }
   }
    public BitSet CommandCreator(BitSet discr, int cmd){
        BitSet tmp=new BitSet(8);
        BitSet result=new BitSet(40);
        if (cmd==0){
            System.out.println("    Creating session UP signal");
            tmp.set(7);
        }
        else if (cmd==1){
            System.out.println("    Creating session Init signal");
            tmp.set(6);
        }
        else if(cmd==2){
            System.out.println("    Creating session Down signal");
            tmp.set(5);            
        }
        else if(cmd==3){
            System.out.println("    Creating session Admin Down signal");
            tmp.set(7);
            tmp.set(5);
        }
        else if(cmd==4){
            System.out.println("    Creating ECHO off signal");
            tmp.set(7);
            tmp.set(6);
            tmp.set(5);
        }
        else {
            System.out.println("    Error unsuported command code");
        }
        for (int i=0;i<8;i++){
            if (tmp.get(i)){
                result.set(i);
            }
        }
        for (int i=8;i<40;i++){
            if (discr.get(i-8)){
                result.set(i);
            }
        }
        System.out.println(result);
        return result;
    }
}
