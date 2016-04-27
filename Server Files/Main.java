import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;
import java.io.*;
import java.net.*;

public class Main extends JFrame {
   //Global Variables
   //GUI Attributes
   private JTextField jtfpassword;
   private JTextField jtfstartTime;
   private JTextField jtfendTime;
   private JTextField jtfprofName;
   private JTextField jtfnameOfFile;
   
   //Field Attributes
   private String password;  //Password Setter
   private String profName;   //Prof name
   private String nameOfFile;   //Name of file to be submitted
   private long startTime;   //Set timer on Server side
   private long endTime;
   
   //Network Attributes
   private final int PORT_NUMBER = 16789;
   private static final long serialVersionUID = -1624623784600240948L;
   private Main.Server server = null;
   private ServerGUI gui;

   public Main(){
      //Main JPanel
      JPanel jpMain = new JPanel(new BorderLayout());
      
      JPanel jpStartMain = new JPanel(new GridLayout(5,2));
      
      JPanel jpStartButton = new JPanel(new FlowLayout());
      
      //All text areas and JLabels
      jpStartMain.add(new JLabel("    Professor Name:"));
      jtfprofName = new JTextField("", 10);
      jpStartMain.add(jtfprofName);
      
      jpStartMain.add(new JLabel("     Name Of Files:"));
      jtfnameOfFile = new JTextField("",10);
      jpStartMain.add(jtfnameOfFile);
      
      jpStartMain.add(new JLabel("     Start Time:"));
      jtfstartTime = new JTextField("",10);
      jpStartMain.add(jtfstartTime);
      
      jpStartMain.add(new JLabel("     End Time:"));
      jtfendTime = new JTextField("",10);
      jpStartMain.add(jtfendTime);
      
      jpStartMain.add(new JLabel("     Password:"));
      jtfpassword = new JTextField("",10);
      jpStartMain.add(jtfpassword);
      
      JButton jbStart = new JButton("Start Exam");
      
      jpMain.add(jpStartMain, BorderLayout.NORTH);
      jpMain.add(jbStart, BorderLayout.SOUTH);
      
      add(jpMain);
      setLocationRelativeTo(null);
      pack();
      setVisible(true);
      
      //Anonymous inner class for start button
      jbStart.addActionListener( 
         new ActionListener(){
            public void actionPerformed(ActionEvent ae){
            //Read in values from GUI
               
               profName = jtfprofName.getText();
               nameOfFile = jtfnameOfFile.getText();
               startTime = Long.parseLong(jtfstartTime.getText());
               endTime = Long.parseLong(jtfendTime.getText());
               password = jtfpassword.getText();
               setVisible(false);

               System.out.println("" + startTime);
               System.out.println("" + endTime);
                                             
               //start server communication
               server = new Main.Server();
               server.start();
               //start gui
               gui = new ServerGUI(server);
               Thread th = new Thread(gui);
               th.start();
            }
         });
         //key listener to hit enter and start exam
         jbStart.addKeyListener(
         new KeyAdapter(){
            public void keyPressed(KeyEvent ke)
            {               
               if(ke.getKeyCode() == KeyEvent.VK_ENTER)
               {
                  jbStart.doClick();
               }   
            }
         });
         //window listener to close window
         addWindowListener(
         new WindowAdapter(){
            public void windowClosing(ActionEvent ae)
            {
               System.exit(0);
            
            }
         });
   }
   
   class Server extends Thread{
   
      public static final int PORT = 16789;
      public Vector<ThreadedClient> clients = new Vector<ThreadedClient>();
      private Hashtable<String,ThreadedClient> clientNames = new Hashtable<String,ThreadedClient>();
   
      public Server(){
      
      }
      
      public ThreadedClient getClientName(String _name){
         ThreadedClient client = clientNames.get(_name);
         return client;
      }
   
      public void run(){      
         try{
         
            ServerSocket ss = new ServerSocket(PORT);
         
            Socket s = null;
         
            while(true){
               System.out.println("Waiting for a client...");
            
               s = ss.accept();
            
               clients.add(new ThreadedClient(s));
               clients.get(clients.size() - 1).start();
            
            }
         
         }
         catch(IOException ioe){
            ioe.printStackTrace();
         }
      }
   
      
   
      class ThreadedClient extends Thread{
      
         private Socket s = null;
         private ObjectInputStream in = null;
         private ObjectOutputStream out = null;
         private String name = null;
         private JTextArea receiveText;
      
      
         public ThreadedClient(Socket _s){
            s = _s;
         
            receiveText = new JTextArea(10,30);  
            
            try{
               in = new ObjectInputStream(s.getInputStream());
               out = new ObjectOutputStream(s.getOutputStream());
            }
            catch(IOException ioe){
               ioe.printStackTrace();
            }
         }
      
         public void run(){
            try{
               name = (String)in.readObject();
            
               System.out.println(name + " has joined the chat.");
               
               synchronized(clients)
               {
                  gui.addClient(name,receiveText);
                  clientNames.put(name,this);
               }
               sendOut("");
               sendOut(name + " has joined the chat.");
            
            }
            catch(IOException ioe){
               ioe.printStackTrace();
            }
            catch(ClassNotFoundException cnf){
               cnf.printStackTrace();
            }
         
            while(true){
               Object obj = null;
               try {
                  obj = in.readObject();
               } 
               catch (IOException e) {
                  System.out.println("IO Exception encountered! " + e.getMessage());
                  break;
               } 
               catch (ClassNotFoundException e) {
                  System.out.println("Class Not Found Exception encountered! " + e.getMessage());
                  break;
               }
            
               if (obj instanceof File) {
               //Create File object and then read in File object from client
                  File file = null;
                  file = (File)obj;
               
               } 
               else if (obj instanceof String) {
               
                  String msg = null;
               
                  msg = (String)obj;
                                 
                  if(msg.equalsIgnoreCase("quit"))
                  {
                     
                     sendOut(name + " has left the chat");
                     sendOut("");
                     break;
                  }
               
                  sendOut(name + ":");
                  sendOut(msg);
                  
                  System.out.println(name + ": " + msg);
                              
               } 
               else {
                  System.out.println("How did we get here?");
               }
            
            }
         
            System.out.println(name + " has disconected");
         
            try
            {
               in.close();
               out.close();
               s.close();
            }
            catch(IOException ioe)
            {
               return;
            }
            finally{
            synchronized(clients)
            {
               gui.removeClient(name,receiveText);
            }
            }
         }
      
         public void sendOut(String msg){
            try{
               out.writeObject(msg);
               out.flush();
               receiveText.append(msg + "\n");
            }
            catch(IOException ioe){
            
               ioe.printStackTrace();   
               return;
            }
         
            if(gui.isUserDisplayed(name)){
               gui.updateScreen();
            }
         }
      }
   }      
      
   public static void main(String [] args){
      new Main();
   }
}