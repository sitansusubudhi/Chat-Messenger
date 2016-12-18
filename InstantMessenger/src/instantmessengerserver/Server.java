/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package instantmessengerserver;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
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
public class Server extends JFrame{
    private JTextField userText;
    private JTextArea chatWindow;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private ServerSocket server;
    private Socket connection;
    
    public Server() {
        super("Server Side IMessenger");
        userText = new JTextField();
        userText.setEditable(false);
        userText.addActionListener(
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage(e.getActionCommand());
                userText.setText("");
            }
                }
        );
        add(userText, BorderLayout.NORTH);
        chatWindow = new JTextArea();
        add(new JScrollPane(chatWindow));
        setSize(320,180);
        setVisible(true);
    }
    
    //Setting up the server 
    public void startRunning(){
        try{
            server = new ServerSocket(8124,100);        //PortNo - remember
            while(true){
                try{
                    //connect
                    waitForConnection();
                    setupStreams();
                    whileChatting();
                }catch(EOFException eofexception){  //connection ending
                    showMessage("\n Server connection ended!");
                }finally{
                    closeConnection();
                }
            }
        }catch(IOException ioexception){
            ioexception.printStackTrace();
        }
    }
    
    //wait for connection, then display 'connected!'

    private void waitForConnection() throws IOException {
        showMessage("Waiting for client to connect...\n");
        connection = server.accept();
        showMessage(" Connected to " + connection.getInetAddress().getHostName());
    }

    
    //setup streams to send and receive data
    private void setupStreams() throws IOException {
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();
        input = new ObjectInputStream(connection.getInputStream());
        showMessage("\n Input and Output Streams set-up! \n");
    }
    
    //The main chatting procedure
    private void whileChatting() throws IOException{
        String message = "You are connected! ";
        sendMessage(message);
        ableToType(true);
        do {
            try {
                message = (String) input.readObject();
                showMessage("\n" + message);
            } catch(ClassNotFoundException classNotFoundException){
                showMessage("\n Unidentified object type! ");
            }
        } while(!message.equals("CLIENT - END"));
    }

    //Closing streams and sockets after conversation ended
    private void closeConnection() {
         showMessage("\n Closing connections.... \n");
         ableToType(false);
         try {
             output.close();
             input.close();
             connection.close();
         } catch(IOException io) {
             io.printStackTrace();
         }
    }
    
    //sendMessage - send message to client
    private void sendMessage(String message){
        try {
            output.writeObject("SERVER - " + message);
            output.flush();
            showMessage("\nSERVER - " + message);
        } catch(IOException ioException){
            chatWindow.append("\n Error: Can't send the message!!");
        }
    }
    
    //showMessage - Display history of chats and prompts and updates chatWindow
    private void showMessage(final String text) {
        SwingUtilities.invokeLater(     //Set a thread to update parts of GUI
                new Runnable() {
                    public void run(){
                        chatWindow.append(text);
                    }
                }
        );  
    }
    
    //ableToType - allow the user to type text
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
