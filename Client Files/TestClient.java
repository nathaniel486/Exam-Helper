import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import java.net.*;

public class TestClient extends JFrame{
  //Global Attributes
   //GUI Attributes
   private JTextField jtfRitName;   //RIT Username of Client
   private JTextField jtfIp;        //IP Address of Client
   private JTextField jtfFirstName; //First name of Client
   private JTextField jtfLastName;  //Last name of Client 
   
   //Field Attributes
   private String ritName;
   private String ip;
   private String firstName;
   private String lastName;
   private String fullName;
   
   //Network Attributes
   private final int PORT_NUMBER = 16789;
   private static final long serialVersionUID = 42L;
   
  /**
   * Creating and building GUI
   */
   public TestClient() {
   
      JPanel jpMain = new JPanel(new BorderLayout()); //Main JPanel for the Beginning GUI where client inputs credentials
      
      JPanel jpStartMain = new JPanel(new GridLayout(4,2)); //JPanel for each textfeild 
      
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
   
      JButton jbStart = new JButton("Start Exam");
      jpStartButton.add(jbStart);
   
         
      jpMain.add(jpStartMain, BorderLayout.NORTH);
      jpMain.add(jbStart, BorderLayout.SOUTH);
          
      add(jpMain);
      setLocationRelativeTo(null);
      pack();
      setVisible(true);
      
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
               
               new SubmissionGUI();
               setVisible(false);              
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
      private JTextArea receiveText;
      private ChatClient chatPanel;
      private FileSubmitter fileSubmitPanel;
      private static final long serialVersionUID = 42L;
      
      public SubmissionGUI() {
        //Networking setup
         //Create socket
         try {
            s = new Socket(ip, PORT_NUMBER);
            out = new ObjectOutputStream(s.getOutputStream());
            out.flush();
            in = new ObjectInputStream(s.getInputStream());
            receiveText = new JTextArea(10,30);
            chatPanel = new ChatClient(out,fullName,receiveText);
            fileSubmitPanel = new FileSubmitter(out);
            
            (new Reading()).start();
         } 
         catch (UnknownHostException e) {
            System.out.println("Unknown Host Exception! " + e.getMessage());
         } 
         catch (IOException e) {
            System.out.println("IO Exception! " + e.getMessage());
         }
         
         
      
      
        //Create main GUI
         //Creation of top menu bar
         JPanel jpMain = new JPanel(new BorderLayout());
         add(jpMain);
         
         JMenuBar jmbar = new JMenuBar();
         JMenu jmFile = new JMenu("File");
         JMenuItem jmiFileQuit = new JMenuItem("Quit");
         JMenuItem jmiFileLogout = new JMenuItem("Logout");
         jmFile.add(jmiFileQuit);
         jmFile.add(jmiFileLogout); 
         jmbar.add(jmFile);
            
         JMenu jmEdit = new JMenu("Edit");
         JMenuItem jmiEditPort = new JMenuItem("Change Port");
         jmEdit.add(jmiEditPort);
         jmbar.add(jmEdit);
         setJMenuBar(jmbar);
         
         //Panel for nathaniel/chat   
         jpMain.add(chatPanel, BorderLayout.EAST);
      
         //Panel for Brendon/FileTransfer
         jpMain.add(fileSubmitPanel, BorderLayout.WEST); 
                  
         setVisible(true);
         setDefaultCloseOperation( EXIT_ON_CLOSE );
         setLocationRelativeTo(null);
         pack();
         
         addWindowListener(
         new WindowAdapter(){
            public void windowClosing(WindowEvent we){
               System.out.println("Closing the Client...");
               try
               {
                  out.writeObject("quit");
                  out.flush();
                  System.exit(0);
               }
               catch(IOException ioe)
               {
               
               }
               	
            }	
         });
         
         
      }
      
      class Reading extends Thread
      {
         public void run()
         {
            Object ob = null;
            while(true)
            {
               try
               {
                  ob = in.readObject();
               
               
                  if(ob instanceof String)
                  {
                     String msg = (String)in.readObject();
                     String inName = (String)ob;
                     receiveText.append(inName + "\n");
                     receiveText.append(msg + "\n");
                     receiveText.setCaretPosition(receiveText.getDocument().getLength());
                  
                  } else if (ob instanceof File) {
                     //Read in error responses OKs from server (stored in a txt file)
                     //Append them into text area in the FileSumbitter JPanel
                  } else {
                     System.out.println("How did we get here?");
                  }
               }
               catch(IOException ioe)
               {
               
               }
               catch(ClassNotFoundException cnf)
               {
                  break;
               }
            }
         }
      }
   }
   public static void main(String[]args){
      new TestClient();
   }
}