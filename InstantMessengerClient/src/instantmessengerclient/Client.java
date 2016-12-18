/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package instantmessengerclient;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *
 * @author silus
 */
public class Client extends JFrame{
    private JTextField userText;
    private JTextArea chatWindow;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String message = "";
    private String serverIP;
    private Socket connection;
    
    //Constructor creates the GUI
    public Client(String host) {
        super("Client side IMessenger");
        serverIP = host;
        userText = new JTextField();
        userText.setEditable(false);
        userText.addActionListener(
                new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    sendMessage(e.getActionCommand());
                    userText.setText("");
                }
            }
        );
        add(userText, BorderLayout.NORTH);
        chatWindow = new JTextArea();
        add(new JScrollPane(chatWindow), BorderLayout.CENTER);
        setSize(320,180);
        setVisible(true);
    }
    
    //Connecting to server
    public void startRunning(){
        try{
            connectToServer();
            setupStreams();
            whileChatting();
        }catch(EOFException eofException){
            showMessage("\n Client closed connection! ");
        }catch(IOException ioException){
            ioException.printStackTrace();
        }finally{
            closeConnection();
        }
    }
    
    //connectToServer
    private void connectToServer(){
        showMessage("Trying to connect... \n");
        try{
        connection = new Socket(InetAddress.getByName(serverIP), 8124); 
        showMessage("Connected to: " + connection.getInetAddress().getHostName());
        }catch(IOException ioException){
            ioException.printStackTrace();
        }
    }
    
    //Set up Input and Output Streams
    private void setupStreams() throws IOException {
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();
        input = new ObjectInputStream(connection.getInputStream());
        showMessage("\n Client side - Input and Output Streams set-up! \n");
    }

    private void whileChatting() throws IOException {
        ableToType(true);
        do{
            try {
                message = (String) input.readObject();
                showMessage("\n" + message); 
            } catch(ClassNotFoundException classNotFoundException) {
                showMessage("\n Unidentified object type! "); 
            }
        }while(!message.equals("SERVER - END"));
                
    }
    
    private void closeConnection(){
        showMessage("\n Closing the connection...");
        ableToType(false);
        try{
            output.close();
            input.close();
            connection.close();
        }catch(IOException iof){
            iof.printStackTrace();
        }
    }
    
    //Sending messages to server program
    private void sendMessage(String message){
        try {
            output.writeObject("CLIENT - " + message);
            output.flush();
            showMessage("\nCLIENT - " + message);
        } catch(IOException ioException){
            chatWindow.append("\n Error: Can't send the message!!");
        }
    }
    
    //showMessage Chat History
    private void showMessage(final String text) {
        SwingUtilities.invokeLater(     //Set a thread to update parts of GUI
                new Runnable() {
                    public void run(){
                        chatWindow.append(text);
                    }
                }
        );  
    }
    
    //ableToType Change state of ableToType
     private void ableToType(final boolean variable){
         SwingUtilities.invokeLater(     //Set a thread to update parts of GUI
                new Runnable() {
                    public void run(){
                        userText.setEditable(variable);
                    }
                }
        );
    }
        
        
}
