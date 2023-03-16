package ugly_command_pattern.Commands;
import ugly_command_pattern.DI_FrameWork.Core;

public class Commands {
     enum LampState {
          OFF, ON
     }

     public static class LampDevice {
          private LampState state;

          public LampDevice() {
               this.state = LampState.OFF;
          }

          public void SwitchOff() {
               if (this.state == LampState.ON)
                    this.state = LampState.OFF;
               this.printState();
          }

          public void SwitchOn() {
               if (this.state == LampState.OFF)
                    this.state = LampState.ON;
               this.printState();
          }

          private void printState() {
               System.out.println(state);
          }
     }

     public static interface Command {
          void exec();
     }

     @Core.Command(name = "SwitchOnCommand")
     public static class SwitchOnCommand implements Command {
          LampDevice device;

          public SwitchOnCommand(LampDevice device) {
               this.device = device;
          }

          @Override
          public void exec() {
               System.out.println("Switch On Command");
               this.device.SwitchOn();
          }
     }

     @Core.Command(name = "SwitchOffCommand")
     public static class SwitchOffCommand implements Command {
          LampDevice device;

          public SwitchOffCommand(LampDevice device) {
               this.device = device;
          }

          @Override
          public void exec() {
               System.out.println("Switch Off Command");
               this.device.SwitchOff();
          }
     }

     public static class UglyRemote {
          Command command;

          public UglyRemote(@Core.Use(name = "SwitchOffCommand") Command command) {
               this.command = command;
          }

          public void useRemote() {
               this.command.exec();
          }
     }
}
