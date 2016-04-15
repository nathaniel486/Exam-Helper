import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

public class FileSubmitter extends JPanel {
   
   private ObjectOutputStream oos;
   
   public FileSubmitter(ObjectOutputStream _oos) {
      oos = _oos;
      
      //Create GUI
      JButton jbOpenFiles = new JButton("Open Files");
      add(jbOpenFiles);
      
      //Create Listener object
      FileTransferListener ftl = new FileTransferListener();
      
      //Register OpenFiles button
      jbOpenFiles.addActionListener(ftl); 
   }
   
   class FileTransferListener implements ActionListener {
         
      public FileTransferListener() {
      }
         
      public void actionPerformed(ActionEvent ae) {
         try {  
            //Create File object
            File file = null;
               
            //Open file via JFileChooser
            JFileChooser chooser = new JFileChooser(new File(".").getAbsolutePath()); //Open FileChooser in current directory
            int returnVal = chooser.showOpenDialog(null);   //Returns the CONSTANT value for whether the choice was Approve, Cancel, or an error
            file = chooser.getSelectedFile();
            //If a file is successfully opened
            if (file != null && returnVal == JFileChooser.APPROVE_OPTION) {
               //Send file to server
               oos.writeObject(file);
               oos.flush();
            }
               
            // //Read in response from server
//             //Server will send back the file if it is named incorrectly
//             //Server will send a String message of Success if the file is named correctly
//             try {
//                Object obj = ois.readObject();
//                if (obj instanceof File) {
//                   file = (File)obj;
//                   System.out.println("Incorrect file name! File named " + file.getName());
//                } 
//                else if (obj instanceof String){
//                   String msg = (String)obj;
//                   System.out.println(msg);
//                }
//             } 
//             catch (ClassNotFoundException e) {
//                System.out.println("Failed to read from server. ClassNotFoundException " + e.getMessage());
//             }
                              
         } 
         catch (IOException e) {
            System.out.println("IO Exception! " + e.getMessage());
         }
      }
   }
   
   
} //end class FileSubmitter