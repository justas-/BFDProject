package bfd.fsm;

import java.util.BitSet;

/**
 * This class can be used to generate or modify a BFD packet
 *
 * @author angelos
 */
public class BFDmessage {
    
    BitSet BFDmsg=new BitSet(192);
    
    public void BFDmessage(){
        BFDmsg.set(0, 191, false);
    }
    public void setBFDmessage(BitSet bitset){
        BFDmsg=bitset;
    }
    public BitSet getBFDMessage(){
        return BFDmsg;
    }
    
    void setVersion(int version){
       if (version==1){
           BFDmsg.set(2);
      }
       else{
           System.out.println("Unsupported Version");
           System.out.println(" Version was not set");
           return;
       }
         System.out.println("Version set");              
    }
    
    BitSet getVersion(){
        return BFDmsg.get(0,3);
    }
    
    void setDiag(int diag){
        if (diag==0){
            BFDmsg.set(3,8,false);
        }
        else if (diag==1){
            BFDmsg.set(7,true);
        }
        else if (diag==2){
            BFDmsg.set(6,true);
        }
        else if (diag==3){
            BFDmsg.set(6,true);
            BFDmsg.set(7,true);
        }
        else if (diag==4){
            BFDmsg.set(5,true);
        }
        else if (diag==5){
            BFDmsg.set(5,true);
            BFDmsg.set(7,true);
        }
        else if (diag==6){
            BFDmsg.set(5,true);
            BFDmsg.set(6,true);
        }
        else if (diag==7){         
            BFDmsg.set(5,true);
            BFDmsg.set(6,true);
            BFDmsg.set(7,true);
        }
        else if (diag==8){           
            BFDmsg.set(4,true);
        }
        else {
            System.out.println("Unsupported Diagnostic");
            System.out.println("Diagnostic was not set");
            return;
        }
        System.out.println("Diagnostics set");                    
    }
    
    BitSet getDiag(){
        return BFDmsg.get(3,8);
    }
    
   void setState(int sta){
       if (sta==0){
          BFDmsg.set(8,false);
          BFDmsg.set(9,false);           
       }
       else if (sta==1){
          BFDmsg.set(8,false);
          BFDmsg.set(9,true);           
       }
       else if (sta==2){
           BFDmsg.set(8,true);
          BFDmsg.set(9,false);           
       }
       else if (sta==3){
          BFDmsg.set(8,true);
          BFDmsg.set(9,true);           
       }
       else {
           System.out.println("Unsupported State");
           System.out.println("State was not set");
           return;
       }
        System.out.println("State set");        
   }
   
   BitSet getState(){
       return BFDmsg.get(8,10);
   }
   void setPoll(int poll){
       if (poll==0){
           BFDmsg.set(10,false);
       }
       else if (poll==1){
           BFDmsg.set(10,true);
       }
       System.out.println("Poll set");
   }
   
   boolean getPoll(){
       return BFDmsg.get(10);
   }
   
   void setFinal(int F){
       if (F==0){
           BFDmsg.set(11,false);
       }
       else if (F==1){
           BFDmsg.set(11,true);
       }
       System.out.println("Final set");
   }
   
   boolean getFinal(){
       return BFDmsg.get(11);
   }
      
   void setControlPlaneIndependent(int C){
        if (C==0){
           BFDmsg.set(12,false);
       }
       else if (C==1){
           BFDmsg.set(12,true);
       }
       System.out.println("Control Plane Independent set");      
   }
   
   boolean getControlPlaneIndpendent(){
       return BFDmsg.get(12);
   }
      
   void setAuthenticationPresent(int A){
        if (A==0){
           BFDmsg.set(13,false);
       }
       else if (A==1){
           BFDmsg.set(13,true);
           System.out.println("Warning!! Authentication is not supported yet");
       }
       System.out.println("Authentication set");      
   } 
   
   boolean getAuthenticationPresent(){
       return BFDmsg.get(13);
   }
   
   void setDemand(int D){
        if (D==0){
           BFDmsg.set(14,false);
       }
       else if (D==1){
           BFDmsg.set(14,true);
       }
       System.out.println("Demand set");       
   }
   
   boolean getDemand(){
       return BFDmsg.get(14);
   }
      
   void setMultipoint(int M){
        if (M==0){
           BFDmsg.set(15,false);
       }
       else if (M==1){
           BFDmsg.set(15,true);
       }
       System.out.println("Multipoint set");        
   }
   
   boolean getMultipoint(){
       return BFDmsg.get(15);
   }
      
