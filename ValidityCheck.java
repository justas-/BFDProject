/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bfd.fsm;

import java.util.BitSet;

/**
 * This class can be used to Validate a BFD message after it has arrived to 
 * the client
 *
 * @author angelos
 */
public class ValidityCheck {
    
   public boolean ValidityCheck( BFDmessage msg,SessionHandler sh){
        if (convert(msg.getVersion())!=1){             
            return false;
        }
        if (msg.getAuthenticationPresent()){
            if (convert(msg.getLength())<26){                
                return false;
            }
        }
        else{
             if (convert(msg.getLength())<24){                
                return false;
            }
        }
        //Add step here ENCAPSULATION PROTOCOL
        if (convert(msg.getDetectMult())==0){           
            return false;
        }
        if (msg.getMultipoint()){            
            return false;
        }
        if (convert(msg.getMyDiscriminator())==0){          
           return false;
        }
        if (convert(msg.getYourDiscriminator())!=0){
            //Select session here
        }
        else{
            if ((convert(msg.getState())!=0)&&(convert(msg.getState())!=1)){               
                return false;
            }
        }
        if (msg.getAuthenticationPresent()){           
            return false;
        }       
        return true;
   }
    
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
