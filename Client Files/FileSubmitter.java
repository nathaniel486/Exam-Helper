import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/**
 * Class which allows for the accepting of *.java files via drag & drop or JFileChooser.
 * Files are then able to be compiled and then sent to an output stream.
 * Class is displayed as a JPanel so it is able to be placed inside a Swing-based GUI 
 * 
 * @author Brendon Strowe
           Group 2
 */
 
public class FileSubmitter extends JPanel {
   
   private static final long serialVersionUID = 6156864232085906921L;
   
  /**
   * Output stream which accepts submitted Files
   */
   private ObjectOutputStream oos;
   
  /**
   * Displays feedback text to user regarding files submission
   */
   private JTextArea jtaDisplay;
   
  /**
   * Component which will receive dropped Files
   */
   private DropTarget dt;
   
  /**
   * JPanel which acts as the drop receiving component
   */
   private JPanel jpDrop;
   
  /**
   * JLabel which displays every file dropped into the drop target component
   */
   private JLabel jlDropListDisplay;
   
  /**
   * JButton which displays JFileChooser to open a File for submission
   */
   private JButton jbOpenFiles;
   
  /**
   * JButton which clears the ArrayList of all the files chosen for submission as well as clears the jlDropListDisplay
   */
   private JButton jbClearFiles;
   
  /**
   * JButton which initiates writing of file objects to the output stream
   */
   private JButton jbSubmitFiles;
   
  /**
   * List of files which have been chosen to be submitted (either via drag and drop or via JFileChooser)
   */
   private ArrayList<File> listOfDroppedFiles;
   
   
  /**
   * Constructor for the FileSubmitter class.
   * Creates a JPanel which contains all of the GUI elements.
   * instanciates the ObjectOutputStream.
   */
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
         
         jbSubmitFiles = new JButton("Compile & Submit!");
         jpButtons.add(jbSubmitFiles);
      add(jpButtons, BorderLayout.CENTER);
      
      jtaDisplay = new JTextArea(15,0);
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
   
  /**
   * Handles what happens when the various buttons on the GUI are pressed.
   * Clear - Clears the files in the list of files added for submission and clears the display text of the files that were added for submission
   * Open - Displays a JFileChooser which can be used to add files for submission
   * Submit - Goes through the list of submitted files and compiles each. Zips up all the files (submited and compiled) and writes them to the ObjectOutputStream
   */
   class FileTransferListener implements ActionListener {
         
