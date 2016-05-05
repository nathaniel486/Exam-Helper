import javax.swing.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

public class FileSubmitter extends JPanel {
   
   private static final long serialVersionUID = 6156864232085906921L;
   
   private ObjectOutputStream oos;
   private JTextArea jtaDisplay;
   private DropTarget dt;  //Component which will receive droped items
   private JPanel jpDrop;
   private JLabel jlDropListDisplay;
   
   private JButton jbOpenFiles;
   private JButton jbClearFiles;
   private JButton jbSubmitFiles;
   
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
         
         jpDrop.setPreferredSize(new Dimension(500,500));
      add(jpDrop, BorderLayout.NORTH);
      
      JPanel jpButtons = new JPanel(new FlowLayout());
         jbClearFiles = new JButton("Clear files");
         jpButtons.add(jbClearFiles);
         
         jbOpenFiles = new JButton("Open Files...");
         jpButtons.add(jbOpenFiles);
         
         jbSubmitFiles = new JButton("Submit!");
         jpButtons.add(jbSubmitFiles);
      add(jpButtons, BorderLayout.CENTER);
      
      jtaDisplay = new JTextArea(10,0);
      jtaDisplay.setEditable(false);
      JScrollPane jscroller = new JScrollPane(jtaDisplay);
      add(jscroller, BorderLayout.SOUTH);
            
      //Create Listener object
      FileTransferListener buttonHandler = new FileTransferListener();
      
      //Register Listeners
      jbClearFiles.addActionListener(buttonHandler);
      jbOpenFiles.addActionListener(buttonHandler);
      jbSubmitFiles.addActionListener(buttonHandler);
      
      DropHandler dropHandler = new DropHandler();
      
      //Set the JPanel to recieve drops
      dt = new DropTarget(this, dropHandler);
   }
   
   class FileTransferListener implements ActionListener {
         
      public void actionPerformed(ActionEvent ae) {
         Object source = ae.getSource();
         
         //If clear button was pressed
         if (source == jbClearFiles) {
            jlDropListDisplay.setText("");
            
            listOfDroppedFiles.clear();
            
            jpDrop.setBackground(new Color(238,238,238));
         //If Open was presesd
         } else if (source == jbOpenFiles) {
            //Create File object
            File selectedFile = null;
               
            //Open file via JFileChooser
            JFileChooser chooser = new JFileChooser(new File(".").getAbsolutePath()); //Open FileChooser in current directory
            int returnVal = chooser.showOpenDialog(null);   //Returns the CONSTANT value for whether the choice was Approve, Cancel, or an error
            selectedFile = chooser.getSelectedFile();
            //If a file is successfully opened
            if (selectedFile != null && returnVal == JFileChooser.APPROVE_OPTION) {
               //Add file to list of files
               listOfDroppedFiles.add(selectedFile);
               
               //Display the list of currently added files to the jlDropListDisplay
               String displayText = "<html>";
               
               for (File droppedFile : listOfDroppedFiles) {
                  displayText += droppedFile.getName() + "<br>";
               }
               
               jlDropListDisplay.setText(displayText);
               
            }                              
            
         } else if (source == jbSubmitFiles) {
            try {  
               //compile files
               String dirName = "Practical";

               StringBuffer rptErr;
               StringBuffer rptIn;   
               StringBuffer rptOut; 
               String lineIn;  
               TreeMap resultItems = new TreeMap();
            	  
            	File baseDir = new File("compiledFiles");
               baseDir.mkdirs();
            	  
         //      String  command = "javac *.java";       // This use to work, now errors
               for (File javaFile : listOfDroppedFiles) {
                  String fileLocation = "" + javaFile;
                  fileLocation = fileLocation.replaceAll("\\s", "\\ ");
                  System.out.println(fileLocation);
                  String command = "javac " + fileLocation;
            
                  try{
            //         Process proc = Runtime.getRuntime().exec(command); // String[] envp
                     Process proc = Runtime.getRuntime().exec(command , null, baseDir); // String[] envp
                     BufferedReader stdErr = new BufferedReader(new InputStreamReader(proc.getErrorStream() ) );
                     BufferedReader stdIn  = new BufferedReader(new InputStreamReader(proc.getInputStream() ) );
                     BufferedWriter stdOut = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream() ) );
                     rptErr = new StringBuffer();
                     rptIn  = new StringBuffer();
                     rptOut = new StringBuffer();
                     while((lineIn=stdErr.readLine())!=null)  { rptErr.append(lineIn + "\n"); }
                     while((lineIn=stdIn.readLine())!=null )  { rptIn.append(lineIn+"\n");    }
                  //             while((lineIn=stdOut.readLine())!=null ) { rptOut.append(lineIn+"\n");   }
                  
                     resultItems.put( dirName, new Boolean(rptErr.length()==0) ); // Compiled=T / No=F
                  
                     System.out.println("StdIn ("+rptIn.length()+") = \n"+ rptIn );
                     System.out.println("StdErr ("+rptErr.length()+")= \n"+ rptErr +"\n\n");
                  //            System.out.println("StdOut ("+rptOut.length()+")= \n"+ rptOut +"\n\n");
                  
                     BufferedWriter outRpt = new BufferedWriter(new FileWriter("log.txt"));
                     outRpt.write("Source directory: " + dirName +"\n");
                     outRpt.write("Termination exit status = " + proc.exitValue() +"\n");
                     outRpt.write("StdIn ("+rptIn.length()+") = \n"+ rptIn +"\n");
                     outRpt.write("StdErr ("+rptErr.length()+")= \n"+ rptErr +"\n\n\n");
                  //              outRpt.write("StdOut ("+rptOut.length()+")= \n"+ rptOut +"\n\n\n");
                     outRpt.close();
                  }
                  catch( IOException ioe ){
                     ioe.printStackTrace();
                  }
         	   }
               
               
               //zip files
               File zipFile = new File("../zippedSubmission.zip");
               
               BufferedInputStream origin = null;
               FileOutputStream dest = new FileOutputStream(zipFile);
               ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(dest));
               //out.setMethod(ZipOutputStream.DEFLATED);
               byte data[] = new byte[1024];
               
               // get a list of files from current directory
               for (File file : listOfDroppedFiles) {
                  System.out.println("Adding: "+ file);  
                  FileInputStream fi = new FileInputStream(file);
                  origin = new BufferedInputStream(fi, 1024);
                  ZipEntry entry = new ZipEntry(file.getPath());
                  zipOut.putNextEntry(entry);
                  
                  int count;
                  while((count = origin.read(data, 0, 1024)) != -1) {
                     zipOut.write(data, 0, count); // Write to the Zip file
                  }
                  origin.close();
               }
               //Send file to server
               oos.writeObject(zipFile);
               oos.flush();
               
               zipOut.close();
               
               
            } 
            catch (IOException e) {
               System.out.println("IO Exception! " + e.getMessage());
            }
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
                  
                  //Add the files to our ArrayList of files
                  java.util.List<File> droppedFiles = (java.util.List<File>)tr.getTransferData(flavor);
                  for (File file : droppedFiles) {
                     listOfDroppedFiles.add(file);
                  }
                  
                  //Display the list of currently added files to the jlDropListDisplay
                  String displayText = "<html>";
                  
                  for (File droppedFile : listOfDroppedFiles) {
                     displayText += droppedFile.getName() + "<br>";
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