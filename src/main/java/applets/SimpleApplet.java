package applets;

import javacard.framework.*;
import javacard.security.*;
import javacardx.crypto.*;

public class SimpleApplet extends javacard.framework.Applet
{
    // MAIN INSTRUCTION CLASS
    //final static byte CLA_SIMPLEAPPLET                = (byte) 0xB0;
    final static byte CLA_PROJECTAPPLET         = (byte) 0xB0;

    // INSTRUCTIONS
    final static byte INS_SENDKEY               = (byte) 0x50;
    final static byte INS_CHANGEKEY             = (byte) 0x51;
    final static byte INS_SETPIN                = (byte) 0x52;
    final static byte INS_VERIFYPIN             = (byte) 0x53;
    final static byte INS_VERIFYPUK             = (byte) 0x54;
    final static byte INS_RUN                   = (byte) 0x55;

    final static short SW_BAD_PARAMETER              = (short) 0x6710;
    final static short SW_KEY_LENGTH_BAD             = (short) 0x6715;
    final static short SW_INVALID_OPERATION          = (short) 0x6680;
    final static short SW_BAD_PIN                    = (short) 0x6900;
    final static short SW_LOCKED                     = (short) 0x6920;
    
    
    final static short ARRAY_LENGTH                   = (short) 0xff;
    final static byte  AES_BLOCK_LENGTH               = (short) 0x16;

    
    final static short SW_Exception                     = (short) 0xff01;
    final static short SW_ArrayIndexOutOfBoundsException = (short) 0xff02;
    final static short SW_ArithmeticException           = (short) 0xff03;
    final static short SW_ArrayStoreException           = (short) 0xff04;
    final static short SW_NullPointerException          = (short) 0xff05;
    final static short SW_NegativeArraySizeException    = (short) 0xff06;
    final static short SW_CryptoException_prefix        = (short) 0xf100;
    final static short SW_SystemException_prefix        = (short) 0xf200;
    final static short SW_PINException_prefix           = (short) 0xf300;
    final static short SW_TransactionException_prefix   = (short) 0xf400;
    final static short SW_CardRuntimeException_prefix   = (short) 0xf500;
    
    
    final static byte FACTORY         = (byte) 1;
    final static byte SETUP           = (byte) 2;
    final static byte NORMAL          = (byte) 3;
    final static byte FAILED          = (byte) 4;
    final static byte AUTHORIZED      = (byte) 5;
    final static byte LOCKED          = (byte) 6;
    
    
    final static byte PIN_LENGTH      = (byte) 4;
    final static byte PIN_TRIES       = (byte) 5;
    final static byte PUK_LENGTH      = (byte) 10;
    final static byte PUK_TRIES       = (byte) 3;
    
    
        private   OwnerPIN       m_puk = null;
    private   byte           state;
    private   RandomData     m_random = null;


    private   AESKey         m_aesKey = null;
    private   Cipher         m_encryptCipher = null;
    private   Cipher         m_decryptCipher = null;
    private   RandomData     m_secureRandom = null;
    private   MessageDigest  m_hash = null;
    private   OwnerPIN       m_pin = null;
    private   Signature      m_sign = null;
    private   KeyPair        m_keyPair = null;
    private   Key            m_privateKey = null;
    private   Key            m_publicKey = null;

    private   short          m_apduLogOffset = (short) 0;
    // TEMPORARRY ARRAY IN RAM
    private   byte        m_ramArray[] = null;
    // PERSISTENT ARRAY IN EEPROM
    private   byte       m_dataArray[] = null;

