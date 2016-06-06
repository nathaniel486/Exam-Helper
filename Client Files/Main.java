import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.text.*;

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
      
      //Password must match!
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
      
      private JPanel jpMain;
      
      private ChatClient chatPanel;
      private JTextArea receiveText;
      private JPanel time;
      private Clock clock;
      private CountDown remainingTime;
      private JLabel infinity = new JLabel("\u221E");
      private boolean ended = false;
      private Font headerFont = new Font(null,Font.BOLD + Font.ITALIC,24);
      private Font subFont = new Font(null,Font.BOLD + Font.ITALIC,18);
      private Color fontColor = new Color(0,0,255);
      
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
            
            out.writeObject(password);
            out.flush();
            
            String msg = (String)in.readObject();
            if(!msg.equals("OK")){
               JOptionPane.showMessageDialog(null,"Password is incorrect");
               return;
            }
            
            
            receiveText = new JTextArea(10,30);
            chatPanel = new ChatClient(out,fullName,receiveText);
            fileSubmitPanel = new FileSubmitter(out);
            main.setVisible(false);
            (new Reading()).start();
            
            clock = new Clock();
            Thread th = new Thread(clock);
            th.start();
            
            
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
         catch(ClassNotFoundException cnf){
            cnf.printStackTrace();
         }
         
        
        //Create main GUI
         //Creation of top menu bar
         jpMain = new JPanel(new BorderLayout());
         add(jpMain);
         
         JMenuBar jmbar = new JMenuBar();
         JMenu jmFile = new JMenu("File");
         JMenuItem jmiFileQuit = new JMenuItem("Quit");
         jmFile.add(jmiFileQuit);
         jmbar.add(jmFile);
            
         JMenu jmAbout = new JMenu("About");
         JMenuItem jmiEditAbout = new JMenuItem("About");
         jmAbout.add(jmiEditAbout);
         jmbar.add(jmAbout);
         setJMenuBar(jmbar);
         
         jmiFileQuit.addActionListener(
            new ActionListener(){
               public void actionPerformed(ActionEvent ae){
                  System.exit(0);
               }
            });
         
         jmiEditAbout.addActionListener(
            new ActionListener(){
               public void actionPerformed(ActionEvent ae){
                  JOptionPane.showMessageDialog(null,"Version: 1.0\nAuthors: Nathaniel Larrimore, Niki Genung, Brendon Strowe\nRelease: May 19, 2016\n\nThis program helps students submit their files during an exam and help facilitate communication between the professor and student(s).\nWhen you are done your exam, add your files to the file submission window, check your compilation, and submit.");
               }
            });
         
         //Panel for chat   
         jpMain.add(chatPanel, BorderLayout.EAST);
      
         //Panel for FileTransfer
         jpMain.add(fileSubmitPanel, BorderLayout.WEST); 
         
         //Clock for time
      
             
                  
         setVisible(true);
         setDefaultCloseOperation( EXIT_ON_CLOSE );
         pack();
         setLocationRelativeTo(null);
         
         addWindowListener( 
            new WindowAdapter() {
               public void windowClosing(WindowEvent we){
                  System.out.println("Closing the Client...");
                  if(!ended){
                     try {
                        out.writeObject("quit");
                        out.flush();
                        in.close();
                        out.close();
                        s.close();
                     } 
                     catch(IOException e)  {
                        System.out.println("Error closing the client. IO Exception " + e.getMessage());
                     }
                     finally{
                        System.exit(0);
                     }	
                  }
                  else{
                     System.exit(0);
                  }
               
               }	
            });
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
                     
                     String inName = (String)obj;
                     if(inName.equalsIgnoreCase("END")){
                        receiveText.append("Operator has ended the exam");
                        JOptionPane.showMessageDialog(null,"Operator has ended the exam");
                        break;
                     }
                     String msg = (String)in.readObject();
                     receiveText.append(inName + "\n");
                     receiveText.append(msg + "\n");
                     receiveText.setCaretPosition(receiveText.getDocument().getLength());
                  
                  }
                  else if (obj instanceof File){
                     File logText = (File)obj;
                     
                     fileSubmitPanel.readLog(logText);
                  }
                  else if(obj instanceof Long){
                     Long timer = (Long)obj;
                     time = new JPanel(new GridLayout(1,0));
                  
                     JLabel currTime = new JLabel("Current time:  ",JLabel.RIGHT);
                     JLabel timeLeft = new JLabel("Time left:  ",JLabel.RIGHT);
                     
                     currTime.setFont(subFont);
                     currTime.setForeground(fontColor); 
                  
                     timeLeft.setFont(subFont);
                     timeLeft.setForeground(fontColor); 
                  
                     infinity.setFont(headerFont);
                     infinity.setForeground(fontColor);
                  
                     time.add(currTime);
                  
                     Clock clock = new Clock();
                     Thread th = new Thread(clock);
                     th.start();
                  
                     time.add(clock);
                  
                     time.add(timeLeft);
                  
                     if(timer != 0){
                        Calendar cal = Calendar.getInstance();
                        System.out.println("" + timer);
                        System.out.println("" + cal.getTimeInMillis());
                     
                     
                        remainingTime = new CountDown((timer - cal.getTimeInMillis()));
                     
                        Thread th2 = new Thread(remainingTime);
                     
                        th2.start();
                        time.add(remainingTime);
                     }
                     else{               
                        time.add(infinity);
                     }
                     jpMain.add(time,BorderLayout.NORTH); 
                     pack();
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
            
            try{
               in.close();
               out.close();
               s.close();
            }
            catch(IOException ioe){
               ioe.printStackTrace();
            }
            finally{
               ended = true;
            }
            
         }
      }
      
      class Clock extends JLabel implements Runnable
      {
         private static final long serialVersionUID = 42L;
         public void run()
         {
            setFont(headerFont);
            setForeground(fontColor);          
            new javax.swing.Timer(1000,
               new ActionListener(){
                  public void actionPerformed(ActionEvent ae)
                  {
                     setText(new SimpleDateFormat("hh:mm:ss a").format(new Date()));
                  
                  }
               }).start();
                      
         }
      }
      
      class CountDown extends JLabel implements Runnable{
         
         private static final long serialVersionUID = 42L;
         private long timeLeft;
         private javax.swing.Timer timer;
         
         public CountDown(long _timeLeft){
            timeLeft = _timeLeft / 1000;
         }
         public void run()
         {
            setFont(headerFont);
            setForeground(fontColor);          
            timer = new javax.swing.Timer(1000,
               new ActionListener(){
                  public void actionPerformed(ActionEvent ae)
                  {
                     timeLeft--;
                     int seconds = (int)timeLeft % 60;
                     int hours = (int)timeLeft / 3600;
                     int minutes = (int)(timeLeft - (hours * 3600)) / 60;
                     String sSeconds = "" + seconds;
                     String sHours = "" + hours;
                     String sMinutes = "" + minutes;
                     
                     if(seconds < 10){
                        sSeconds = "0" + seconds;
                     }
                     if(hours < 10){
                        sHours = "0" + hours;
                     }
                     if(minutes < 10){
                        sMinutes = "0" + minutes;
                     }
                     if(timeLeft >= 0){
                        setText(sHours + ":" + sMinutes + ":" + sSeconds);
                     }
                     if(timeLeft <= 0){
                        try{
                           in.close();
                           out.close();
                           s.close();
                        }
                        catch(IOException ioe){
                           ioe.printStackTrace();
                        }
                        finally{
                           ended = true;
                        }
                        
                        JOptionPane.showMessageDialog(null,"Exam has ended: Ran out of Time.");
                        end();
                     }
                  }
               });
            timer.start();
         }
         public void end(){
            timer.stop();
         }
         
         
      }
   }//End Submission
   
   
  /**
   * Main method of client program. Starts client GUI
   */
   public static void main(String[] args){
      new Main();
   }
}