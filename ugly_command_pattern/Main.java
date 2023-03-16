package ugly_command_pattern;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.Scanner;

import ugly_command_pattern.Commands.Commands.UglyRemote;
import ugly_command_pattern.DI_FrameWork.Core;


public class Main{

     public static void main(String args[]) throws NoSuchMethodException, ClassNotFoundException, IOException, URISyntaxException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SecurityException {
          Core di = new Core();
          di.init("ugly_command_pattern.Commands");
          UglyRemote xRemote = di.getInstance(UglyRemote.class);

          Scanner sc = new Scanner(System.in);
          while(true){
               String str = sc.next();
               if(str.equals("end")) break;
               // AS IF SOMETHING COULD CHANGE !! 😉
               xRemote.useRemote();
          }
          sc.close();
          
     }
}