import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

public class FileSubmitter extends JPanel {
   
   private static final long serialVersionUID = 6156864232085906921L;
   
   private ObjectOutputStream oos;
   private JTextArea jtaDisplay;
   
   public FileSubmitter(ObjectOutputStream _oos) {
      oos = _oos;
      
      //Create GUI
      setLayout(new BorderLayout());
      
      JButton jbOpenFiles = new JButton("Open Files");
      add(jbOpenFiles, BorderLayout.NORTH);
      
      jtaDisplay = new JTextArea(5,0);
      jtaDisplay.setEditable(false);
      JScrollPane jscroller = new JScrollPane(jtaDisplay);
      add(jscroller, BorderLayout.SOUTH);
            
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
         } 
         catch (IOException e) {
            System.out.println("IO Exception! " + e.getMessage());
         }
      }
   }
   
   public void readLog(File logTextFile) {
      try {
		   BufferedReader br = new BufferedReader(new FileReader(logTextFile));
					
			// read all data from file and add it to the textArea display
			String line = null;
			while( (line = br.readLine()) != null) {
            jtaDisplay.append(line + "\n");
			}
         jtaDisplay.append("\n");
         br.close();
      } catch (IOException e) {
         System.out.println("Could not read in file from server. IO Exception " + e.getMessage());
      }
   }
   
} //end class FileSubmitter