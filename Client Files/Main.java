import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;

/*
   Main Client Class to run all files and set up the client, asks for your rit username,
   IP adress that you wish to connect to, first name, last name, and the password specified
   by the professor then opens up our drag and drop and chat ready for the practical to start.
*/
public class Main extends JFrame{
  //Global Attributes
   //GUI Attributes
   private JTextField jtfRitName;
   private JTextField jtfIp;
   private JTextField jtfFirstName;
   private JTextField jtfLastName;
   private JTextField jtfPassword;
   
   //Field Attributes
   private String ritName;   //RIT Username of Client
   private String ip;        //IP Address of Client
   private String firstName; //First name of Client
   private String lastName;  //Last name of Client 
   private String fullName;  //Full name of Client
   private String password;  //Password 
   
   //Network Attributes
   private final int PORT_NUMBER = 16789;
   private static final long serialVersionUID = 42L;
   
   private Main main;
   
   //Timer Attributes
   private SimpleDateFormat sdf;
   private Date date;
   
   /**
   * Creating and building GUI
   */
   public Main() {
   
      main = this;
   
      JPanel jpMain = new JPanel(new BorderLayout()); //Main JPanel for the Beginning GUI where client inputs credentials
      
      JPanel jpStartMain = new JPanel(new GridLayout(5,2)); //JPanel for each textfeild 
      
      JPanel jpStartButton = new JPanel(new FlowLayout());
   
      //Labels and textfeilds on welcome panel   
      jpStartMain.add(new JLabel("   RIT Username:"));
      jtfRitName = new JTextField(10);
      jpStartMain.add(jtfRitName);
         
      jpStartMain.add(new JLabel("   IP Adress:"));
      jtfIp = new JTextField(10);
      jpStartMain.add(jtfIp);
   
      jpStartMain.add(new JLabel("   First Name:"));
      jtfFirstName = new JTextField(10);
      jpStartMain.add(jtfFirstName);
   
      jpStartMain.add(new JLabel("   Last Name:"));
      jtfLastName = new JTextField(10);
      jpStartMain.add(jtfLastName);
      
      jpStartMain.add(new JLabel("   Password:"));
      jtfPassword = new JTextField(10);
      jpStartMain.add(jtfPassword);
   
      JButton jbStart = new JButton("Start Exam");
      jpStartButton.add(jbStart);
   
         
      jpMain.add(jpStartMain, BorderLayout.NORTH);
      jpMain.add(jbStart, BorderLayout.SOUTH);
          
      add(jpMain);
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      setLocationRelativeTo(null);
      pack();
      setVisible(true);
      setResizable(false);
      
      //Anonymous inner class for the start button to start the SubmissionGUI
      jbStart.addActionListener( 
         new ActionListener(){
            public void actionPerformed(ActionEvent ae) {
            //Read in values from GUI
            
               ritName = jtfRitName.getText();
               ip = jtfIp.getText();
               firstName = jtfFirstName.getText();
               lastName = jtfLastName.getText();
               fullName = firstName + " " + lastName;
               password = jtfPassword.getText();
            
               //All validation for start screen
               if(ritName.equals("")){
                  JOptionPane.showMessageDialog(null,"Please enter your RIT userName.");
                  return;
               }
               else if(ip.equals("")){
                  JOptionPane.showMessageDialog(null,"Please enter the IP address of the server you wish to connect to.");
               }
               else if(firstName.equals("")){
                  JOptionPane.showMessageDialog(null,"Please enter your first name.");
               }  
               else if(lastName.equals("")){
                  JOptionPane.showMessageDialog(null,"Please enter your last name.");
               }
               else{
                  new SubmissionGUI();
               }
            
               
            }
         });
   }
   
  /**
   * Submission GUI will be the main GUI where we have the file transfer
   */ 
   class SubmissionGUI extends JFrame{      
      private Socket s = null;
      private ObjectOutputStream  out = null;
      private ObjectInputStream in = null;
      
      private ChatClient chatPanel;
      private JTextArea receiveText;
      
      private FileSubmitter fileSubmitPanel;
      
      private static final long serialVersionUID = 42L;
      
