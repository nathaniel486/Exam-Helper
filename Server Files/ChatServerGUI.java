import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.io.*;
import java.net.*;
import java.util.*;


public class ChatServerGUI extends JPanel
{
   private JFrame mainFrame;
   private JPanel chat;
   private JList<String> users;
   private JButton send;
   private JTextField sendText;
   public JTextArea receiveText;
   private JPanel textPanel;
   private String[] names;
   private Vector<String> connectedClients = new Vector<String>();
   private JScrollPane scrollPane;
   private String name = "Server";
   private String address = "localhost";
   private int port = 16789;
   private Socket s = null;
   private ObjectOutputStream  out = null;
   private ObjectInputStream in = null;
   private static final long serialVersionUID = 42L;
   private HashMap<String,JTextArea> userPanes;
   private String currentVisible = "Server";
   public JScrollPane scrollReceive = new JScrollPane(new JTextArea(10,30));


   public ChatServerGUI(HashMap<String,JTextArea> _userPanes)
   {
   
      userPanes = _userPanes;
   
      mainFrame = new JFrame();
      
      setLayout(new BorderLayout());
   
   
      users = new JList<String>();
      
      scrollPane = new JScrollPane();
      scrollPane.getViewport().add( users );
      add( scrollPane, BorderLayout.WEST );
         
      users.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      users.setSize(new Dimension(200,100));
      
      users.addListSelectionListener(
         new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent event)
            {
            
               userPanes.get(currentVisible).setVisible(false);
               userPanes.get(connectedClients.get(users.getSelectedIndex())).setVisible(true);
               currentVisible = connectedClients.get(users.getSelectedIndex());              
               mainFrame.pack();
               mainFrame.repaint();
               
            }
         });
   
      
   
      send = new JButton("Send");
      
      
      //send messages
      sendText = new JTextField(50);
      sendText.setBorder(new EtchedBorder());
      
      
      //receive messages    
      
      
      JPanel sendPanel = new JPanel(new BorderLayout());
      sendPanel.add(sendText,BorderLayout.WEST);
      sendPanel.add(send,BorderLayout.EAST);
      
      textPanel = new JPanel();
      textPanel.setLayout(new BorderLayout());
      
            
      textPanel.add(scrollReceive,BorderLayout.CENTER);
      textPanel.add(sendPanel,BorderLayout.SOUTH);
   
      add(textPanel,BorderLayout.CENTER);
      
      mainFrame.add(this);
      mainFrame.pack();
      mainFrame.setLocationRelativeTo(null);
      mainFrame.setVisible(true); 
      
      
      
      send.addActionListener(
         new ActionListener(){
            public void actionPerformed(ActionEvent ae)
            {
               try
               {
                  String msg = sendText.getText();
                  out.writeObject(msg);
                  out.flush();
                  sendText.setText(null);
               }
               catch(IOException ioe)
               {
                           
               }
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
         
      mainFrame.addWindowListener(
         new WindowAdapter(){
            public void windowClosing(WindowEvent we){
               System.out.println("Closing the Client...");
               try
               {
                  out.writeObject("quit");
                  out.flush();
                  sendText.setText(null);
                  System.exit(0);
               }
               catch(IOException ioe)
               {
                           
               }
                           	
            }	
         });    
   }
   
   
   public void startClient()
   {
      try
      {
      
         s = new Socket(address,port);
         out = new ObjectOutputStream(s.getOutputStream());
         out.flush();
         in = new ObjectInputStream(s.getInputStream());
         //(new Reading()).start();
      
         
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
   
   public void addClient(String _name)
   {
      
      connectedClients.add(_name);
   	
      users.setListData( connectedClients );
      scrollPane.revalidate();
      scrollPane.repaint();
   
   }
   
   public void removeClient(String _name)
   {
      
      connectedClients.remove(_name);
   	
      users.setListData( connectedClients );
      scrollPane.revalidate();
      scrollPane.repaint();
   
   }
   
   
   
//    class Reading extends Thread
//    {
//       public void run()
//       {
//          Object ob = null;
//          while(true)
//          {
//             try
//             {
//                ob = in.readObject();
//             
//             
//                if(ob instanceof String)
//                {
//                   String msg = (String)in.readObject();
//                   String inName = (String)ob;
//                   receiveText.append(inName + "\n");
//                   receiveText.append(msg + "\n");
//                   receiveText.setCaretPosition(receiveText.getDocument().getLength());
//                
//                }
//             }
//             catch(IOException ioe)
//             {
//             
//             }
//             catch(ClassNotFoundException cnf)
//             {
//                break;
//             }
//          }
//       }
//    }
}