    /**
     * SimpleApplet default constructor
     * Only this class's install method should create the applet object.
     */
    protected SimpleApplet(byte[] buffer, short offset, byte length)
    {
        short dataOffset = offset;
        
        // shift to privilege offset
        dataOffset += (short)( 1 + buffer[offset]);
        
        // finally shift to Application specific offset
        dataOffset += (short)( 1 + buffer[dataOffset]);
        
        //System.out.println("////////////// dataOffset "+dataOffset);
        byte lData = buffer[dataOffset];
        
        // go to proprietary data
        dataOffset++;
        
        /*System.out.println("//////////////");
        System.out.println(buffer[offset]);
        System.out.println(buffer[dataOffset]);
        
        System.out.println("//////////////");
        System.out.println(lData);
        System.out.println(PUK_LENGTH);*/
        //if (lData == PUK_LENGTH) {        
            m_ramArray = JCSystem.makeTransientByteArray((short) 260, JCSystem.CLEAR_ON_DESELECT);
            Util.arrayFillNonAtomic(m_ramArray, (short) 0, (short) 260, (byte) 0);

            m_pin = new OwnerPIN(PIN_TRIES, PIN_LENGTH);
            m_puk = new OwnerPIN(PUK_TRIES, PUK_LENGTH);
            m_aesKey = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_256, false);
            m_random = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);

            m_random.generateData(m_ramArray, (short) 0, (short) (KeyBuilder.LENGTH_AES_256 / 8));
            m_aesKey.setKey(m_ramArray, (short) 0);

            //m_puk.update(buffer, dataOffset, PUK_LENGTH);

            state = FACTORY;
        /*} else {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }*/
        
        register();
    }

    /**
     * Method installing the applet.
     * @param bArray the array constaining installation parameters
     * @param bOffset the starting offset in bArray
     * @param bLength the length in bytes of the data parameter in bArray
     */
    public static void install(byte[] bArray, short bOffset, byte bLength) throws ISOException
    {
        // applet  instance creation 
        //System.out.println(bOffset);
        //System.out.println(bLength);
        new SimpleApplet (bArray, bOffset, bLength);
    }

    /**
     * Select method returns true if applet selection is supported.
     * @return boolean status of selection.
     */
    public boolean select()
    {
        // <PUT YOUR SELECTION ACTION HERE>
        
      return true;
    }

    /**
     * Deselect method called by the system in the deselection process.
     */
    public void deselect()
    {

        // <PUT YOUR DESELECTION ACTION HERE>

        return;
    }

    /**
     * Method processing an incoming APDU.
     * @see APDU
     * @param apdu the incoming APDU
     * @exception ISOException with the response bytes defined by ISO 7816-4
     */
    public void process(APDU apdu) throws ISOException
    {
        byte[] apduBuffer = apdu.getBuffer();

        if (state == LOCKED) {
            return;
        }
        
        if (apdu.isISOInterindustryCLA()) {
            if (selectingApplet()) {
                return;
            } else {
                ISOException.throwIt (ISO7816.SW_CLA_NOT_SUPPORTED); 
            }
        }

        try {
            if (apduBuffer[ISO7816.OFFSET_CLA] == CLA_PROJECTAPPLET) {
                switch ( apduBuffer[ISO7816.OFFSET_INS] )
                {
                    case INS_SENDKEY: sendKey(apdu); break;
                    case INS_CHANGEKEY: changeKey(apdu); break;
                    case INS_SETPIN: setPIN(apdu); break;
                    case INS_VERIFYPIN: verifyPIN(apdu); break;
                    case INS_VERIFYPUK: verifyPUK(apdu); break;
                    case INS_RUN: run(apdu); break;
                    default :
                        ISOException.throwIt( ISO7816.SW_INS_NOT_SUPPORTED ) ;
                    break ;

                }
            }
            else ISOException.throwIt( ISO7816.SW_CLA_NOT_SUPPORTED);
            
            // Capture all reasonable exceptions and change into readable ones (instead of 0x6f00) 
        } catch (ISOException e) {
            throw e; // Our exception from code, just re-emit
        } catch (ArrayIndexOutOfBoundsException e) {
            ISOException.throwIt(SW_ArrayIndexOutOfBoundsException);
        } catch (ArithmeticException e) {
            ISOException.throwIt(SW_ArithmeticException);
        } catch (ArrayStoreException e) {
            ISOException.throwIt(SW_ArrayStoreException);
        } catch (NullPointerException e) {
            ISOException.throwIt(SW_NullPointerException);
        } catch (NegativeArraySizeException e) {
            ISOException.throwIt(SW_NegativeArraySizeException);
        } catch (CryptoException e) {
            ISOException.throwIt((short) (SW_CryptoException_prefix | e.getReason()));
        } catch (SystemException e) {
            ISOException.throwIt((short) (SW_SystemException_prefix | e.getReason()));
        } catch (PINException e) {
            ISOException.throwIt((short) (SW_PINException_prefix | e.getReason()));
        } catch (TransactionException e) {
            ISOException.throwIt((short) (SW_TransactionException_prefix | e.getReason()));
        } catch (CardRuntimeException e) {
            ISOException.throwIt((short) (SW_CardRuntimeException_prefix | e.getReason()));
        } catch (Exception e) {
            ISOException.throwIt(SW_Exception);
        }
        
    }

    public void sendKey(APDU apdu) throws ISOException
    {
        byte[] apdubuf = apdu.getBuffer();
        
        if ((state != FACTORY) && (state != AUTHORIZED)) {
            ISOException.throwIt(SW_INVALID_OPERATION);
        }
        
        m_aesKey.getKey(apdubuf, ISO7816.OFFSET_CDATA);
        apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (short) (m_aesKey.getSize() / (short) 8));
        
        if (state == AUTHORIZED) {
            state = NORMAL;
        }
    }
    
    public void changeKey(APDU apdu) throws ISOException
    {
        byte[] apdubuf = apdu.getBuffer();
        short dataLen = apdu.setIncomingAndReceive();
        
        if (state != FACTORY) {
            ISOException.throwIt(SW_INVALID_OPERATION);
        }
        
        if (apdubuf[ISO7816.OFFSET_P1] == (byte) 0) {
            if ((short) (dataLen * (byte) 8) != KeyBuilder.LENGTH_AES_256) {
                ISOException.throwIt(SW_KEY_LENGTH_BAD);
            }
            m_aesKey.setKey(apdubuf, ISO7816.OFFSET_CDATA);
        } else if (apdubuf[ISO7816.OFFSET_P1] == (byte) 1) {
            m_random.generateData(m_ramArray, (short) 0, (short) (KeyBuilder.LENGTH_AES_256 / (byte) 8));
            m_aesKey.setKey(m_ramArray, (short) (KeyBuilder.LENGTH_AES_256 / (byte) 8));
        } else {
            ISOException.throwIt(SW_BAD_PARAMETER);
        }
        
    }
    
    public void setPIN(APDU apdu) throws ISOException
    {
        byte[] apdubuf = apdu.getBuffer();
        short dataLen = apdu.setIncomingAndReceive();
        
        if ((state != SETUP) && (state != AUTHORIZED)) {
            ISOException.throwIt(SW_INVALID_OPERATION);
        }
        
        if (dataLen != PIN_LENGTH) {
            ISOException.throwIt(SW_BAD_PIN);
        }
        
        m_pin.update(apdubuf, ISO7816.OFFSET_CDATA, PIN_LENGTH);
        
        state = NORMAL;
    }
    
    public void verifyPIN(APDU apdu) throws ISOException
    {
        byte[] apdubuf = apdu.getBuffer();
        short dataLen = apdu.setIncomingAndReceive();
        
        //System.out.println(state);
        if (state != NORMAL) {
            ISOException.throwIt(SW_INVALID_OPERATION);
        }
        
        if (m_pin.check(apdubuf, ISO7816.OFFSET_CDATA, (byte) dataLen) == false) {
            if (m_pin.getTriesRemaining() == (byte) 0) {
                state = FAILED;
            }
            ISOException.throwIt(SW_BAD_PIN);
        }
        
        m_pin.reset();
        state = AUTHORIZED;
    }
    
    public void verifyPUK(APDU apdu) throws ISOException
    {
        byte[] apdubuf = apdu.getBuffer();
        short dataLen = apdu.setIncomingAndReceive();
        
        if ((state != FAILED) && (state != NORMAL)) {
            ISOException.throwIt(SW_INVALID_OPERATION);
        }
        
        if (m_puk.check(apdubuf, ISO7816.OFFSET_CDATA, (byte) dataLen) == false) {
            if (m_puk.getTriesRemaining() == (byte) 0) {
                state = LOCKED;
                ISOException.throwIt(SW_LOCKED);
            }
            ISOException.throwIt(SW_BAD_PIN);
        }
        
        m_puk.reset();
        
        if (state == FAILED) {
            m_pin.resetAndUnblock();
            state = AUTHORIZED;
        } else {
            state = FACTORY;
        }
    }
    
    public void run(APDU apdu) throws ISOException
    {
        if (state != FACTORY) {
            ISOException.throwIt(SW_INVALID_OPERATION);
        }
        
        state = SETUP;
    }

    
}

