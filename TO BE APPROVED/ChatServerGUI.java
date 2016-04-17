import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServerGUI extends JPanel implements Runnable {

   private JList<String> users;
   private JButton send;
   private JTextField sendText;
   private JTextArea receiveText;
   private JPanel textPanel;
   private JScrollPane scrollPane,scrollReceive;
   private Vector<String> connectedClients = new Vector<String>();
   private Vector<String> unAnsweredClients = new Vector<String>();
   
   private String name = "Server";
   private String address = "localhost";
   private int port = 16789;
   private Socket s = null;
   private ObjectOutputStream  out = null;
   private ObjectInputStream in = null;
   private static final long serialVersionUID = 42L;
  
   private Hashtable<String, JTextArea> userPanes = new Hashtable<String,JTextArea>();
   private String currentDisplayed = null;
   private boolean adding = false;
   private ChatServer test;


   public ChatServerGUI(ChatServer _test){
      test = _test;
      
      setLayout(new BorderLayout());
      
      users = new JList<String>();
      
      scrollPane = new JScrollPane();
      scrollPane.getViewport().add(users);
      add(scrollPane, BorderLayout.WEST );
         
      users.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      users.setSize(new Dimension(200,100));
      
      users.addListSelectionListener(
         new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent event)
            {
               if(!adding){
                  updateScreen();
               
               }
            }
         });
   
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
      
      JPanel sendPanel = new JPanel(new BorderLayout());
      sendPanel.add(sendText,BorderLayout.WEST);
      sendPanel.add(send,BorderLayout.EAST);
            
      add(scrollReceive,BorderLayout.CENTER);
      add(sendPanel,BorderLayout.SOUTH);
            
      send.addActionListener(
         new ActionListener(){
            public void actionPerformed(ActionEvent ae)
            {
               String msg = sendText.getText();
               sendOut(msg);
               sendText.setText(null);
            
            }
         });
         
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
            
   }
   
   public boolean isUserDisplayed(String name){
      adding = true;
      if(!users.isSelectionEmpty()){
         if(users.getSelectedValue().equals(name)){
            adding = false;
            return true;
         }
         else{
            
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
   
   public void updateScreen(){
      JTextArea work = userPanes.get(users.getSelectedValue());
      receiveText.setText(work.getText());
      userPanes.put(users.getSelectedValue(),work);
      receiveText.setCaretPosition(receiveText.getDocument().getLength());
      
      repaint();
      
      unAnsweredClients.remove(users.getSelectedValue());
      
   }
   
   
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
   
   public void sendOut(String msg){
      
      if(!users.isSelectionEmpty()){   
         if(users.getSelectedValue().equals(name)){
            for(int i = 0;i < test.clients.size();i++){
               test.clients.get(i).sendOut(name + ":");
               test.clients.get(i).sendOut(msg);
            }
         }
         else{
            test.getClientName(users.getSelectedValue()).sendOut(name + ":");
            test.getClientName(users.getSelectedValue()).sendOut(msg);
         }
      }
   }
   
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
         out.writeObject("Server");
         out.flush();
      }
      catch(IOException ioe)
      {
         ioe.printStackTrace();
      }
   }

}