// copyright (c) 2002, Paul Dana.
// Distributed under the GNU GPL: http://www.fsf.org/copyleft/gpl.html

// TorqueDebugListener

// A class must implement this interface if it wishes to use the
// TorqueDebug component for communicating with the TGE debugger

package com.garagegames.torque;

import java.util.*;

public interface TorqueDebugListener
{
   // called if creation itself fails
   // the TorqueDebug object reference should be
   // nulled by the implementor of this interface when
   // this method is called.
   public void createFailed(Exception e);

   // called when an error occurs
   public void error(Exception e);

   // called when a new output string is received from TGE
   public void rawOutput(String s);

   // called when a new input string is sent to TGE
   public void rawInput(String s);

   // called when program quits or connection is lost
   // the TorqueDebug object reference should be
   // nulled by the implementor of this interface when
   // this method is called.
   public void preDestroy();

   // called when connection has been established
   public void connected();

   // called when script execution paused
   public void paused(String fileName, int lineNumber);

   // called when script execution is running
   public void running();

   // called when a script file list received from TGE
   public void fileList(String[] list);

   // called when new callstack available
   // NOTE: paused() is called *before* this method
   public void callStack(String[] filestack, int[] numberstack,
                         String[] functionstack);

   // console output
   public void consoleOutput(String s);

   // called when breakpoint list has been updated
   public void breakPointListChanged(String fileName, Collection breakList);

   // called when 'evaluated' variable value has been reported
   public void evaluationReady(String variable, String value);
}
