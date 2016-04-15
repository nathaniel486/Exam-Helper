import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
/**
*
*/

public class Client extends JFrame implements ActionListener {
   //GUI Attributes
   private JTextField sendText;
   private JTextArea receiveText;
   private JPanel 	textPanel;
   private JButton 	send;
   private JButton 	exit;
   private JButton   retry;
   private JPanel 	jpButtonPanel;
   private JPanel    connection;

   //Network Attributes
   private Socket    s = null;
   private BufferedReader in = null;
   private PrintWriter out = null;
   private JTextField userName,address,port;
   private JButton   connect, disconnect;
   private String name;
   private boolean isConnected = false;
   /**
*
*/
   public static void main(String [] args) 
   {
      new Client();
   }  // end main
   /**
*
*/
   public Client()
   {
      setTitle("Chat with Friends!");
      //get the server information
      connection = new JPanel(new FlowLayout());
      
      userName = new JTextField("Annonymous",10);
      address = new JTextField(10);
      port = new JTextField(10);
      
      connect = new JButton("Connect");
      
      connection.add(new JLabel("Name:"));
      connection.add(userName);
      connection.add(new JLabel("Server Address:"));
      connection.add(address);
      connection.add(new JLabel("Server Port:"));
      connection.add(port);
      connection.add(connect);
      
      //send messages
      sendText = new JTextField();
      sendText.setBorder(new EtchedBorder());
      
      
      //receive messages      
      receiveText = new JTextArea(10,30);
      receiveText.setBorder(new EtchedBorder());
      receiveText.setLineWrap(true);
      receiveText.setWrapStyleWord(true);
      receiveText.setEditable(false);
      
      JPanel sendTextPanel = new JPanel(new FlowLayout());
      
      //sendTextPanel.add(new JLabel(">>"));
     //sendTextPanel.add(sendText);
      //setSize(new Dimension(200,200));
      
      
      textPanel = new JPanel();
      textPanel.setLayout(new BorderLayout());
      
      JScrollPane scrollReceive = new JScrollPane(receiveText);
      
      textPanel.add(scrollReceive,BorderLayout.CENTER);
      
      
      
      textPanel.add(sendText,BorderLayout.SOUTH);
      
      //add the button to send and exit
      send = new JButton("Send");
      disconnect = new JButton("Disconnect");
      exit = new JButton("Exit");
      retry = new JButton("Retry Connection");
      jpButtonPanel = new JPanel();
      jpButtonPanel.add(send);
      jpButtonPanel.add(exit);
      
      
      send.setEnabled(false);
      //add action listeners
      retry.addActionListener(this);
      exit.addActionListener(this);
      send.addActionListener(this);
      connect.addActionListener(this);
      disconnect.addActionListener(this);
      //make so the enter key sends
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
            public void windowClosing(WindowEvent we){
               
               exit();	
            }	
         });
      
      add(connection,BorderLayout.NORTH);
      add(jpButtonPanel,BorderLayout.SOUTH);
      add(textPanel,  BorderLayout.CENTER);
      
      pack();
      setLocationRelativeTo(null);
      setVisible(true);   
   }//end constructor
      /**
*
*/
   public void actionPerformed(ActionEvent ae)
   {
      Object choice = ae.getSource();
         
      if(choice == connect)
         connect();
      if(choice == send)
         send();
      if(choice == exit)
         exit();
      if(choice == retry)
         retry();
      if(choice == disconnect)
         disconnect();   
   }
      /**
*
*/
   public boolean connection(String _address, int _port)
   {
      boolean canConnect = false;
      try 
      {
         s = new Socket(_address,_port);
         in = new BufferedReader(new InputStreamReader(s.getInputStream()));
         out = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
         jpButtonPanel.remove(retry);
         canConnect = true;
         isConnected= true;
         write(name);
         Reading read = new Reading();
         read.start();
         send.setEnabled(true);
         userName.setEditable(false);
         address.setEditable(false);
         port.setEditable(false);
         sendText.requestFocus(true);
         connect.setEnabled(false);
         jpButtonPanel.add(disconnect);
         repaint();
         pack();
      }
      catch(UnknownHostException uhe) {
         receiveText.setText("Unable to connect to host.");
      }
      catch(IOException ie) {
         receiveText.append("Cannot Connect to server.  Try again later.\n");
         jpButtonPanel.add(retry);
         repaint();
         pack();
         send.setEnabled(false);
      }
         
      return canConnect;
   }
      /**
*
*/
   public void connect()
   {
      
      String host = address.getText();
      String portText = port.getText();
      int portNum;
      try
      {
         portNum = Integer.parseInt(portText);
      }
      catch(NumberFormatException nfe)
      {
         port.setText(null);
         receiveText.append("Please end a valid port number\n");
         return;
      }
         
      name = userName.getText();
      if(name.equals(""))
      {
         name = "Annonymous";
      }
         
      if(host.equals("") || portText.equals(null))
      {
         receiveText.append("Please specify a server address and port number\n");
         return;
      }
         
         
      if(connection(host,portNum))
      {
         jpButtonPanel.remove(retry);
         repaint();
         pack();
         send.setEnabled(true);
      }
   }
      /**
*
*/
   public void send()
   {
      String temp = sendText.getText();
      if(!temp.equals(""))
      {
         write(temp);
         sendText.setText(null);
      }
   }
      /**
*
*/
   public void exit()
   {
      
      disconnect();
      System.exit(0);
   }
      /**
*
*/
   public void retry()
   {
      String host = address.getText();
      int portNum = Integer.parseInt(port.getText());
      if(connection(host,portNum))
      {
         jpButtonPanel.remove(retry);
         repaint();
         pack();
         send.setEnabled(true);
      }
         
   }
   /**
*
*/
   public void disconnect()
   {
      send.setEnabled(false);
      userName.setEditable(true);
      address.setEditable(true);
      port.setEditable(true);
      connect.setEnabled(true);
      jpButtonPanel.remove(disconnect);
      repaint();
      pack();
         
      receiveText.append("Connection Disconnected\n");
         
      if(send.isEnabled())
      {
         write("quit");
      }
      if(isConnected)
      {
         try
         {
            in.close();
            out.close();
            s.close();
            isConnected = false;
         }
         catch(IOException ioe)
         {
            ioe.printStackTrace();
         }
      }
   }
      
   public void write(String temp)
   {
      out.println(temp);
      out.flush();
   }
      
   class Reading extends Thread
   {
      public void run()
      {
         String displayMsg = null;
         receiveText.append(name + " has join the chat\n");
         while(true)
         {
            String msg = null;
            String name = null;
            try
            {
               name = in.readLine();
               msg = in.readLine();
               displayMsg = String.format("%s:\n%s\n\n",name,msg);
               
               receiveText.append(displayMsg);
               receiveText.setCaretPosition(receiveText.getDocument().getLength());
               
               
            }
            catch(IOException ioe)
            {
               receiveText.append("Server:\n");
               receiveText.append("Server disconnected.  Communication terminated.\n");
               break;
            }
         
         }
      }
   }
}