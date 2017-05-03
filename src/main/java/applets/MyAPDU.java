/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package applets;

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
        // Start Connection
            byte[] installData = new byte[13]; // no special install data passed now - can be used to pass initial keys etc.
            installData[0] = (byte) 0;
            installData[1] = (byte) 0;
            installData[2] = (byte) 10;
            
            installData[3] = (byte) 1;
            installData[4] = (byte) 1;
            installData[5] = (byte) 1;
            installData[6] = (byte) 1;
            installData[7] = (byte) 1;
            installData[8] = (byte) 1;
            installData[9] = (byte) 1;
            installData[10] = (byte) 1;
            installData[11] = (byte) 1;
            installData[12] = (byte) 1;
            System.out.println("succ=> 1");
            //cardManager.prepareLocalSimulatorApplet(APPLET_AID, installData, SimpleApplet.class);
            // System.out.println("succ=> 2");
            
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
    
    public void executeRun() throws Exception{
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
        
    }
    
    public boolean setPin(byte a, byte b, byte c, byte d) throws Exception{
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
        String result = CardMngr.bytesToHex(response);

        System.out.println("result of setPin=> "+result);
        
        return response.getSW() == 0x9000;
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
        String result = CardMngr.bytesToHex(response);

        System.out.println("result of verifyPin=> "+result);
        

        switch (response.getSW()) {
            case 0x9000:
                return 1;
            case 0x6900:
                return -1; // bad PIN
            case 0x6680:
                return -2; // invalid operation
            default:
                return -3; // locked
        }
    }
    
    public int verifyPuk(byte[] array) throws Exception{
        short additionalDataLen = 10;
        byte apdu[] = new byte[CardMngr.HEADER_LENGTH + additionalDataLen];
        apdu[CardMngr.OFFSET_CLA] = (byte) 0xB0;
        apdu[CardMngr.OFFSET_INS] = (byte) 0x53;
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
        
        switch (response.getSW()) {
            case 0x9000:
                return 1;
            case 0x6900:
                return -1; // bad PUK
            case 0x6680:
                return -2; // invalid operation
            default:
                return -3; // locked
        }
    }
}
