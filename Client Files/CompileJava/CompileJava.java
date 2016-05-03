import java.io.*;
import java.util.*;

public class CompileJava {

   public static void main(String [] args){
      String assignmentNum = "Prac1";
   
      String dirName = "TestPrac1";
      String destDir = "reports_"+assignmentNum;
      StringBuffer rptErr;
      StringBuffer rptIn;   
      StringBuffer rptOut; 
      String lineIn;  
      TreeMap resultItems = new TreeMap();
   
      File baseDir = new File(dirName);
      String  command = "javac Student2.java";
//      String  command = "javac *.java";       // This use to work, now errors
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
      
         BufferedWriter outRpt = new BufferedWriter(new FileWriter(destDir+ "\\"+ dirName ));
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
}