      public void actionPerformed(ActionEvent ae) {
         Object source = ae.getSource();
         
         //If clear button was pressed
         if (source == jbClearFiles) {
            jlDropListDisplay.setText("no files have been added yet...");
            
            listOfDroppedFiles.clear();
            
            jpDrop.setBackground(new Color(238,238,238));
         
         //If Open was presesd
         } else if (source == jbOpenFiles) {
            //Create File object
            File selectedFile = null;
               
            //Open file via JFileChooser
            JFileChooser chooser = new JFileChooser(new File(".").getAbsolutePath()); //Open FileChooser in current directory
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Java source code files", "java"); //Only allow *.java files to be chosen
            chooser.setFileFilter(filter);
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
         
         //If Submit was pressed
         } else if (source == jbSubmitFiles) {
            try {  
              /*
               * Compile files added by user
               * - Display compile results
               * If user wishes to submit files, zip them up and write them to the output stream
               */
               
               ArrayList<File> listToBeSubmitted = new ArrayList<File>();
               
               //---Compile files---
               String dirName = "Practical";

               StringBuffer errorReport;  //Contains the error report
               StringBuffer inReport;     //Contains the input stream
               StringBuffer outReport;    //Contains the output stream
               String lineIn;  
               //TreeMap resultItems = new TreeMap();
               Boolean successfulCompile = null;
               
            	File baseDir = new File("CompiledFiles");
               baseDir.mkdirs();
            	  
               //String  command = "javac *.java";       // This use to work, now errors
               for (File javaFile : listOfDroppedFiles) {
                  
                  //Get the path of the file to be compiled
                  String fileLocation = "" + javaFile;
                  
                  String [] command = new String[] {"javac", fileLocation}; //Terminal command to be run to compile Java file
            
                  try {

                     Process proc = Runtime.getRuntime().exec(command , null, null); //Run command to compile files and save them in the baseDir
                     
                     BufferedReader stdErr = new BufferedReader(new InputStreamReader(proc.getErrorStream() ) );  //Get compiler errors
                     BufferedReader stdIn  = new BufferedReader(new InputStreamReader(proc.getInputStream() ) );  //Get input stream
                     BufferedWriter stdOut = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream() ) );//Get compiler output stream
                     
                     errorReport = new StringBuffer();
                     inReport  = new StringBuffer();
                     outReport = new StringBuffer();
                     
                     //Read in error report
                     while((lineIn=stdErr.readLine())!=null) {
                        errorReport.append(lineIn + "\n");
                     }
                     
                     //Read in inputstream
                     while((lineIn=stdIn.readLine())!=null) {
                        inReport.append(lineIn+"\n");
                     }
                     
                  // while((lineIn=stdOut.readLine())!=null) {
                  //    rptOut.append(lineIn+"\n");
                  // }
                     successfulCompile = new Boolean(errorReport.length()==0);
                     //resultItems.put( dirName, successfulCompile ); // Compiled=T / No=F
                  
                     System.out.println("StdIn ("+inReport.length()+") = \n"+ inReport );
                     System.out.println("StdErr ("+errorReport.length()+")= \n"+ errorReport +"\n\n");
                  // System.out.println("StdOut ("+rptOut.length()+")= \n"+ rptOut +"\n\n");
                  
                     jtaDisplay.append("Compiled file: " + javaFile.getName() +"\n");
                     jtaDisplay.append("Termination exit status = " + proc.exitValue() +"\n");
                     jtaDisplay.append("StdIn ("+inReport.length()+") = \n"+ inReport +"\n");
                     jtaDisplay.append("StdErr ("+errorReport.length()+")= \n"+ errorReport +"\n\n\n");
                  // jtaDisplay.append("StdOut ("+rptOut.length()+")= \n"+ rptOut +"\n\n\n");
                     
                     System.out.println(new File(javaFile.getAbsolutePath().substring(0, javaFile.getAbsolutePath().length()-5) + ".class"));
                     
                     File compiledFile = new File(javaFile.getAbsolutePath().substring(0, javaFile.getAbsolutePath().length()-5) + ".class");
                     
                     listToBeSubmitted.add(javaFile);
                     listToBeSubmitted.add(compiledFile);
                  }
                  catch( IOException ioe ){
                     ioe.printStackTrace();
                  }
         	   }
               
               String promptMessage = successfulCompile.equals(Boolean.TRUE) ? "Your code successfully compiled! Submit it?" : "Your code did NOT successfully compile. Submit it anyway?"; 
               
               //Prompt user if they want to submit compiled files?
               int intOption = JOptionPane.showConfirmDialog(
                                              null,
                                              promptMessage,
                                              "Submit Your Code?",
                                              JOptionPane.YES_NO_OPTION,
                                              JOptionPane.QUESTION_MESSAGE);
               
               if (intOption == JOptionPane.YES_OPTION) {
                  //zip files
                  File zipFile = new File("Submission.zip");
                  
                  BufferedInputStream origin = null;
                  FileOutputStream dest = new FileOutputStream(zipFile);
                  ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(dest));
                  //out.setMethod(ZipOutputStream.DEFLATED);
                  byte data[] = new byte[1024];
                  
                  // get a list of files from current directory
                  for (File file : listToBeSubmitted) {
                     System.out.println("Adding: "+ file);  
                     FileInputStream fi = new FileInputStream(file);
                     origin = new BufferedInputStream(fi, 1024);
                     ZipEntry entry = new ZipEntry(file.getName());
                     zipOut.putNextEntry(entry);
                     
                     int count;
                     while((count = origin.read(data, 0, 1024)) != -1) {
                        zipOut.write(data, 0, count); // Write to the Zip file
                     }
                     origin.close();
                  }
                  //Send file to server
                  oos.writeObject(Files.readAllBytes(zipFile.toPath()));
                  oos.flush();
                  
                  zipOut.close();
                  
                  zipFile.delete();
               } else {
                  JOptionPane.showMessageDialog(null, "Compiled code NOT sent!");
               }
               
               
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