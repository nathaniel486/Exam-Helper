import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.net.*;

public class MainServer extends JFrame {
   //Global Variables
   //GUI Attributes
   private JTextField jtfpassword;
   private JTextField jtfminute;
   private JTextField jtfhour;
   private JComboBox<String> jcbtimeOfDay;
   private JTextField jtfprofName;
   private JTextField jtfsaveDir;
   
   //Field Attributes
   private String password;   //Password Setter
   private String profName;   //Prof name
   private String endTime;      //Set end time on Server side
   private File saveDir;      //Directory where all of the student submissions will be saved
   
   //Network Attributes
   private final int PORT_NUMBER = 16789;
   private static final long serialVersionUID = -1624623784600240948L;
   private MainServer.Server server = null;
   private ServerGUI gui;

   public MainServer(){
      //Main JPanel
      JPanel jpMain = new JPanel(new BorderLayout());
      
      JPanel jpStartMain = new JPanel(new GridLayout(4,2));
      
      JPanel jpStartButton = new JPanel(new FlowLayout());
      
      //All text areas and JLabels
      jpStartMain.add(new JLabel("    Professor Name:"));
      jtfprofName = new JTextField("", 10);
      jpStartMain.add(jtfprofName);
   
      
      JButton jbChooseSaveDir = new JButton("Choose Save Location");
      jpStartMain.add(jbChooseSaveDir);
      jtfsaveDir = new JTextField("",10);
      jpStartMain.add(jtfsaveDir);
      jtfsaveDir.setEnabled(false);
      
      jpStartMain.add(new JLabel("     End Time:"));
      JPanel time = new JPanel(new FlowLayout());
      jtfhour = new JTextField(10);
      jtfminute = new JTextField(10);
      String[] times = {"AM","PM"};
      jcbtimeOfDay = new JComboBox<String>(times);
      time.add(jtfhour);
      time.add(jtfminute);
      time.add(jcbtimeOfDay);
      jpStartMain.add(time);
   
      
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
      //setResizable(false);
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      
      //Anonymous inner class for save directory button
      jbChooseSaveDir.addActionListener( 
         new ActionListener(){
            public void actionPerformed(ActionEvent ae){
            //Prompt user for a locaiton to save submitted files
               JFileChooser chooser = new JFileChooser(new File("~/").getAbsolutePath()); //Open FileChooser in current directory
               chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);   //Set the Chooser to only allow the user to select directories
               chooser.setAcceptAllFileFilterUsed(false);
               int returnVal = chooser.showDialog(null, "Save Submitted Files Here");   //Returns the CONSTANT value for whether the choice was Approve, Cancel, or an error
               saveDir = chooser.getSelectedFile();
            //If a file is successfully opened
               if (saveDir != null && returnVal == JFileChooser.APPROVE_OPTION) {
                  jtfsaveDir.setText(saveDir.getName());
               }
            }
         });
      
      //Anonymous inner class for start button
      jbStart.addActionListener( 
         new ActionListener(){
            public void actionPerformed(ActionEvent ae){
            //Read in values from GUI
               
               profName = jtfprofName.getText();
               //endTime = jtfendTime.getText();
               password = jtfpassword.getText();
               setVisible(false);
               
               if(profName.equals("")){
                  profName = "Professor";   
               }
               
                                             
               //start server communication
                  server = new MainServer.Server();
                  server.start();
               //start gui
                  gui = new ServerGUI(server,profName);
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
   
  /*
   * Running server code for the ImDone server
   */
   class Server extends Thread{
   
      public static final int PORT = 16789;
      public Vector<ThreadedClient> clients = new Vector<ThreadedClient>();
      private Hashtable<String,ThreadedClient> clientNames = new Hashtable<String,ThreadedClient>();
      
      public ThreadedClient getClientName(String _name) {
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
         private boolean firstTime = true;
      
      
         public ThreadedClient(Socket _s){
            s = _s;
         
            receiveText = new JTextArea(10,30);  
            
            try {
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
               sendOut("",name + " has joined the chat.");
            
            }
            catch(IOException ioe){
               ioe.printStackTrace();
            }
            catch(ClassNotFoundException cnf){
               cnf.printStackTrace();
            }
         
            //Reading in to server
            while(true) {
               
               Object obj = null;
               
               try {
                  obj = in.readObject();
               } 
               catch (IOException e) {
                  System.out.println("Error reading in object to server. IO Exception! " + e.getMessage());
                  break;
               } 
               catch (ClassNotFoundException e) {
                  System.out.println("Object read into server not recognized. Class Not Found Exception! " + e.getMessage());
                  break;
               }
               
               //If Object read into server is a File
               if (obj instanceof byte[]) {
               //Read in File object from client
                  byte[] submittedFileBytes = (byte[])obj;
                  
                  //Make a directory for the submitting client
                  File studentDir = new File(saveDir, name);
                  studentDir.mkdirs();
                  
                  System.out.println(studentDir.getPath());
                  File logText = new File(studentDir, "logFile.txt");
                  
                  //Save the submitted file to the created directory
                  try {
                     // read all data from the submitted file and save it to the studentDir
                     File submittedFile = new File(studentDir, "Submission.zip");
                     Files.write(submittedFile.toPath(), submittedFileBytes);
                             
                     System.out.println("Saved out file");
                     
                     //Write to log file message of successful file submission
                     BufferedWriter bw = new BufferedWriter(new FileWriter(logText));
                     PrintWriter pw = new PrintWriter(bw);
                     
                     pw.println("Your code has been successfully submitted!");
                     bw.flush();
                     bw.close();
                     
                     out.writeObject(logText);
                     
                  } 
                  catch (IOException e) {
                     System.out.println("Error writing log file. IO Exception " + e.getMessage() );
                  }
               }
                //If Object read into server is a String, it is a Chat message
               else if (obj instanceof String) {
               
                  firstTime = false;
               
                  String msg = (String)obj;
                                 
                  if(msg.equalsIgnoreCase("quit")) {
                     sendOut(name + " has left the chat","");
                     
                     break;
                  }
                  sendOut(name + ":",msg);
                  
                  System.out.println(name + ": " + msg);
                 
               //If Object read into server is not a String or a File, welp, now isn't that strange...             
               } 
               else {
                  System.out.println("How did we get here?");
               }
            
            }
         
            System.out.println(name + " has disconected");
         
            try {
               in.close();
               out.close();
               s.close();
            } 
            catch(IOException ioe) {
               System.out.println("Error closing input, output, and socket connections. IO Exception! " + ioe.getMessage());
               return;
            } 
            finally{
               synchronized(clients) {
                  gui.removeClient(name,receiveText);
               }
            }
         }
         
         public boolean isFirst(){
            return firstTime;
         }
      
         public void sendOut(String msg1,String msg2){
            try{
               out.writeObject(msg1);
               out.flush();
               receiveText.append(msg1 + "\n");
               
               out.writeObject(msg2);
               out.flush();
               receiveText.append(msg2 + "\n");
            } 
            catch(IOException ioe){
               System.out.println("Error sending message. IO Exception! " + ioe.getMessage());
               ioe.printStackTrace();   
               return;
            }
         
            if(gui.isUserDisplayed(name)){
               gui.updateScreen();
            }
            else{
               gui.addWaitingClient(name);
            }
         }
      }
   }      
      
   public static void main(String [] args){
      new MainServer();
   }
}