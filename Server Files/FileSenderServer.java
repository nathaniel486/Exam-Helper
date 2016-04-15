/**
 * Server receives objects of File type.
 * The server then checks the name of the File and sends a message back to the client 
 * whether or not the File received is correct or not.
 */

import java.net.*;
import java.io.*;

public class FileSenderServer {
   
   public static void main (String [] args) {
      new FileSenderServer();
   } //end main()
   
   public FileSenderServer() {
      //Create Sockets
      ServerSocket ss = null;
      Socket s = null;
      try {
         //Create server socket
         ss = new ServerSocket(16789);
         System.out.println("Waiting for client to connect");
         //Accept connection from client
         s = ss.accept(); 
         System.out.println("Client Connected!");
      } catch (IOException e) {
         System.out.println("IO Exception! " + e.getMessage());
      }
      
      try {
         //Create object input stream
         InputStream in = s.getInputStream();
         ObjectInputStream ois = new ObjectInputStream(in);
         
         //Create object output stream
         OutputStream out = s.getOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(out);
         
         //Create File object and then read in File object from client
         File file = null;
         try {
            file = (File)ois.readObject();
         } catch (ClassNotFoundException e) {
            System.out.println("IO Exception encountered! " + e.getMessage());
         }
         
         //If File read is successful
         if (file != null) {
            
            //check fileName
            String fileName = file.getName();
            if (fileName.equalsIgnoreCase("text.txt")) {
               //IF the fileName is correct, respond with message that it is correct and then print out the file to the Server Screen
               oos.writeObject("Correct file name!");
               //print out file contents
               StringBuffer sb = new StringBuffer();
               try {
         		   BufferedReader br = new BufferedReader(new FileReader(file));
      				// read all data from file and print it out
      				String line = null;
      				while( (line = br.readLine()) != null) {
                     System.out.println(line + "\n");
      				}
                  br.close();
                  
      			} catch(IOException e) {
      				System.out.println("IO Exception encountered! " + e.getMessage());
               }
               //If the fileName is not correct, send the File back to the client
            } else {
               oos.writeObject(file);
            }
            oos.flush();
            
            //Close ALL the things
            oos.close();
            ois.close();
            s.close();
         }
      } catch(IOException e) {
   	   System.out.println("IO Exception encountered! " + e.getMessage());
      }
      
   } //end constructor FileSenderServer
   
} //end class FileSenderServer