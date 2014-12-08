
package dummyserver;

/**
 *
 * @author angelos
 */

import java.net.*;
import java.io.*;
import java.util.Arrays;
import java.util.BitSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DummyServer {
         
   public static void main(String [] args)
   {    
       
       try {
           Socket server;
           ServerSocket serverSocket;
           serverSocket = new ServerSocket(50000);
           serverSocket.setSoTimeout(0);
           server = serverSocket.accept();
           System.out.println("Just connected to "
                   + server.getRemoteSocketAddress());
                     
            DataInputStream in =new DataInputStream(server.getInputStream());
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
                System.out.println("    Message Data recieved is "+Arrays.toString(test));
                System.out.println("    Message recieved is "+bits);
                
               //Send back the data
                 OutputStream outToClient = server.getOutputStream();    
                 DataOutputStream out =
                       new DataOutputStream(outToClient);
                out.write(data);
            }
        } catch (IOException ex) {
           Logger.getLogger(DummyServer.class.getName()).log(Level.SEVERE, null, ex);
       }
       
   }

    
}