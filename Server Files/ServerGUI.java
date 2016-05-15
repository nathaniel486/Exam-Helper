import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
   This class handles the communication via chat between teh professor and the student.  
   each student has a private chat witht the professor.
   The professor also has the capability to send a broadcast message to every client.
   @author Nathaniel Larrimore
   Team 2: Team Nineteen
*/

public class ServerGUI extends JFrame implements Runnable{

   private JPanel jpMain;/**JPanel that holds eveything*/
   private JList<String> users;/**JList to display the users that are connected*/
   private JButton send;/**button to send the text*/
   private JTextField sendText;/**Field to hold the text you wish to send*/
   private JTextArea receiveText;/**Field to hold the all previously sent messages*/
   private JPanel textPanel;/**JPanel to hold the sendText and send button*/
   private JScrollPane scrollPane,scrollReceive;/** */
 
   private Vector<String> connectedClients = new Vector<String>();/**Holds all the names of the connected clients*/
   private Vector<String> unAnsweredClients = new Vector<String>();/**holds all the names of clients waiting for a response*/
   private Hashtable<String, JTextArea> userPanes = new Hashtable<String,JTextArea>();/**Holds the name of the client, and their particular chat history*/
   private String currentDisplayed = null;/**Is the user sending a message displayed*/
   private boolean adding = false;/**used for making a change to the JList*/
   private boolean sending = false;/**used for the server sending a message*/
   private MainServer.Server comm;/**the comunication portion of the server*/
 
   private String name;/**name of the professor*/
   private String pass;/**Password for the server*/
   private String address = "localhost";/**address to connect to*/
   private int port = 16789;/**port to bind on*/
   private Socket s = null;/**socket to send over*/
   private ObjectOutputStream  out = null;/**stream used to send*/
   private ObjectInputStream in = null;/**stream used to recieve*/
   private static final long serialVersionUID = 42L;
  
