import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import java.net.*;

public class ImDoneServer extends JFrame{
   //Global Variables
   //GUI Attributes
   private JTextField jtfpassword;
   private JTextField jtfstartTime;
   private JTextField jtfendTime;
   private JTextField jtfprofName;
   private JTextField jtfnameOfFile;
   
   //Field Attributes
   private String password;  //Password Setter
   private String startTime;   //Set timer on Server side
   private String profName;   //Prof name
   private String nameOfFile;   //Name of file to be submitted 
   private String endTime;
   
   //Network Attributes
   private final int PORT_NUMBER = 16789;
   private static final long serialVersionUID = -1624623784600240948L;

   public ImDoneServer(){
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
               startTime = jtfstartTime.getText();
               endTime = jtfendTime.getText();
               password = jtfpassword.getText();
               
               new Server();
               setVisible(false);
            }
         });
   }      
      
   class Server extends JFrame{      
      private Socket s = null;
      private ObjectOutputStream  out = null;
      private ObjectInputStream in = null;
      private ChatServer chatServer;
      
      private static final long serialVersionUID = 42L;
      
      public Server(){
         
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
      }
   }
   public static void main(String [] args){
      new ImDoneServer();
   }
}