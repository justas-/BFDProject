
package consoleserver;

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

public class ConsoleServer {
         
   public static void main(String [] args)
   {    
       
       try {          
            Socket server;
            ServerSocket serverSocket;
            serverSocket = new ServerSocket(50002);
            serverSocket.setSoTimeout(0);
            server = serverSocket.accept();
            System.out.println("Just connected to "
                   + server.getRemoteSocketAddress());          
            DataInputStream in =new DataInputStream(server.getInputStream());
            byte[] data =new byte [5];          
            while(true){               
                in.readFully(data);
                BitSet bits = new BitSet();
                for (int i = 0; i < (data.length) * 8; i++) {
                    if ((data[(data.length) - i / 8 - 1] & (1 << (i % 8))) > 0) {
                        bits.set(i);
                    }
                }
                System.out.println("    Command recieved is "+bits);            
                OutputStream outToClient = server.getOutputStream();    
                DataOutputStream out =
                       new DataOutputStream(outToClient);
                out.write(data);           
            }
        } catch (IOException ex) {
           Logger.getLogger(ConsoleServer.class.getName()).log(Level.SEVERE, null, ex);
       }
       
   }

    
}