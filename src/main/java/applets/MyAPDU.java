/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package applets;

import static applets.CardMngr.bytesToHex;
import javax.smartcardio.ResponseAPDU;

/**
 *
 * @author ismail
 */
public class MyAPDU {
    private static byte APPLET_AID[] = {(byte) 0x73, (byte) 0x69, (byte) 0x6D, (byte) 0x70, (byte) 0x6C, 
        (byte) 0x65, (byte) 0x61, (byte) 0x70, (byte) 0x70, (byte) 0x6C, (byte) 0x65, (byte) 0x74};
    static CardMngr cardManager = new CardMngr();
    
    
    public MyAPDU () throws Exception{            
            if(cardManager.ConnectToCard()){
                System.out.println("succ=> ConnectToCard");
            }
    }
    
    public String getKey() throws Exception{
        short additionalDataLen = 0;
            byte apdu[] = new byte[CardMngr.HEADER_LENGTH + additionalDataLen];
            apdu[CardMngr.OFFSET_CLA] = (byte) 0xB0;
            apdu[CardMngr.OFFSET_INS] = (byte) 0x50;
            apdu[CardMngr.OFFSET_P1] = (byte) 0x00;
            apdu[CardMngr.OFFSET_P2] = (byte) 0x00;
            apdu[CardMngr.OFFSET_LC] = (byte) additionalDataLen;
            
            ResponseAPDU response = cardManager.sendAPDU(apdu); 
            String result = CardMngr.bytesToHex(response);
        
            System.out.println("result of getKey=> "+result);
            
            if (response.getSW() != 0x9000){
                result =  "-1";
            }
          
        
        return result;
    }
    
    public int executeRun() throws Exception{
        // TODO: prepare proper APDU command
        short additionalDataLen = 0;
        byte apdu[] = new byte[CardMngr.HEADER_LENGTH + additionalDataLen];
        apdu[CardMngr.OFFSET_CLA] = (byte) 0xB0;
        apdu[CardMngr.OFFSET_INS] = (byte) 0x55;
        apdu[CardMngr.OFFSET_P1] = (byte) 0x00;
        apdu[CardMngr.OFFSET_P2] = (byte) 0x00;
        apdu[CardMngr.OFFSET_LC] = (byte) additionalDataLen;

        ResponseAPDU response = cardManager.sendAPDU(apdu); 
        String result = CardMngr.bytesToHex(response);

        System.out.println("result of run=> "+result);
        
        return response.getSW();
    }
    
    public int setPin(byte a, byte b, byte c, byte d) throws Exception{
        short additionalDataLen = 4;
        byte apdu[] = new byte[CardMngr.HEADER_LENGTH + additionalDataLen];
        apdu[CardMngr.OFFSET_CLA] = (byte) 0xB0;
        apdu[CardMngr.OFFSET_INS] = (byte) 0x52;
        apdu[CardMngr.OFFSET_P1] = (byte) 0x00;
        apdu[CardMngr.OFFSET_P2] = (byte) 0x00;
        apdu[CardMngr.OFFSET_LC] = (byte) additionalDataLen;

        
        apdu[CardMngr.OFFSET_DATA] = a;
        apdu[CardMngr.OFFSET_DATA+1] = b;
        apdu[CardMngr.OFFSET_DATA+2] = c;
        apdu[CardMngr.OFFSET_DATA+3] = d;
        ResponseAPDU response = cardManager.sendAPDU(apdu); 
        //String result = CardMngr.bytesToHex(response);

        //System.out.println("result of setPin=> "+result);
        
        return Integer.parseInt(bytesToHex(response.getBytes()));
    }
    
