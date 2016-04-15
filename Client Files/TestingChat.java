import java.net.*;
import java.io.*;
import javax.swing.*;

public class TestingChat
{
   private Socket s = null;
   private ObjectOutputStream  out = null;
   private ObjectInputStream in = null;
   private String address = "localhost";
   private int port = 16789;
   private String name = null;
   private JTextArea receiveText;


   public static void main(String[] args)
   {
      if(args.length > 0)
      {
         new TestingChat(args[0]);
      }
      else
      {
         System.out.println("specify a name")  ; 
      }
   }
   
   public TestingChat(String _name)
   {
      name = _name;
      try
      {
      
         s = new Socket(address,port);
         out = new ObjectOutputStream(s.getOutputStream());
         out.flush();
         in = new ObjectInputStream(s.getInputStream());
         receiveText = new JTextArea(10,30);
         ChatClient c = new ChatClient(s,out,in,name,receiveText);
         (new Reading()).start();
      
         
      }
      catch(UnknownHostException uhe) 
      {
         System.out.println("Unable to connect to host.");
      }
      catch(IOException ie) 
      {
         System.out.println("Unable to connect to host.");
      }
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