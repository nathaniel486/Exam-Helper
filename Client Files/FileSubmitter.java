import javax.swing.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class FileSubmitter extends JPanel {
   
   private static final long serialVersionUID = 6156864232085906921L;
   
   private ObjectOutputStream oos;
   private JTextArea jtaDisplay;
   private DropTarget dt;  //Component which will receive droped items
   private JPanel jpDrop;
   private JLabel jlDropListDisplay;
   private ArrayList<File> listOfDroppedFiles;

   public FileSubmitter(ObjectOutputStream _oos) {
      oos = _oos;
      
      listOfDroppedFiles = new ArrayList<File>();
      
      //Create GUI
      setLayout(new BorderLayout());
      
      jpDrop = new JPanel(new BorderLayout());
         JLabel jlDrop = new JLabel("Drop files here...", SwingConstants.CENTER);
            jlDrop.setFont(new Font("Arial", Font.BOLD, 24));
            jlDrop.setForeground(new Color(24,103,154));
         jpDrop.add(jlDrop, BorderLayout.NORTH);
         jlDropListDisplay = new JLabel("no files have been added yet...", SwingConstants.CENTER);
         jpDrop.add(jlDropListDisplay, BorderLayout.CENTER);
         JButton jbOpenFiles = new JButton("Submit Files...");
         jpDrop.add(jbOpenFiles, BorderLayout.SOUTH);
         jpDrop.setPreferredSize(new Dimension(500,500));
      add(jpDrop);
      
      jtaDisplay = new JTextArea(10,0);
      jtaDisplay.setEditable(false);
      JScrollPane jscroller = new JScrollPane(jtaDisplay);
      add(jscroller, BorderLayout.SOUTH);
            
      //Create Listener object
      FileTransferListener ftl = new FileTransferListener();
      
      //Register Listeners
      jbOpenFiles.addActionListener(ftl);
      DropHandler dropHandler = new DropHandler();
      
      //Set the JPanel to recieve drops
      dt = new DropTarget(this, dropHandler);
   }
   
   class FileTransferListener implements ActionListener {
         
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
   
   class DropHandler extends DropTargetAdapter { 
 
      public void dragEnter(DropTargetDragEvent dtde) {
         System.out.println("Drag Enter");
         jpDrop.setBackground(new Color(195,205,235));
      }
    
      public void dragExit(DropTargetEvent dtde) {
         System.out.println("Drag Exit");
         jpDrop.setBackground(new Color(238,238,238));
      }
        
      public void drop(DropTargetDropEvent dtde) {
         
         try {
            //Get the dropped Object and try to figure out what it is
            Transferable tr = dtde.getTransferable();
            DataFlavor[] flavors = tr.getTransferDataFlavors(); //list of DataFlavors acceptable by the DropTargetDropEvent class
            
            for (DataFlavor flavor : flavors) {               
               //If we're dropping a FileList...
               if (flavor.isFlavorJavaFileListType()) {
                  //Copy in the dropped files and display a note of the action
                  dtde.acceptDrop(DnDConstants.ACTION_COPY);
                  
                  jlDropListDisplay.setText("");
                  String displayText = "<html>";
                  
                  //Add the list of file names to our text area and add the files to our ArrayList of files
                  java.util.List<File> droppedFiles = (java.util.List<File>)tr.getTransferData(flavor);
                  for (File file : droppedFiles) {
                     displayText += file.getName() + "<br>";
                     listOfDroppedFiles.add(file);
                  }
                  jlDropListDisplay.setText(displayText);
                  
                  //Everything worked! Woo!
                  dtde.dropComplete(true);
                  jpDrop.setBackground(new Color(155,238,155));
                  return;
               }
            }
            
            //The user didn't drop a supported dataFavor that we were looking for (i.e. fileList)
            System.out.println("Drop failed: " + dtde);
            jpDrop.setBackground(new Color(238,155,155));
            dtde.rejectDrop();
         } 
         catch (UnsupportedFlavorException e) {
            System.out.println("The data you are trying to drop is not supported. Unsupported Flavor Exception! " + e.getMessage());
            e.printStackTrace();
            dtde.rejectDrop();
         } catch (IOException e) {
            System.out.println("Error trying to transfer the dropped data. IO Exception! " + e.getMessage());
            e.printStackTrace();
            dtde.rejectDrop();
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