   void setDetectMult(int dm){
       String str=Integer.toBinaryString(dm);
       while (str.length()<8){
           str="0"+str;
       } 
       if (str.length()>8){
           System.out.println("Warning!!! Detect Multipoint value will cause an overflow");
           return;
       }
       for (int i=0;i<str.length();i++){
           if (Character.getNumericValue(str.charAt(i))==0){
           BFDmsg.set(16+i,false);
           }
           else {
              BFDmsg.set(16+i,true); 
           }
       }
       System.out.println("Detect Multipoint set");
   }
   
   BitSet getDetectMult(){
       return BFDmsg.get(16,24);
   }
   void setLength(int L){
       String str=Integer.toBinaryString(L);
       while (str.length()<8){
           str="0"+str;
       } 
       if (str.length()>8){
           System.out.println("Warning!!! Length value will cause an overflow");
           return;
       }
       for (int i=0;i<str.length();i++){
           if (Character.getNumericValue(str.charAt(i))==0){
           BFDmsg.set(24+i,false);
           }
           else {
              BFDmsg.set(24+i,true); 
           }
       }
       System.out.println("Length set");       
   }
   
    BitSet getLength(){
       return BFDmsg.get(24,32);
   }
      
   void setMyDiscriminator(int myd){
       String str=Integer.toBinaryString(myd);
       if (myd==0){
           System.out.println("Warning!!! Discriminator values must be nonZero");
           return;
       }
       while (str.length()<32){
           str="0"+str;
       } 
       if (str.length()>32){
           System.out.println("Warning!!! My Discriminator value will cause an overflow");
           return;
       }
       for (int i=0;i<str.length();i++){
           if (Character.getNumericValue(str.charAt(i))==0){
           BFDmsg.set(32+i,false);
           }
           else {
              BFDmsg.set(32+i,true); 
           }
       }
       System.out.println("My discriminator set");         
   }
   
   BitSet getMyDiscriminator(){
       return BFDmsg.get(32, 64);
   }
   void setYourDiscriminator(int yd){
       String str=Integer.toBinaryString(yd);
       if (yd==0){
           System.out.println("Warning!!! Discriminator values must be nonZero");
           return;
       }
       while (str.length()<32){
           str="0"+str;
       } 
       if (str.length()>32){
           System.out.println("Warning!!! My Discriminator value will cause an overflow");
           return;
       }
       for (int i=0;i<str.length();i++){
           if (Character.getNumericValue(str.charAt(i))==0){
           BFDmsg.set(64+i,false);
           }
           else {
              BFDmsg.set(64+i,true); 
           }
       }
       System.out.println("Your discriminator set");         
   }
      
    BitSet getYourDiscriminator(){
       return BFDmsg.get(64, 96);
   }
    void setDesiredMinTxInterval(int interval){
       String str=Integer.toBinaryString(interval);
       while (str.length()<32){
           str="0"+str;
       } 
       if (str.length()>32){
           System.out.println("Warning!!! Desired Min Tx Interval value will cause an overflow");
           return;
       }
       for (int i=0;i<str.length();i++){
           if (Character.getNumericValue(str.charAt(i))==0){
           BFDmsg.set(96+i,false);
           }
           else {
              BFDmsg.set(96+i,true); 
           }
       }
       System.out.println("Desired Min Tx Interval set");         
   }  
      
    BitSet getDesiredMinTxInterval(){
       return BFDmsg.get(96,128);
   }
    void setRequiredMinRxInterval(int interval){
       String str=Integer.toBinaryString(interval);
       while (str.length()<32){
           str="0"+str;
       } 
       if (str.length()>32){
           System.out.println("Warning!!! Required Min Rx Interval value will cause an overflow");
           return;
       }
       for (int i=0;i<str.length();i++){
           if (Character.getNumericValue(str.charAt(i))==0){
           BFDmsg.set(128+i,false);
           }
           else {
              BFDmsg.set(128+i,true); 
           }
       }
       System.out.println("Required Min Rx Interval set");         
   }
      
    BitSet getRequiredMinRxInterval(){
          return BFDmsg.get(128,160);
      }
    void setRequiredMinEchoRxInterval(int interval){
       String str=Integer.toBinaryString(interval);
       while (str.length()<32){
           str="0"+str;
       } 
       if (str.length()>32){
           System.out.println("Warning!!! Required Echo Min Rx Interval value will cause an overflow");
           return;
       }
       for (int i=0;i<str.length();i++){
           if (Character.getNumericValue(str.charAt(i))==0){
           BFDmsg.set(160+i,false);
           }
           else {
              BFDmsg.set(160+i,true); 
           }
       }
       System.out.println("Required Echo Min Rx Interval set");             
      }
      
    BitSet getRequiredMinEchoRxInterval(){
          return BFDmsg.get(160,192);
      }
}