      public SubmissionGUI() {
        //Networking setup
         //Create socket and input/output streams
         try {
            s = new Socket(ip, PORT_NUMBER);
            out = new ObjectOutputStream(s.getOutputStream());
            out.flush();
            in = new ObjectInputStream(s.getInputStream());
            
            receiveText = new JTextArea(10,30);
            chatPanel = new ChatClient(out,fullName,receiveText);
            
            fileSubmitPanel = new FileSubmitter(out);
            
            main.setVisible(false);
            
            (new Reading()).start();
         } 
         catch (UnknownHostException e) {
            System.out.println("Unknown Host Exception! " + e.getMessage());
            JOptionPane.showMessageDialog(null,"Cannot connect to server at specified address.  Please try again later.");
            return;
         } 
         catch (IOException e) {
            System.out.println("IO Exception! " + e.getMessage());
            JOptionPane.showMessageDialog(null,"Cannot connect to server at specified address.  Please try again later.");
            return;
         }
         
        
        //Create main GUI
         //Creation of top menu bar
         JPanel jpMain = new JPanel(new BorderLayout());
         add(jpMain);
         
         JMenuBar jmbar = new JMenuBar();
         JMenu jmFile = new JMenu("File");
         JMenuItem jmiFileQuit = new JMenuItem("Quit");
         jmiFileQuit.addActionListener(
            new ActionListener(){
               public void actionPerformed(ActionEvent a){
                  try{
                     System.exit(0);
                  }
                  catch(Exception e){
                     System.out.println(e);
                  }
               }
            });
         JMenuItem jmiFileLogout = new JMenuItem("Logout");
         jmFile.add(jmiFileQuit);
         jmFile.add(jmiFileLogout); 
         jmbar.add(jmFile);
            
         JMenu jmEdit = new JMenu("Edit");
         JMenuItem jmiEditPort = new JMenuItem("Change Port");
         jmEdit.add(jmiEditPort);
         jmbar.add(jmEdit);
         setJMenuBar(jmbar);
         
         //Panel for chat   
         jpMain.add(chatPanel, BorderLayout.EAST);
      
         //Panel for FileTransfer
         jpMain.add(fileSubmitPanel, BorderLayout.WEST); 
         
         //Panel for Timer
         Clock time = new Clock();
         jpMain.add(time, BorderLayout.NORTH);
         Thread th = new Thread(time);
         th.start();
                  
         setVisible(true);
         setDefaultCloseOperation( EXIT_ON_CLOSE );
         pack();
         setResizable(false);
         setLocationRelativeTo(null);
         
         addWindowListener( 
            new WindowAdapter() {
               public void windowClosing(WindowEvent we){
                  System.out.println("Closing the Client...");
                  try {
                     out.writeObject("quit");
                     out.flush();
                     System.exit(0);
                  } 
                  catch(IOException e)  {
                     System.out.println("Error closing the client. IO Exception " + e.getMessage());
                  }	
               }	
            });
      }
      /*
      *Timer Class for top timer so you know how much time is left
      */
      class Clock extends JLabel implements Runnable{
         public void run()
         {
            int delay = 1000;
         
            new javax.swing.Timer(delay, 
               new ActionListener(){
                  public void actionPerformed(ActionEvent ae)
                  {
                     setText(sdf.format(date));
                     Font font = new Font("Arial", Font.BOLD, 30);
                     setFont(font);
                     
                  }
               }).start();
         
         }
      }
     /**
      * class which handles the reading in of objects from the server. Depending on what type of object
      * is read in from the server, different things happen.
      */
      class Reading extends Thread {
         
         public void run() {
            Object obj = null;
            
            //Always ready to read in from server
            while(true) {
               try {
                  obj = in.readObject();   //Generic object to start with for reading in
               
                  if(obj instanceof String) {
                   //If object is of type String, it is a chat message and should handled by the ChatClient
                     String msg = (String)in.readObject();
                     String inName = (String)obj;
                     receiveText.append(inName + "\n");
                     receiveText.append(msg + "\n");
                     receiveText.setCaretPosition(receiveText.getDocument().getLength());
                  
                  }
                  else if (obj instanceof File){
                     File logText = (File)obj;
                     
                     fileSubmitPanel.readLog(logText);
                  }
                  else if (obj instanceof Long){
                     long cur = System.currentTimeMillis();
                     long timer = (long)obj - cur;
                     date = new Date(timer);
                     sdf = new SimpleDateFormat("HH:mm:ss");
                  }
                  else {
                     System.out.println("How did we get here?");
                  }
               }//End try 
               catch(IOException e){
                  System.out.println("Could not read in from server. IO Exception " + e.getMessage());
                  break;
               }
               catch(ClassNotFoundException e){
                  System.out.println("Could not understand the type of Object read in from server. Class Not Found Exception " + e.getMessage());
                  break;
                  //break;
               }
            }
         }
      }
   }//End Submission
   
   
  /**
   Main method of client program. Starts client GUI
   */
   public static void main(String[]args){
      new Main();
   }
}