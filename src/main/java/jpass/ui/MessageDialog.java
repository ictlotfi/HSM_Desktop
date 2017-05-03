/*
 * JPass
 *
 * Copyright (c) 2009-2017 Gabor Bata
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package jpass.ui;

import applets.MyAPDU;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import jpass.util.CryptUtils;
import jpass.util.SpringUtilities;
import jpass.util.StringUtils;

/**
 * Utility class for displaying message dialogs.
 *
 * @author Gabor_Bata
 *
 */
public final class MessageDialog extends JDialog implements ActionListener {
    private static final long serialVersionUID = -1860703845867414123L;

    public static final int DEFAULT_OPTION = -1;
    public static final int YES_NO_OPTION = 0;
    public static final int YES_NO_CANCEL_OPTION = 1;
    public static final int OK_CANCEL_OPTION = 2;

    public static final int YES_OPTION = 0;
    public static final int OK_OPTION = 0;
    public static final int NO_OPTION = 1;
    public static final int CANCEL_OPTION = 2;
    public static final int CLOSED_OPTION = -1;
    private static MyAPDU m_apdu;

    private int selectedOption;

    private MessageDialog(final Dialog parent, final Object message, final String title, ImageIcon icon, int optionType) {
        super(parent);
        initializeDialog(parent, message, title, icon, optionType);
    }

    private MessageDialog(final Frame parent, final Object message, final String title, ImageIcon icon, int optionType) {
        super(parent);
        initializeDialog(parent, message, title, icon, optionType);
    }

