import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class ServerGUI extends JFrame implements Runnable{

   private JPanel jpMain;
   private JList<String> users;
   private JButton send;
   private JTextField sendText;
   private JTextArea receiveText;
   private JPanel textPanel;
   private JScrollPane scrollPane,scrollReceive;
 
   private Vector<String> connectedClients = new Vector<String>();
   private Vector<String> unAnsweredClients = new Vector<String>();
   private Hashtable<String, JTextArea> userPanes = new Hashtable<String,JTextArea>();
   private String currentDisplayed = null;
   private boolean adding = false;
   private MainServer.Server comm;
 
   private String name;
   private String address = "localhost";
   private int port = 16789;
   private Socket s = null;
   private ObjectOutputStream  out = null;
   private ObjectInputStream in = null;
   private static final long serialVersionUID = 42L;
  
  public ServerGUI(MainServer.Server _comm,String _name){
      name = _name;
      comm = _comm;
      
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
         if(users.getSelectedValue().equals(name)){
            adding = false;
            return true;
         }
         else{
            //if not displayed add user to list of unanswered clients
            unAnsweredClients.add(name);
            connectedClients.remove(name);
            connectedClients.add(0,name);
            users.setListData( connectedClients );
            scrollPane.revalidate();
            scrollPane.repaint();
                           
            adding = false;
            return false;      
         }
      }
      else{
         //if nothing is displayed add user to list of unanswered clients
         unAnsweredClients.add(name);
         connectedClients.remove(name);
         connectedClients.add(0,name);
         users.setListData( connectedClients );
         scrollPane.revalidate();
         scrollPane.repaint();
         adding = false;
         return false;
      }
      
   }
   /**
   update the screen with a new chat message
   */
   public void updateScreen(){
      JTextArea work = userPanes.get(users.getSelectedValue());
      receiveText.setText(work.getText());
      userPanes.put(users.getSelectedValue(),work);
      receiveText.setCaretPosition(receiveText.getDocument().getLength());
      
      jpMain.repaint();
      
      unAnsweredClients.remove(users.getSelectedValue());
      
   }
   
   /**
   Add a client to the list of connected clients
   */
   public void addClient(String _name,JTextArea userArea){
      adding = true;
      users.clearSelection();
      connectedClients.add(_name);
      userPanes.put(_name,userArea);
      scrollReceive.add(userArea);
   	
      
      users.setListData( connectedClients );
      scrollPane.revalidate();
      scrollPane.repaint();
      adding = false;
   }
   /**
   Remove a client from list of connected clients
   */
   public void removeClient(String _name,JTextArea userArea){
      adding = true;
      users.clearSelection();
      connectedClients.remove(_name);
      userPanes.remove(_name,userArea);
      scrollReceive.remove(userArea);
      
      
      users.setListData( connectedClients );
      scrollPane.revalidate();
      scrollPane.repaint();
      adding = false;
   }
   /**
   send message to particular client
   */
   public void sendOut(String msg){
      if(!users.isSelectionEmpty()){   
         if(users.getSelectedValue().equals(name)){
            sendAll(msg);
         }
         else{
            comm.getClientName(users.getSelectedValue()).sendOut(name + ":");
            comm.getClientName(users.getSelectedValue()).sendOut(msg);
         }
      }
   }
   public void sendAll(String msg){
      for(int i = 0;i < comm.clients.size();i++){
               comm.clients.get(i).sendOut(name + ":");
               comm.clients.get(i).sendOut(msg);
            }
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
         out.writeObject(name);
         out.flush();
      }
      catch(IOException ioe)
      {
         ioe.printStackTrace();
      }
   }

}