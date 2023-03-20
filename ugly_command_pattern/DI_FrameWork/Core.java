package ugly_command_pattern.DI_FrameWork;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import ugly_command_pattern.Commands.Commands.*;

public class Core{
     private HashMap<String, Class<?>> registery;

     @Retention(RetentionPolicy.RUNTIME)
     public @interface Use{
          String name();
     }

     @Retention(RetentionPolicy.RUNTIME)
     public @interface Command {
          String name();
     }

     private static Class<?>[] getClasses(String packageName)
               throws ClassNotFoundException, IOException, URISyntaxException {
          ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
          assert classLoader != null;
          String path = packageName.replace('.', '/');
          Enumeration<URL> resources = classLoader.getResources(path);
          List<File> dirs = new ArrayList<File>();
          while (resources.hasMoreElements()) {
               URL resource = resources.nextElement();
               Path filepath = Paths.get(resource.toURI());
               dirs.add(filepath.toFile());
          }
          ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
          for (File directory : dirs) {
               classes.addAll(findClasses(directory, packageName));
          }
          return classes.toArray(new Class[classes.size()]);
     }

     private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
          List<Class<?>> classes = new ArrayList<Class<?>>();
          if (!directory.exists()) {
               return classes;
          }
          File[] files = directory.listFiles();
          for (File file : files) {
               if (file.isDirectory()) {
                    assert !file.getName().contains(".");
                    classes.addAll(findClasses(file, packageName + "." + file.getName()));
               } else if (file.getName().endsWith(".class")) {
                    classes.add(Class
                              .forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
               }
          }
          return classes;
     }

     public void init(String pkg) throws ClassNotFoundException, IOException, URISyntaxException{
          this.registery = new HashMap<>();
          Class<?> classes[] = getClasses(pkg);
          for(var cls : classes){
               for(var annotation : cls.getAnnotations())
                    if(annotation.annotationType().equals(Command.class))
                         this.registery.put(cls.getName().split("\\$")[1], cls);
          }
     }

     public <T> T getInstance(Class<T> cls) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
          Constructor c = null;
          for(var _c : cls.getDeclaredConstructors()){
               var parameter_count = _c.getParameterCount();
               var annotation_count = _c.getParameterAnnotations().length;
               // TODO check if known annotation
               if(parameter_count == annotation_count){
                    c = _c;
                    break;
               }
          }
          if(c == null) return null;
          var parameter_count = c.getParameterCount();
          Object params[] = new Object[parameter_count];
          var parameters =  c.getParameters();
          
          for(int i=0; i< parameter_count; i++){
               var command_annotation = parameters[i].getAnnotation(Use.class);
               Class<?> param_x_cls = this.registery.get(command_annotation.name());
               params[i] = param_x_cls.getDeclaredConstructor(LampDevice.class).newInstance(new LampDevice());
          }
          return (T) c.newInstance(params);
     }
}