import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.*;
import java.net.*;

/**
*This class extends JPanel and is used for the chat between the professor and student.  
*/

public class ChatClient extends JPanel
{
   private ObjectOutputStream out = null;/**Used for writing messages */
   private JTextField sendText;/** the text field you type your message into*/
   private JTextArea receiveText;/**The message history */
   private JPanel 	textPanel;/** holds the sendText and send button objects*/
   private JButton 	send;/**Send the message you typed */
   private String name = null;/** the name of the person*/
   private boolean isConnected = false;/** are you connected?*/
   private static final long serialVersionUID = 42L;
   
   
   public ChatClient(ObjectOutputStream _out,String _name,JTextArea _receiveText)
   {
      //instanciate the reader and writer objects
      out = _out;
      name = _name;
      receiveText = _receiveText;   
      
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
      
      //action performed for the send button            
      send.addActionListener(
         new ActionListener(){
            public void actionPerformed(ActionEvent ae)
            {
               try
               {
                  String msg = sendText.getText();
                  if(!msg.equals("")){
                     out.writeObject(msg);
                     out.flush();
                     sendText.setText(null);
                  }
               }
               catch(IOException ioe)
               {
               
               }
            }
         });
      //key listener to send message if the key is hit   
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