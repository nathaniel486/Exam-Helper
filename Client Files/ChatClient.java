import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.*;
import java.net.*;

public class ChatClient extends JPanel
{
   private ObjectOutputStream  out = null;
   private JTextField sendText;
   private JTextArea receiveText;
   private JPanel 	textPanel;
   private JButton 	send;
   private String name = null;
   private boolean isConnected = false;
   private static final long serialVersionUID = 42L;
   
   
   public ChatClient(ObjectOutputStream _out,String _name,JTextArea _receiveText)
   {
   
      out = _out;
      name = _name;
      receiveText = _receiveText;
   
      System.out.println("Starting the client..");
   
      
      setLayout(new BorderLayout());
   
   
      send = new JButton("Send");
      
      
      //send messages
      sendText = new JTextField(50);
      sendText.setBorder(new EtchedBorder());
      
      
      //receive messages      
      receiveText.setBorder(new EtchedBorder());
      receiveText.setLineWrap(true);
      receiveText.setWrapStyleWord(true);
      receiveText.setEditable(false);
      
      JPanel sendPanel = new JPanel(new BorderLayout());
      sendPanel.add(sendText,BorderLayout.WEST);
      sendPanel.add(send,BorderLayout.EAST);
      
      textPanel = new JPanel();
      textPanel.setLayout(new BorderLayout());
      
      JScrollPane scrollReceive = new JScrollPane(receiveText);
      
      textPanel.add(scrollReceive,BorderLayout.CENTER);
      textPanel.add(sendPanel,BorderLayout.SOUTH);
   
      add(textPanel,BorderLayout.CENTER);
      

                  
      send.addActionListener(
         new ActionListener(){
            public void actionPerformed(ActionEvent ae)
            {
               try
               {
               String msg = sendText.getText();
               out.writeObject(msg);
               out.flush();
               sendText.setText(null);
               }
               catch(IOException ioe)
               {
               
               }
            }
         });
      sendText.addKeyListener(
         new KeyAdapter(){
            public void keyPressed(KeyEvent ke)
            {               
               if(ke.getKeyCode() == KeyEvent.VK_ENTER)
               {
                  send.doClick();
               }   
            }
         });
         
      
         
      try
      {
         out.writeObject(name);
         out.flush();
      }
      catch(IOException ioe)
      {
         ioe.printStackTrace();
      }   
   }

}