    private void initializeDialog(final Component parent, final Object message, final String title, ImageIcon icon, int optionType) {
        setModal(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(title);
        this.selectedOption = CLOSED_OPTION;

        try {
            m_apdu = new MyAPDU();
        } catch (Exception ex) {
            Logger.getLogger(MessageDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        JButton defaultButton;
        switch (optionType) {
        case YES_NO_OPTION:
            defaultButton = createButton("Yes", YES_OPTION, getIcon("accept"));
            buttonPanel.add(defaultButton);
            buttonPanel.add(createButton("No", NO_OPTION, getIcon("close")));
            break;
        case YES_NO_CANCEL_OPTION:
            defaultButton = createButton("Yes", YES_OPTION, getIcon("accept"));
            buttonPanel.add(defaultButton);
            buttonPanel.add(createButton("No", NO_OPTION, getIcon("close")));
            buttonPanel.add(createButton("Cancel", CANCEL_OPTION, getIcon("cancel")));
            break;
        case OK_CANCEL_OPTION:
            defaultButton = createButton("OK", OK_OPTION, getIcon("accept"));
            buttonPanel.add(defaultButton);
            buttonPanel.add(createButton("Cancel", CANCEL_OPTION, getIcon("cancel")));
            break;
        default:
            defaultButton = createButton("OK", OK_OPTION, getIcon("accept"));
            buttonPanel.add(defaultButton);
            break;
        }
        getRootPane().setDefaultButton(defaultButton);

        JPanel mainPanel = new JPanel(new BorderLayout(5, 0));

        float widthMultiplier;
        JPanel messagePanel = new JPanel(new BorderLayout());
        if (message instanceof JScrollPane) {
            widthMultiplier = 1.0f;
            messagePanel.add((Component) message, BorderLayout.CENTER);
        } else if (message instanceof Component) {
            widthMultiplier = 1.5f;
            messagePanel.add((Component) message, BorderLayout.NORTH);
        } else {
            widthMultiplier = 1.0f;
            messagePanel.setBorder(new EmptyBorder(10, 0, 10, 10));
            messagePanel.add(new JLabel("<html>" + String.valueOf(message).replaceAll("\\n", "<br />") + "</html>"), BorderLayout.CENTER);
        }
        mainPanel.add(messagePanel, BorderLayout.CENTER);

        if (icon != null) {
            JLabel image = new JLabel(icon);
            image.setVerticalAlignment(SwingConstants.TOP);
            image.setBorder(new EmptyBorder(10, 10, 0, 10));
            mainPanel.add(image, BorderLayout.WEST);
        }
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        getContentPane().add(mainPanel);
        setResizable(false);
        pack();
        setSize((int) (getWidth() * widthMultiplier), getHeight());
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private JButton createButton(String name, int option, ImageIcon icon) {
        JButton button = new JButton(name, icon);
        button.setMnemonic(name.charAt(0));
        button.setActionCommand(String.valueOf(option));
        button.addActionListener(this);
        return button;
    }

    private int getSelectedOption() {
        return this.selectedOption;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        this.selectedOption = Integer.parseInt(event.getActionCommand());
        dispose();
    }

    private static void showMessageDialog(final Component parent, final Object message, final String title, ImageIcon icon) {
        showMessageDialog(parent, message, title, icon, DEFAULT_OPTION);
    }

    private static int showMessageDialog(final Component parent, final Object message, final String title, ImageIcon icon, int optionType) {
        int ret = CLOSED_OPTION;
        MessageDialog dialog = null;
        if (parent instanceof Frame) {
            dialog = new MessageDialog((Frame) parent, message, title, icon, optionType);
        } else if (parent instanceof Dialog) {
            dialog = new MessageDialog((Dialog) parent, message, title, icon, optionType);
        }
        if (dialog != null) {
            ret = dialog.getSelectedOption();
        }
        return ret;
    }

    /**
     * Shows a warning message.
     *
     * @param parent parent component
     * @param message dialog message
     */
    public static void showWarningMessage(final Component parent, final String message) {
        showMessageDialog(parent, message, "Warning", getIcon("dialog_warning"));
    }

    /**
     * Shows an error message.
     *
     * @param parent parent component
     * @param message dialog message
     */
    public static void showErrorMessage(final Component parent, final String message) {
        showMessageDialog(parent, message, "Error", getIcon("dialog_error"));
    }

    /**
     * Shows an information message.
     *
     * @param parent parent component
     * @param message dialog message
     */
    public static void showInformationMessage(final Component parent, final String message) {
        showMessageDialog(parent, message, "Information", getIcon("dialog_info"));
    }

    /**
     * Shows a question dialog.
     *
     * @param parent parent component
     * @param message dialog message
     * @param optionType question type
     * @return selected option
     */
    public static int showQuestionMessage(final Component parent, final String message, final int optionType) {
        return showMessageDialog(parent, message, "Confirmation", getIcon("dialog_question"), optionType);
    }

    /**
     * Shows a m_pin dialog.
     *
     * @param parent parent component
     * @param confirm m_pin confirmation
     * @return the m_pin
     */
    public static byte[] showPasswordDialog(final Component parent, final boolean confirm) throws Exception {
        JPanel panel = new JPanel();
        panel.add(new JLabel("PIN:"));
        final JPasswordField m_pin = TextComponentFactory.newPasswordField();
        panel.add(m_pin);
        JPasswordField repeat_pin = null;
        if (confirm) {
            repeat_pin = TextComponentFactory.newPasswordField();
            panel.add(new JLabel("Repeat:"));
            panel.add(repeat_pin);
        }
        panel.setLayout(new SpringLayout());
        SpringUtilities.makeCompactGrid(panel, confirm ? 2 : 1, 2, 5, 5, 5, 5);
        boolean notCorrect = true;

        while (notCorrect) {
            int option = showMessageDialog(parent, panel, "Enter PIN", getIcon("dialog_lock"), OK_CANCEL_OPTION);
            if (option == OK_OPTION) {
                System.out.println("PIN: "+m_pin.getPassword()[0]);
                if (m_pin.getPassword().length != 4 || !isNumeric(m_pin.getPassword())) {
                    showWarningMessage(parent, "Please enter a valid PIN.");
                } else if (confirm && !Arrays.equals(m_pin.getPassword(), repeat_pin.getPassword())) {
                    showWarningMessage(parent, "PIN and repeated PIN are not identical.");
                } else {
                    notCorrect = false;
                }
            } else {
                return null;
            }
        }

        // Now we are sure that we have a valid PIN of 4 digits
        System.out.println("PIN: "+m_pin.getPassword()[0]);
        
        char[] pin_array = m_pin.getPassword();
        
        
/*SIMULATION CODE--------------------------------------------------------------------******************/        
       // m_apdu.executeRun(); // Change the card State to SETUP state
        /*if (!confirm) {// new file
            m_apdu.verifyPin((byte) pin_array[0], (byte) pin_array[1], 
                (byte) pin_array[2], (byte) pin_array[3]); // Change the card State to NORMAL state
            }
        else { // verify PIN
            m_apdu.verifyPin((byte) pin_array[0], (byte) pin_array[1], 
                (byte) pin_array[2], (byte) pin_array[3]); // Change the card State to AUTHORIZED stat
        }*/
        
       /* m_apdu.setPin((byte) pin_array[0], (byte) pin_array[1], 
                (byte) pin_array[2], (byte) pin_array[3]); // Change the card State to NORMAL state
        */
       String key = "-1";
       
       
       System.out.println("pin_array[0]=> "+Character.getNumericValue(pin_array[0]));
       System.out.println("pin_araaray[0]=> "+pin_array[1]);
       System.out.println("pin_array[0]=> "+pin_array[2]);
       System.out.println("pin_arraaay[0]=> "+Character.getNumericValue(pin_array[3]));
       
       
        int result = m_apdu.verifyPin(Character.getNumericValue(pin_array[0]),  Character.getNumericValue(pin_array[1]), 
                Character.getNumericValue(pin_array[2]), Character.getNumericValue(pin_array[3])); // Change the card State to AUTHORIZED state
        
        switch (result) {
          
            case -1:
                key = m_apdu.getKey();
                if (key.equals("-1")) {
                    showWarningMessage(parent, "bad PIN.");
                }
                break;
            case -2:                
                key = m_apdu.getKey();
                if (key.equals("-1")) {
                    showWarningMessage(parent, "invalid operation.");
                }
                break;
            case -3:
                showWarningMessage(parent, "Locked.");
                break;
            default:
                break;
        }
        
        key = m_apdu.getKey();
        if (key.equals("-1")) {
            showWarningMessage(parent, "Error getting Key.");
        }
        // here we get the pin, send it to the javacard, then receive the key and encrypt it, finally send it back
        byte[] passwordHash = null;
        try {
            passwordHash = CryptUtils.getPKCS5Sha256Hash(key.toCharArray());
        } catch (Exception e) {
            showErrorMessage(parent,
                    "Cannot generate password hash:\n" + StringUtils.stripString(e.getMessage()) + "\n\nOpening and saving files are not possible!");
        }
        return passwordHash;
    }
    
    private static boolean isNumeric(char[] array){
        try{
            String str = new String(array);
            int num = Integer.parseInt(str);
            return num > 0;
            // is an integer!
          } catch (NumberFormatException e) {
            // not an integer!
            return false;
          }
    }

    /**
     * Returns an image resource.
     *
     * @param name image name without path and extension
     * @return ImageIcon object
     */
    public static ImageIcon getIcon(String name) {
        try {
            return new ImageIcon(MessageDialog.class.getClassLoader().getResource("resources/images/" + name + ".png"));
        } catch (Exception e) {
            return null;
        }
    }

    /** Get resource as string */
    private static String getResourceAsString(String name) {
        StringBuilder builder = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            InputStream is = MessageDialog.class.getClassLoader().getResourceAsStream("resources/" + name);
            bufferedReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line).append('\n');
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return builder.toString();
    }

    /**
     * Shows a textfile from the classpath.
     *
     * @param parent parent component
     * @param title window title
     * @param textFile text file name
     */
    public static void showTextFile(final Component parent, final String title, final String textFile) {
        JTextArea area = TextComponentFactory.newTextArea(getResourceAsString(textFile));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        showMessageDialog(parent, scrollPane, title, null, DEFAULT_OPTION);
    }
}
