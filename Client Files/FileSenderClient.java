/**
 * User chooses a file from a JFileChooser and then that file is send to the server.
 * The client then awaits a response from the server regarding weather that file was good or not.
 */

import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class FileSenderClient {
   
   public static void main (String [] args) {
      new FileSenderClient();
   }
   
   public FileSenderClient() {
      
      //Create socket
      Socket s = null;
      try {
         s = new Socket("localhost", 16789);
      } catch (UnknownHostException e) {
         System.out.println("Unknown Host Exception! " + e.getMessage());
      } catch (IOException e) {
         System.out.println("IO Exception! " + e.getMessage());
      }
      
      try {
         //Create Object output stream
         OutputStream out = s.getOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(out);
         
         //Create Object input stream
         InputStream in = s.getInputStream();
         ObjectInputStream ois = new ObjectInputStream(in);
         
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
         
         //Read in response from server
         //Server will send back the file if it is named incorrectly
         //Server will send a String message of Success if the file is named correctly
         try {
            Object obj = ois.readObject();
            if (obj instanceof File) {
                file = (File)obj;
                System.out.println("Incorrect file name! File named " + file.getName());
            } else if (obj instanceof String){
               String msg = (String)obj;
               System.out.println(msg);
            }
         } catch (ClassNotFoundException e) {
            System.out.println("Failed to read from server. ClassNotFoundException " + e.getMessage());
         }
         
         //Close everything
         oos.close();
         ois.close();  
         s.close(); 
         
      } catch (IOException e) {
         System.out.println("IO Exception! " + e.getMessage());
      }
   }
} //end class FileSender