   public ServerGUI(MainServer.Server _comm,String _name,String pass){
      name = _name;
      comm = _comm;
      this.pass = pass;
      
      jpMain = new JPanel();
      jpMain.setLayout(new BorderLayout());   
      
      //list of connected users      
      users = new JList<String>();
      
      scrollPane = new JScrollPane();
      scrollPane.getViewport().add(users);
      jpMain.add(scrollPane, BorderLayout.WEST );
      
      users.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      users.setSize(new Dimension(200,100));
      
      //selection listener for JList
      users.addListSelectionListener(
         new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent event)
            {
               if(!adding){
                  updateScreen();
               
               }
            }
         });
   
      //send button
      send = new JButton("Send");
      
      //send messages
      sendText = new JTextField(40);
      sendText.setBorder(new EtchedBorder());
      
      //receive messages    
      receiveText = new JTextArea(10,30);  
      receiveText.setBorder(new EtchedBorder());
      receiveText.setLineWrap(true);
      receiveText.setWrapStyleWord(true);
      receiveText.setEditable(false);
      scrollReceive = new JScrollPane(receiveText);
      
      //jpanel for the sending portion of the GUI
      JPanel sendPanel = new JPanel(new BorderLayout());
      sendPanel.add(sendText,BorderLayout.WEST);
      sendPanel.add(send,BorderLayout.EAST);
      
      textPanel = new JPanel(new BorderLayout());
      textPanel.add(scrollReceive,BorderLayout.CENTER);
      textPanel.add(sendPanel,BorderLayout.SOUTH);
      
      jpMain.add(textPanel,BorderLayout.CENTER);
      //action listener for send button   
      send.addActionListener(
         new ActionListener(){
            public void actionPerformed(ActionEvent ae)
            {
               String msg = sendText.getText();
               sendOut(msg);
               sendText.setText(null);
            
            }
         });
      //key listener so when user hits enter the message is sent   
      sendText.addKeyListener(
         new KeyAdapter(){
            public void keyPressed(KeyEvent ke)
            {               
               if(ke.getKeyCode() == KeyEvent.VK_ENTER)
               {
                  send.doClick();
               }   
            }
         });
   
      addWindowListener(
         new WindowAdapter(){
            public void windowClosed(ActionEvent ae)
            {
               sendOut("quit");
               
               sendText.setText(null);
               disconnect();
               System.exit(0);
            
            }
         });
         
         
      add(jpMain);
      pack();
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      setLocationRelativeTo(null);
      setVisible(true);
   }
   /**
   check to see if a particular clinet's chat is being displayed at that moment
   @param name Name of client you wish to see is currently being displayed or not
   @return return true if user is displayed, return false if they aren't
   */
   public boolean isUserDisplayed(String name){
      adding = true;
      if(!users.isSelectionEmpty()){
         if(users.getSelectedValue().equals(name) || users.getSelectedValue().equals("*" + name)){
            adding = false;
            return true;
         }
         else{
         
            adding = false;
            return false;      
         }
      }
      else{
         
         adding = false;
         return false;
      }
      
   }
   
   public void addWaitingClient(String name){
      if(!sending && !comm.getClientName(name).isFirst()){
         adding = true;
         unAnsweredClients.add(name);
         connectedClients.remove(name);
         connectedClients.add(0,"*" + name);
         users.setListData( connectedClients );
         scrollPane.revalidate();
         scrollPane.repaint();
         adding = false;
      }
   }
   
   
   /**
   update the screen with a new chat message
   */
   public void updateScreen(){
      adding = true;
      String currentUser = users.getSelectedValue().substring(1);
      
      if(users.getSelectedValue().substring(0,1).equals("*")){
         JTextArea work = userPanes.get(currentUser);
         receiveText.setText(work.getText());
         userPanes.put(currentUser,work);
         receiveText.setCaretPosition(receiveText.getDocument().getLength());
         
         jpMain.repaint();
         
         unAnsweredClients.remove(users.getSelectedValue());
         
         unAnsweredClients.remove(currentUser);
         int location = connectedClients.indexOf(users.getSelectedValue());
         connectedClients.remove(users.getSelectedValue());
         connectedClients.add(location,currentUser);
         users.setListData( connectedClients );
         scrollPane.revalidate();
         scrollPane.repaint();
         users.setSelectedIndex(connectedClients.indexOf(currentUser));
         adding = false;
      }
      else{
         JTextArea work = userPanes.get(users.getSelectedValue());
         receiveText.setText(work.getText());
         userPanes.put(users.getSelectedValue(),work);
         receiveText.setCaretPosition(receiveText.getDocument().getLength());
         
         jpMain.repaint();
      }
      adding = false;
   }
   
   /**
   Add a client to the list of connected clients
   @param _name of the user to be added
   @param userArea JTextArea to hold the chat history for this client
   */
   public void addClient(String _name,JTextArea userArea){
      adding = true;
      connectedClients.add(_name);
      userPanes.put(_name,userArea);
   	
      
      users.setListData( connectedClients );
      scrollPane.revalidate();
      scrollPane.repaint();
      adding = false;
   }
   /**
   Remove a client from list of connected clients
   @param _name of the user to be removed
   @param userArea JTextArea to hold the chat history for this client
   */
   public void removeClient(String _name,JTextArea userArea){
      adding = true;
      connectedClients.remove(_name);
      userPanes.remove(_name,userArea);
      
      
      users.setListData( connectedClients );
      scrollPane.revalidate();
      scrollPane.repaint();
      adding = false;
   }
   /**
   send message to particular client
   @param msg String to be sent from the server
   */
   public void sendOut(String msg){
      if(!users.isSelectionEmpty()){   
         if(users.getSelectedValue().equals(name)){
            sendAll(msg);
         }
         else{
            comm.getClientName(users.getSelectedValue()).sendOut(name + ":",msg);
         }
      }
   }
   public void sendAll(String msg){
      sending = true;
      for(int i = 0;i < comm.clients.size();i++){
         comm.clients.get(i).sendOut(name + ":",msg);
      }
      sending = false;
   }
   
   public void disconnect(){
      try{
         in.close();
         out.close();
         s.close();
      }
      catch(IOException ioe){
         ioe.printStackTrace();
      }  
   }
   
   /**
   Start the communication portion of the GUI
   */
   public void run(){
      try
      {
      
         s = new Socket(address,port);
         out = new ObjectOutputStream(s.getOutputStream());
         out.flush();
         in = new ObjectInputStream(s.getInputStream());
      
         
      }
      catch(UnknownHostException uhe) 
      {
         System.out.println("Unable to connect to host.");
      }
      catch(IOException ie) 
      {
         System.out.println("Unable to connect to host.");
      }
      
      
         
      try
      {
         out.writeObject(pass);
         out.flush();
         in.readObject();
         out.writeObject(name);
         out.flush();
      }
      catch(IOException ioe)
      {
         ioe.printStackTrace();
      }
      catch(ClassNotFoundException cnf){
         cnf.printStackTrace();
      }
   }

}