    public int verifyPin(int a, int b, int c, int d) throws Exception{
        short additionalDataLen = 4;
        byte apdu[] = new byte[CardMngr.HEADER_LENGTH + additionalDataLen];
        apdu[CardMngr.OFFSET_CLA] = (byte) 0xB0;
        apdu[CardMngr.OFFSET_INS] = (byte) 0x53;
        apdu[CardMngr.OFFSET_P1] = (byte) 0x00;
        apdu[CardMngr.OFFSET_P2] = (byte) 0x00;
        apdu[CardMngr.OFFSET_LC] = (byte) additionalDataLen;

        
        apdu[CardMngr.OFFSET_DATA] = (byte)a;
        apdu[CardMngr.OFFSET_DATA+1] = (byte)b;
        apdu[CardMngr.OFFSET_DATA+2] = (byte)c;
        apdu[CardMngr.OFFSET_DATA+3] = (byte)d;
        ResponseAPDU response = cardManager.sendAPDU(apdu); 
        //String result = CardMngr.bytesToHex(response);

       // System.out.println(response.getSW());
        ///System.out.println(bytesToHex(response.getBytes()));
                
        return Integer.parseInt(bytesToHex(response.getBytes()));
    }
    
    
    public int setPUK(byte[] array) throws Exception{
        short additionalDataLen = 10;
        byte apdu[] = new byte[CardMngr.HEADER_LENGTH + additionalDataLen];
        apdu[CardMngr.OFFSET_CLA] = (byte) 0xB0;
        apdu[CardMngr.OFFSET_INS] = (byte) 0x56;
        apdu[CardMngr.OFFSET_P1] = (byte) 0x00;
        apdu[CardMngr.OFFSET_P2] = (byte) 0x00;
        apdu[CardMngr.OFFSET_LC] = (byte) additionalDataLen;

        apdu[CardMngr.OFFSET_DATA] = array[0];
        apdu[CardMngr.OFFSET_DATA+1] = array[1];
        apdu[CardMngr.OFFSET_DATA+2] = array[2];
        apdu[CardMngr.OFFSET_DATA+3] = array[3];
        apdu[CardMngr.OFFSET_DATA+4] = array[4];
        apdu[CardMngr.OFFSET_DATA+5] = array[5];
        apdu[CardMngr.OFFSET_DATA+6] = array[6];
        apdu[CardMngr.OFFSET_DATA+7] = array[7];
        apdu[CardMngr.OFFSET_DATA+8] = array[8];
        apdu[CardMngr.OFFSET_DATA+9] = array[9];
        
        ResponseAPDU response = cardManager.sendAPDU(apdu); 
        String result = CardMngr.bytesToHex(response);

        System.out.println("result of setPin=> "+result);
        
        return response.getSW();
    }
    
    
    public int verifyPuk(byte[] array) throws Exception{
        short additionalDataLen = 10;
        byte apdu[] = new byte[CardMngr.HEADER_LENGTH + additionalDataLen];
        apdu[CardMngr.OFFSET_CLA] = (byte) 0xB0;
        apdu[CardMngr.OFFSET_INS] = (byte) 0x54;
        apdu[CardMngr.OFFSET_P1] = (byte) 0x00;
        apdu[CardMngr.OFFSET_P2] = (byte) 0x00;
        apdu[CardMngr.OFFSET_LC] = (byte) additionalDataLen;

        
        apdu[CardMngr.OFFSET_DATA] = array[0];
        apdu[CardMngr.OFFSET_DATA+1] = array[1];
        apdu[CardMngr.OFFSET_DATA+2] = array[2];
        apdu[CardMngr.OFFSET_DATA+3] = array[3];
        apdu[CardMngr.OFFSET_DATA+4] = array[4];
        apdu[CardMngr.OFFSET_DATA+5] = array[5];
        apdu[CardMngr.OFFSET_DATA+6] = array[6];
        apdu[CardMngr.OFFSET_DATA+7] = array[7];
        apdu[CardMngr.OFFSET_DATA+8] = array[8];
        apdu[CardMngr.OFFSET_DATA+9] = array[9];
        
        
        ResponseAPDU response = cardManager.sendAPDU(apdu); 
        String result = CardMngr.bytesToHex(response);

        System.out.println("result of verifyPuk=> "+result);
        
        return Integer.parseInt(bytesToHex(response.getBytes()));
    }
    
    
}
