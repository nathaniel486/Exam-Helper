import java.net.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;




public class ChatServer
{
   private Vector<ThreadedClient> clients = new Vector<ThreadedClient>();
   private Vector<String> clientNames = new Vector<String>();
   private ChatServerGUI gui;
   private HashMap<String,JTextArea> users = new HashMap<String,JTextArea>();


   
   public static void main(String[] args)
   {
      
      new ChatServer();
   }
   
   public ChatServer()
   {
      System.out.println("Starting the server...");
      
      
      
      ServerSocket ss = null;
      Socket s = null;
      try
      {   
         ss = new ServerSocket(16789);
         
      }
      catch(IOException ioe)
      {
         ioe.printStackTrace();
      }
      
      gui = new ChatServerGUI(users);
      if(!clientNames.contains("Server"))
      {
         (new StartServerClient()).start();
      }
      while(true)
      {
         try
         {
            s = ss.accept();
            System.out.println(s);
         }
         catch(IOException ioe)
         {
            ioe.printStackTrace();
         }
         clients.add(new ThreadedClient(s));
         clients.get(clients.size() - 1).start();
         
      }
   }
   class StartServerClient extends Thread
   {
      public void run()
      {
         
         gui.startClient();
         
      }
   }
   
   class ThreadedClient extends Thread
   {
      private Socket s = null;
      private ObjectInputStream in = null;
      private ObjectOutputStream out = null;
      private String name = null;
      private int position;
      private JTextArea receiveText;
      
      public ThreadedClient(Socket _s)
      {
         s = _s;
         
         receiveText = new JTextArea(10,30);  
         receiveText.setBorder(new EtchedBorder());
         receiveText.setLineWrap(true);
         receiveText.setWrapStyleWord(true);
         receiveText.setEditable(false);
         gui.scrollReceive.add(receiveText);
         
         try
         {
            out = new ObjectOutputStream(s.getOutputStream());
            out.flush();
            in = new ObjectInputStream(s.getInputStream());
            
         }
         catch(IOException ioe)
         {
            ioe.printStackTrace();   
         }
      
      }
   
      public void run()
      {
      
         position = clients.indexOf(this);
         
         System.out.println("" + position);
      
         String msg = null; 
         try
         {
            name = (String)in.readObject();
            clientNames.add(position,name);
            users.put(name,receiveText);
               
            synchronized(clientNames)
            {
               gui.addClient(name);
            }
            System.out.println(name + " has joind the chat");
            
            sendOut("");
            sendOut(name + " has joind the chat");
            
         }
         catch(IOException ioe)
         {
            ioe.printStackTrace();   
         }
         catch(ClassNotFoundException cnf)
         {
            cnf.printStackTrace();
         }
         
         
         
         do
         {
            Object obj = null;
            try {
               obj = in.readObject();
            } 
            catch (IOException e) {
               System.out.println("IO Exception encountered! " + e.getMessage());
            } 
            catch (ClassNotFoundException e) {
               System.out.println("Class Not Found Exception encountered! " + e.getMessage());
            }
         
            if (obj instanceof File) {
               //Create File object and then read in File object from client
               File file = null;
               file = (File)obj;
               
            } 
            else if (obj instanceof String) {
               
               msg = (String)obj;
                                 
               if(msg.equalsIgnoreCase("quit"))
               {
                     
                  sendOut(name + " has left the chat");
                  sendOut("");
                     
                     
                  break;
               }
               
                  
               sendOut(name + ":");
               sendOut(msg);
                  
               System.out.println(name + ": " + msg);
                              
               
            } 
            else {
               System.out.println("How did we get here?");
            }
            
         }
         while(true);
         System.out.println(name + " has disconected");
         try
         {
            in.close();
            out.close();
            s.close();
         }
         catch(IOException ioe)
         {
            return;
         }
         
         clients.remove(clients.indexOf(this));
         synchronized(clientNames)
         {
            gui.removeClient(name);
         }
      }
   
      public void sendOut(String msg)
      {
         try
         {
            out.writeObject(msg);
            out.flush();
            receiveText.append(msg + "\n");
            receiveText.setCaretPosition(receiveText.getDocument().getLength());
         
            
         }
         catch(IOException ioe)
         {
         
         }
      }
   
   }
   
}
 
