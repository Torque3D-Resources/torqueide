// copyright (c) 2002, Paul Dana.
// Distributed under the GNU GPL: http://www.fsf.org/copyleft/gpl.html

// TorqueDebug

// Handles communication with Torque Game Engine debugger.

package com.garagegames.torque;

import java.io.*;
import java.net.*;
import java.util.*;

import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.msg.EditPaneUpdate;
import org.gjt.sp.jedit.msg.ViewUpdate;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.util.Log;

/**
 *  Description of the Class
 *
 *@author     Paul Dana
 *@created    15. Dezember 2003
 */
public class TorqueDebug implements Runnable {
   // debug options
   TorqueDebugOptions options;

   // if we launched local game executable
   Process gameProcess;

   // control
   Thread thread;
   TorqueDebugListener listener;
   Socket sock;
   PrintWriter w;
   BufferedReader r;

   // if we disconnect or the program we launched exits...then we DIE :)
   boolean initialized;

   // state
   long startTime;
   // system time we 1st attempted connect
   boolean paused;
   boolean compiling;
   String currentFileCompiling;
   boolean shuttingDown = false;

   // files containing breakpoints stored in a hash table
   // the key is the name of the file, the value is an ArrayList
   // containing the list of TorqueBreakPoint data for that file
   Hashtable files = new Hashtable();

   // the variable we are currently evaluating
   /**
    *  Description of the Field
    */
   protected String evaluationVariable;


   // private constructor
   /**
    *  Constructor for the TorqueDebug object
    *
    *@param  listener  Description of the Parameter
    *@param  options   Description of the Parameter
    */
   private TorqueDebug(TorqueDebugListener listener, TorqueDebugOptions options) {
      this.listener = listener;
      this.options = options;
   }


   // these create methods are the only way to get an instance of this class.
   // this method creates a new instance and runs it in it's own thread.
   /**
    *  Description of the Method
    *
    *@param  listener  Description of the Parameter
    *@param  launch    Description of the Parameter
    *@param  host      Description of the Parameter
    *@param  port      Description of the Parameter
    *@param  password  Description of the Parameter
    *@param  gamePath  Description of the Parameter
    *@return           Description of the Return Value
    */
   public static synchronized TorqueDebug create(TorqueDebugListener listener,
         boolean launch, String host, int port, String password,
         File gamePath)
   {
      // we MUST have a listener
      if (listener == null)
      {
         return null;
      }

      // if any param is null
      if (host == null || password == null || gamePath == null)
      {
         listener.createFailed(new TorqueDebugException(
                                  "Null Parameter in creation"));
         return null;
      }

      // if game does not exist
      if (!gamePath.exists())
      {
         listener.createFailed(new TorqueDebugException(
                                  "Game Path does not exist: " + gamePath.getAbsolutePath()));
         return null;
      }

      // create the options
      TorqueDebugOptions options = new TorqueDebugOptions(launch, host, port,
                                   password, gamePath);

      // create the instance
      TorqueDebug worker = new TorqueDebug(listener, options);

      // launch game if we were asked to do so
      if (options.launch)
      {
         // this will tell listener if we failed
         worker.gameProcess = launchGame(listener, options);
         if (worker.gameProcess == null)
         {
            return null;
         }
      }

      // create a worker thread and set it going
      Thread t = new Thread(worker);
      t.start();
      worker.thread = t;

      // return the object reference now that we have launched game
      return worker;
   }


   /**
    *  Description of the Method
    *
    *@param  listener  Description of the Parameter
    *@param  options   Description of the Parameter
    *@return           Description of the Return Value
    */
   public static synchronized TorqueDebug create(TorqueDebugListener listener,
         TorqueDebugOptions options)
   {
      TorqueDebug worker = TorqueDebug.create(listener, options.launch,
                                              options.host, options.port, options.password, options.gamePath);
      return worker;
   }


   // launch the game locally
   /**
    *  Description of the Method
    *
    *@param  listener  Description of the Parameter
    *@param  options   Description of the Parameter
    *@return           Description of the Return Value
    */
   public static Process launchGame(TorqueDebugListener listener,
                                    TorqueDebugOptions options) {
      Process gameProcess = null;

      // launch it!
      String[] gameCommand = new String[6];
      gameCommand[0] = options.gamePath.getAbsolutePath();
      gameCommand[1] = "-dbgEnable";
      gameCommand[2] = "-dbgPassword";
      gameCommand[3] = options.password;
      gameCommand[4] = "-dbgPort";
      gameCommand[5] = "" + options.port;

      Runtime rt = Runtime.getRuntime();
      try
      {
         gameProcess = rt.exec(gameCommand, null, options.gamePath.getParentFile());
      }
      catch (IOException ioe)
      {
         listener.createFailed(new TorqueDebugException("Unable to launch game: "
                               + options.gamePath.getAbsolutePath() + "\n" + "Error Message:"
                               + ioe.getMessage()));
         ioe.printStackTrace();
      }

      // OK...now at this point the game is just *starting* to launch but is
      // obviously no where near done. Certainly the telnet port is not yet open
      // and ready for debugging.

      // At this point we could execute Thread.sleep(someGodAwfulNumberOfSeconds)
      // and hope that the program was done launching by then, but this is
      // not the best we can do. Instead we just drop through at this point
      // and later on we allow the connect to fail for some number of seconds
      // before calling it a failure. The advantage to this is that the user
      // does not have to wait any longer than nessessary.

      return gameProcess;
   }


   // thread access
   /**
    *  Gets the thread attribute of the TorqueDebug object
    *
    *@return    The thread value
    */
   public Thread getThread() {
      return this.thread;
   }


   // game process access
   /**
    *  Gets the gameProcess attribute of the TorqueDebug object
    *
    *@return    The gameProcess value
    */
   public Process getGameProcess() {
      return this.gameProcess;
   }


   // state access
   /**
    *  Gets the paused attribute of the TorqueDebug object
    *
    *@return    The paused value
    */
   public boolean isPaused() {
      return this.paused;
   }


   /**
    *  Gets the compiling attribute of the TorqueDebug object
    *
    *@return    The compiling value
    */
   public boolean isCompiling() {
      return this.compiling;
   }


   /**
    *  Main processing method for the TorqueDebug object
    * runs in new thread
    */
   public void run() {
      // we must have listener
      if (listener == null)
      {
         return;
      }

      // OK...if we are launching the game locally then we allow the
      // connect to fail for some number of seconds before we call it
      // a failure. This outer loop lets us try to reconnect as many
      // times as we want
      startTime = System.currentTimeMillis();
      boolean doSleep = false;
      for (; ; )
      {
         if (doSleep)
         {
            System.out.println("TorqueDebug: retrying connect...");
            try
            {
               Thread.sleep(1000);
               // sleep for a sec
            }
            catch (Exception e)
            {
            }
            doSleep = false;
         }

         try
         {
            // main communicating loop...once we are connected this waits
            mainLoop();
         }
         catch (Exception e)
         {
            if (shuttingDown)
            {
               // if we are shutting down then ignore any erros
            }
            else if (!initialized)
            {
               // if we launched the game locally then allow a certain timeout
               // before we call the connect a failure
               long thisTime = System.currentTimeMillis();
               if (options.launch && thisTime < (startTime + options.connectTimeout))
               {
                  // we only retry to connect after this much time as elapsed
                  doSleep = true;
                  continue;
               }
               System.out.println("TorqueDebug: Create Error: " + e.getLocalizedMessage());
               listener.createFailed(e);
            }
            else
            {
               System.out.println("TorqueDebug: Error: " + e.getLocalizedMessage());
               listener.error(e);
            }
         }

         // we only have this loop for reconnect attempts...if we get this
         // far then we need to break out for real and let this thread die
         break;
      }
   }


   /**
    *  Description of the Method
    *
    *@exception  Exception  Description of the Exception
    */
   protected void mainLoop()
   throws Exception {
      sock = new Socket(options.host, options.port);
      w = new PrintWriter(sock.getOutputStream(), true);
      // autoFlush
      r = new BufferedReader(new InputStreamReader(sock.getInputStream()));

      // first thing we must send to TGE debugger is the password
      // we dont use rawCommand here because we are not yet 'connected'
      w.println(options.password);
      listener.rawInput(options.password);

      // now just loop forever reading lines of text from TGE and processing them.
      while (true)
      {
         String s = r.readLine();

         if (s != null)
         {
            processPacket(s);
         }
         else
         {
            break;
         }
      }

      // we going bye bye
      onDestroy();
   }


   // we need to disconnect and tell listener we are destroying ourselves
   /**
    *  Description of the Method
    *
    *@exception  Exception  Description of the Exception
    */
   protected void onDestroy()
   throws Exception {
      // we are shutting down so we can ignore any errrors from here on out
      shuttingDown = true;

      if (initialized)
      {
         initialized = false;
         // if we launched the game...kill that process
         if (gameProcess != null)
         {
            gameProcess.destroy();
            gameProcess = null;
         }
         listener.preDestroy();
      }

      // close connection
      paused = false;
      compiling = false;
      r.close();
      w.close();
      sock.close();
   }


   // cleanup incoming packet. trim it. replace backslashes with forward slashes
   // convert all to upper case?
   /**
    *  Description of the Method
    *
    *@param  s  Description of the Parameter
    *@return    Description of the Return Value
    */
   public String packetClean(String s) {
      String str = s.trim();
      str = str.replace('\\', '/');
      return str;
   }


   // NOTE: the original delphi code for Tribal IDE was smart about
   // packet params: it would not consider spaces (delims) inside
   // of double quotes as delimiters but would consider everything
   // between the starting and ending double quotes as one param
   // I dont think the StringTokenizer does this

   // return the given indexed param from packet (delimited by delim)
   /**
    *  Description of the Method
    *
    *@param  delim  Description of the Parameter
    *@param  index  Description of the Parameter
    *@param  s      Description of the Parameter
    *@return        Description of the Return Value
    */
   protected String packetParamStr(String delim, int index, String s) {
      StringTokenizer st = new StringTokenizer(s, delim);
      for (int i = 0; i != index; i++)
      {
         if (!st.hasMoreElements())
         {
            return "";
         }
         st.nextToken();
      }

      if (!st.hasMoreElements())
      {
         return "";
      }
      return st.nextToken();
   }


   // return true if given indexed param is equal to given string
   /**
    *  Description of the Method
    *
    *@param  s        Description of the Parameter
    *@param  index    Description of the Parameter
    *@param  compare  Description of the Parameter
    *@return          Description of the Return Value
    */
   protected boolean paramIs(String s, int index, String compare) {
      return packetParamStr(" ", index, s).equalsIgnoreCase(compare);
   }


   // return the given indexed param and everything after it
   /**
    *  Description of the Method
    *
    *@param  delim  Description of the Parameter
    *@param  index  Description of the Parameter
    *@param  s      Description of the Parameter
    *@return        Description of the Return Value
    */
   protected String packetRestStr(String delim, int index, String s) {
      // find the given delim
      int lastIndex = 0;

      for (int i = 0; i < index; i++)
      {
         // find delim from last
         lastIndex = s.indexOf(delim, lastIndex) + 1;
      }

      // now get the substring from this last index to end
      return s.substring(lastIndex);
   }


   // return true if given "rest" of string is equal to given string
   /**
    *  Description of the Method
    *
    *@param  s        Description of the Parameter
    *@param  index    Description of the Parameter
    *@param  compare  Description of the Parameter
    *@return          Description of the Return Value
    */
   protected boolean restIs(String s, int index, String compare) {
      return packetRestStr(" ", index, s).equalsIgnoreCase(compare);
   }


   // count the number of params in packet
   /**
    *  Description of the Method
    *
    *@param  delim  Description of the Parameter
    *@param  s      Description of the Parameter
    *@return        Description of the Return Value
    */
   protected int packetParamCount(String delim, String s) {
      StringTokenizer st = new StringTokenizer(s, delim);
      return st.countTokens();
   }


   // convert absolute filename to relative filename with unix style delims '/'
   /**
    *  Description of the Method
    *
    *@param  basePath  Description of the Parameter
    *@param  filename  Description of the Parameter
    *@return           Description of the Return Value
    */
   protected String extractUnixRelativePath(String basePath, String filename) {
      // No need to convert "\" to "/" as that was done during 'packet cleaning'.
      // Also the filenames are all sent as relative, so there does not
      // ever seem to be a need to pull off a basepath from an absolute path
      // to turn it into a relative path...at least that's my best guess as to
      // what the delphi code from Tribal IDE is doing
      return filename;
   }


   // process an incoming packet
   // logic for this method is cribbed from Tribal IDE's delphi code shown below
   /**
    *  Description of the Method
    *
    *@param  s  Description of the Parameter
    */
   protected void processPacket(String s) {
      // give it as raw output to listener
      listener.rawOutput(s);

      // process packet and generate more specific events for listener
      String clean_packet = packetClean(s);

      // password authenticated...................................................
      if (paramIs(clean_packet, 0, "PASS"))
      {
         if (restIs(clean_packet, 1, "WrongPassword."))
         {
            // bad password
            listener.createFailed(new TorqueDebugException("Incorrect Password"));
         }
         else if (restIs(clean_packet, 1, "Connected."))
         {
            // we are connected
            initialized = true;
            listener.connected();

            // we connected...request a list of scripts from server
            rawCommand("FILELIST");
         }
      }
      // received file list.......................................................
      else if (paramIs(clean_packet, 0, "FILELISTOUT"))
      {
         // how many files?
         int numFiles = packetParamCount(" ", clean_packet) - 1;

         // copy file list to an array of strings and tell listener
         if (numFiles > 0)
         {
            String[] list = new String[numFiles];
            StringTokenizer st = new StringTokenizer(clean_packet);
            st.nextToken();
            for (int i = 0; i < numFiles; i++)
            {
               list[i] = st.nextToken();
            }

            // tell listener
            listener.fileList(list);
         }
      }
      // hit breakpoint...........................................................
      else if (paramIs(clean_packet, 0, "BREAK"))
      {
         // get rest
         String str = packetRestStr(" ", 1, clean_packet);

         // create an array of strings giving callstack and tell listener
         int count = (packetParamCount(" ", str)) / 3;

         String[] filestack = new String[count];
         int[] numberstack = new int[count];
         String[] functionstack = new String[count];
         for (int i = 0; i < count; i++)
         {
            filestack[i] = packetParamStr(" ", (i * 3) + 0, str);
            numberstack[i] = Integer.parseInt(packetParamStr(" ", (i * 3) + 1, str));
            functionstack[i] = packetParamStr(" ", (i * 3) + 2, str);
         }

         // ok now we are paused
         paused = true;
         listener.paused(filestack[0], numberstack[0]);

         listener.callStack(filestack, numberstack, functionstack);

         String fileName = packetParamStr(" ", 1, clean_packet);
         String relFilename = extractUnixRelativePath(options.gamePath.getParent(),
                              fileName);

         // if we dont already have breakpoints for this file
         refreshBreakPoints(fileName);
      }
      // running again............................................................
      else if (paramIs(clean_packet, 0, "RUNNING"))
      {
         // no longer paused
         paused = false;
         listener.running();
      }
      // console output...........................................................
      else if (paramIs(clean_packet, 0, "COUT"))
      {
         // first show it on console
         listener.consoleOutput(packetRestStr(" ", 1, clean_packet));

         // need to add 'compiling' code here
      }
      // breaklist................................................................
      else if (paramIs(clean_packet, 0, "BREAKLISTOUT"))
      {
         // file in question
         String fileName = this.packetParamStr(" ", 1, clean_packet);

         // if we dont already have breakpoints for this file
         if (!files.containsKey(fileName))
         {
            // new list of breakpoints
            ArrayList breakList = new ArrayList();

            // this logic was from tribal IDE and does not suit the way we do
            // breakpoints...
            // delete any breakpoints we might already have that are not break
            // points the user wanted
            //deleteFilesNonSetBreakPoints(fileName);

            // create new breakpoints from incoming data
            setBreakPoints(fileName, breakList, packetRestStr(" ", 2, clean_packet));

            // now we do have breakpoints for this file...
            files.put(fileName, breakList);

            // now send "real" breakpoints to torque
            // this logic was from Tribal IDE but would seem to have no
            // effect since all new breakpoints are NOT active and NOT enabled
            resendBreakPoints(fileName, breakList, false);

            // tell listener
            listener.breakPointListChanged(fileName, breakList);
         }
      }
      // a watch value............................................................
      else if (paramIs(clean_packet, 0, "EVALOUT"))
      {
         // ID == -1 is used for generic variable evaluation
         int id = Integer.parseInt(packetParamStr(" ", 1, clean_packet));
         if (id == -1)
         {
            // evaluation
            String value = packetRestStr(" ", 2, clean_packet);

            // tell listener
            listener.evaluationReady(evaluationVariable, value);
         }
         else
         {
            // normal watch...
         }
      }
      // a list of watch sub items................................................
      else if (paramIs(clean_packet, 0, "OBJTAGLISTOUT"))
      {
      }
   }


   // ask for breakpoint info (unless we already have it)
   /**
    *  Description of the Method
    *
    *@param  fileName  Description of the Parameter
    */
   public void refreshBreakPoints(String fileName) {
      if (!files.containsKey(fileName))
      {
         rawCommand("BREAKLIST " + fileName);
      }
   }


   // clear all breakpoints from all files
   /**
    *  Description of the Method
    */
   public void removeAllFiles() {
      files.clear();
   }


   // remove breakpoint data for the given file
   // return true if this file was removed
   /**
    *  Description of the Method
    *
    *@param  fileName  Description of the Parameter
    *@return           Description of the Return Value
    */
   public boolean removeFile(String fileName) {
      ArrayList breakList = (ArrayList) files.remove(fileName);
      if (breakList == null)
      {
         return false;
      }
      breakList.clear();
      return true;
   }


   // create new breakpoints from incoming data
   /**
    *  Sets the breakPoints attribute of the TorqueDebug object
    *
    *@param  fileName     The new breakPoints value
    *@param  breakList    The new breakPoints value
    *@param  breakPoints  The new breakPoints value
    */
   public void setBreakPoints(String fileName, ArrayList breakList, String breakPoints) {
      int count = Integer.parseInt(packetParamStr(" ", 0, breakPoints));

      int lineNumber = 1;

      for (int i = 0; i < count; i++)
      {
         int skip = Integer.parseInt(packetParamStr(" ", (i * 2) + 1, breakPoints));
         int num = Integer.parseInt(packetParamStr(" ", (i * 2) + 2, breakPoints));

         // skip ahead the given amount
         lineNumber += skip;

         // add all the breakpoints...
         for (int j = 0; j < num; j++)
         {
            addBreakPoint(fileName, breakList, lineNumber, "", 0, "");
            lineNumber++;
         }
      }
   }


   // find a breakpoint by filename/linenumber
   /**
    *  Description of the Method
    *
    *@param  fileName    Description of the Parameter
    *@param  lineNumber  Description of the Parameter
    *@return             Description of the Return Value
    */
   public TorqueBreakPoint findBreakPoint(String fileName, int lineNumber) {
      ArrayList breakList = getBreakPointList(fileName);
      if (breakList == null)
      {
         return null;
      }
      return findBreakPoint(fileName, breakList, lineNumber);
   }


   /**
    *  Description of the Method
    *
    *@param  fileName    Description of the Parameter
    *@param  breakList   Description of the Parameter
    *@param  lineNumber  Description of the Parameter
    *@return             Description of the Return Value
    */
   public TorqueBreakPoint findBreakPoint(String fileName, ArrayList breakList, int lineNumber) {
      Iterator iter = breakList.iterator();
      while (iter.hasNext())
      {
         TorqueBreakPoint bp = (TorqueBreakPoint) iter.next();
         if (bp.fileName.equals(fileName) && bp.lineNumber == lineNumber)
         {
            return bp;
         }
      }

      return null;
   }


   // add a breakpoint (if there is not already one at this line)...return bp at this line
   /**
    *  Adds a feature to the BreakPoint attribute of the TorqueDebug object
    *
    *@param  fileName    The feature to be added to the BreakPoint attribute
    *@param  breakList   The feature to be added to the BreakPoint attribute
    *@param  lineNumber  The feature to be added to the BreakPoint attribute
    *@param  condition   The feature to be added to the BreakPoint attribute
    *@param  passCount   The feature to be added to the BreakPoint attribute
    *@param  group       The feature to be added to the BreakPoint attribute
    *@return             Description of the Return Value
    */
   public TorqueBreakPoint addBreakPoint(String fileName, ArrayList breakList, int lineNumber,
                                         String condition, int passCount, String group) {
      TorqueBreakPoint bp = findBreakPoint(fileName, breakList, lineNumber);
      if (bp == null)
      {
         bp = new TorqueBreakPoint(fileName, lineNumber, false, condition,
                                   passCount, group);
         breakList.add(bp);

         return bp;
      }

      return bp;
   }


   // now send "real" breakpoints to torque from array list
   /**
    *  Description of the Method
    *
    *@param  fileName   Description of the Parameter
    *@param  breakList  Description of the Parameter
    *@param  invert     Description of the Parameter
    */
   public void resendBreakPoints(String fileName, ArrayList breakList, boolean invert) {
      Iterator iter = breakList.iterator();
      while (iter.hasNext())
      {
         TorqueBreakPoint bp = (TorqueBreakPoint) iter.next();
         if (bp.active && bp.fileName.equals(fileName))
         {
            if (bp.enabled)
            {
               // set/clear breakpoint
               setBreakPoint(bp, fileName, bp.lineNumber, !invert);
            }
            else
            {
               // if breakpoint disabled we always send the clr breakpoint cmd
               setBreakPoint(bp, fileName, bp.lineNumber, false);
            }
         }
      }
   }


   // now send "real" breakpoints to torque from enumeration
   /**
    *  Description of the Method
    *
    *@param  fileName  Description of the Parameter
    *@param  e         Description of the Parameter
    *@param  invert    Description of the Parameter
    */
   public void resendBreakPoints(String fileName, Enumeration e, boolean invert) {
      while (e.hasMoreElements())
      {
         TorqueBreakPoint bp = (TorqueBreakPoint) e.nextElement();
         if (bp.active && bp.fileName.equals(fileName))
         {
            if (bp.enabled)
            {
               // set/clear breakpoint
               setBreakPoint(bp, fileName, bp.lineNumber, !invert);
            }
            else
            {
               // if breakpoint disabled we always send the clr breakpoint cmd
               setBreakPoint(bp, fileName, bp.lineNumber, false);
            }
         }
      }
   }


   // set a single breakpoint by sending command to torque
   /**
    *  Sets the breakPoint attribute of the TorqueDebug object
    *
    *@param  bp                The new breakPoint value
    *@param  fileName          The new breakPoint value
    *@param  lineNumber        The new breakPoint value
    *@param  breakPointStatus  The new breakPoint value
    */
   public void setBreakPoint(TorqueBreakPoint bp, String fileName,
                             int lineNumber, boolean breakPointStatus) {
      // sposedly we convert path to unix relative...but it already is
      // so there is nothing to do there

      if (breakPointStatus && bp.enabled)
      {
         String sendCondition;
         if (bp.condition.equals(""))
         {
            sendCondition = "1";
         }
         // convert "" to true
         else
         {
            sendCondition = bp.condition;
         }

         // send command to toruqe
         rawCommand("BRKSET " + fileName + " " + lineNumber + " 0 " + bp.passCount
                    + " " + sendCondition);
      }
      else
      {
         rawCommand("BRKCLR " + fileName + " " + lineNumber);
      }
   }



   // change active status for a breakpoint
   /**
    *  Sets the active attribute of the TorqueDebug object
    *
    *@param  bp      The new active value
    *@param  active  The new active value
    */
   public void setActive(TorqueBreakPoint bp, boolean active) {
      // if not already
      if (bp.active != active)
      {
         bp.active = active;
         bp.enabled = true;

         // if breakpoint being removed...reset condition/passcount/etc
         if (!bp.active)
         {
            bp.condition = "";
            bp.passCount = 0;
            //bp.enabled = true;
            bp.group = "";
            bp.disableAfterHit = false;
         }

         // tell torque about it
         setBreakPoint(bp, bp.fileName, bp.lineNumber, bp.active);
      }
   }


   // Logic for above method is cribbed from this delphi code from Tribal IDE
   /*
    *  { process the packet }
    *  procedure Tform_main.ProcessPacket ( inPacket : string );
    *  var
    *  clean_packet : string;
    *  i            : integer;
    *  editor_index : integer;
    *  project_index : integer;
    *  line_number  : integer;
    *  original_packet : string;
    *  watch_id  : integer;
    *  watch_index : integer;
    *  compile_end : string;
    *  breakpoint_index : integer;
    *  call_stack_count : integer;
    *  filename : string;
    *  begin
    *  original_packet := Trim( inPacket );
    *  clean_packet := StringReplace( original_packet, '/','\', [rfIgnoreCase, rfReplaceAll]);
    *  /AddMessage('!'+clean_packet+'!');
    *  { password authenticated ----------------------------------------------------}
    *  if ( PacketParamStr(' ',0, clean_packet) = 'PASS' ) then
    *  begin
    *  if ( PacketRestStr(' ',1, clean_packet) = 'WrongPassword.' ) then
    *  begin
    *  { bad password }
    *  AddMessage('Invalid Password');
    *  end
    *  else if ( PacketRestStr(' ',1, clean_packet) = 'Connected.' ) then
    *  begin
    *  { set the fact we are now debuging }
    *  server_debugging := True;
    *  { as we have just connected, we are not stepping through code }
    *  debugging_step := False;
    *  { password ok }
    *  AddMessage('Password Accepted');
    *  AddMessage('Requesting Server Script List');
    *  { request list of the scripts on server }
    *  SendPacket('FILELIST');
    *  end;
    *  end
    *  { Received Filelist ---------------------------------------------------------}
    *  else if ( PacketParamStr(' ',0, clean_packet) = 'FILELISTOUT' ) then
    *  begin
    *  debug_file_list.clear;
    *  { add files to string list }
    *  for i :=1 to PacketParamCount(' ',clean_packet)-1 do
    *  begin
    *  debug_file_list.Add(PacketParamStr(' ',i-1, clean_packet));
    *  end;
    *  project_list.UpdateIcons;
    *  { get breakpoint for current active }
    *  OnActiveChange ( editor_list.CurrentActive );
    *  end
    *  { we have hit a breakpoint --------------------------------------------------}
    *  else if ( PacketParamStr(' ',0, clean_packet) = 'BREAK' ) then
    *  begin
    *  { can now step through code }
    *  debugging_step := True;
    *  { clear hover watch }
    *  last_variable := '';
    *  { get all the set watches }
    *  GetWatches;
    *  { fill callstack }
    *  call_stack_count := (PacketParamCount(' ', clean_packet)-1) div 4;
    *  form_callstack.eltree_callstack.items.Clear;
    *  for i := 0 to call_stack_count-1 do
    *  begin
    *  AddCallStack (Format('%s (%s): %s',[
    *  PacketParamStr(' ', (i*3)+1, clean_packet),
    *  PacketParamStr(' ', (i*3)+2, clean_packet),
    *  PacketParamStr(' ', (i*3)+3, clean_packet)]));
    *  end;
    *  { open the file were stopped in }
    *  editor_index := project_list.OpenRelativeFilename ( PacketParamStr(' ',1, clean_packet) );
    *  if ( editor_index <> -1 ) then
    *  begin
    *  { get line number }
    *  line_number := StrToInt(PacketParamStr(' ',2, clean_packet));
    *  { set stepping cursor }
    *  debugger_list.SteppingAtLine := line_number ;
    *  debugger_list.SteppingAtFile := editor_list.editors[editor_index].filename;
    *  { goto correct line number }
    *  with editor_list[editor_index] do
    *  begin
    *  GotoLine( line_number );
    *  end;
    *  { was this a breakpoint ? }
    *  breakpoint_index := debugger_list.FindBreakPoint(
    *  editor_list.editors[editor_index].filename,
    *  line_number);
    *  if ( breakpoint_index <> -1 ) then
    *  begin
    *  { if this was a clear after hit then clear it }
    *  if ( debugger_list[breakpoint_index].DisableAfterHit ) and
    *  ( debugger_list[breakpoint_index].Enabled ) and
    *  ( debugger_list[breakpoint_index].Active ) then
    *  begin
    *  debugger_list[breakpoint_index].Enabled := False;
    *  end;
    *  end;
    *  filename := ExtractUnixRelativePath(project_list.BasePath, editor_list.editors[editor_index].filename);
    *  SendPacket(Format('BREAKLIST %s',[filename]));
    *  Application.BringToFront;
    *  end;
    *  end
    *  { we started the process again ----------------------------------------------}
    *  else if ( PacketParamStr(' ',0, clean_packet) = 'RUNNING' ) then
    *  begin
    *  { can now not step through code }
    *  debugging_step := False;
    *  { clear cursor watch }
    *  statusbar_main.Panels[3].Text := '';
    *  end
    *  { output from console -------------------------------------------------------}
    *  else if ( PacketParamStr(' ',0, clean_packet) = 'COUT' ) then
    *  begin
    *  AddConsole(PacketRestStr(' ',1, clean_packet));
    *  { we are compiling arent we? }
    *  if ( doing_compile ) then
    *  begin
    *  { finished so exec }
    *  if ( PacketRestStr(' ',1, clean_packet) = MSG_COMPILATION_COMPLETE ) then
    *  begin
    *  doing_compile := false;
    *  ExecCompiledFiles;
    *  end
    *  else
    *  { compiling a file }
    *  if ( PacketParamStr(' ',1, clean_packet) = 'Compiling' ) then
    *  begin
    *  compile_end := PacketRestStr(' ',2, clean_packet);
    *  { remove the ... }
    *  compile_end := Copy(compile_end, 1, length(compile_end)-3);
    *  current_file_compiling := compile_end;
    *  end
    *  else
    *  { one of the errors starts with exec: }
    *  if ( PacketParamStr(' ',1, clean_packet) = 'exec:') then
    *  begin
    *  { invalid script file }
    *  AddMessage(PacketRestStr(' ',2, clean_packet));
    *  end
    *  else
    *  begin
    *  { syntax error  format of filename Line: 000 - Syntax error
    *  we convert it to filename (000): Syntax error }
    *  AddMessage(ConvertError(PacketRestStr(' ',1, clean_packet)));
    *  end;
    *  end;
    *  { breakpoint list }
    *  else if ( PacketParamStr(' ',0, clean_packet) = 'BREAKLISTOUT' ) then
    *  begin
    *  { find file }
    *  project_index := project_list.HasRelativeFilename (  PacketParamStr(' ',1, clean_packet) );
    *  if ( project_index <> -1 ) then
    *  begin
    *  if ( not project_list[project_index].BreakPointsGot ) and
    *  ( editor_list.IsOpen( project_list[project_index].filename ) <> -1 ) then
    *  begin
    *  { we have the breakpoints for this file }
    *  project_list[project_index].BreakPointsGot := True;
    *  debugger_list.DeleteFilesNonSetBreakPoints( project_list[project_index].filename );
    *  { set break points }
    *  debugger_list.SetBreakPoints( project_list[project_index].filename, PacketRestStr(' ',2, clean_packet));
    *  { this might be an update to an already open file so resend our valid breakpoints }
    *  debugger_list.ResendBreakPoints ( project_list[project_index].filename, false );
    *  { only allow incoming breakpoints on open files  }
    *  { open the file were stopped in }
    *  editor_index := project_list.OpenRelativeFilename ( PacketParamStr(' ',1, clean_packet) );
    *  { update editor }
    *  if ( editor_index <> -1 ) then
    *  editor_list[editor_index].Editor.Invalidate;
    *  end;
    *  end;
    *  end
    *  { A watch value  ------------------------------------------------------------}
    *  else if ( PacketParamStr(' ',0, clean_packet) = 'EVALOUT' ) then
    *  begin
    *  watch_id := strtoint(PacketParamStr(' ',1, clean_packet));
    *  if ( watch_id <> -1 ) then
    *  begin
    *  { now find this watch }
    *  watch_index := watch_list.FindwatchViaID( watch_id );
    *  if ( watch_index <> -1 ) and ( watch_list[watch_index].Parent = nil ) then
    *  begin
    *  watch_list[watch_index].value :=PacketRestStr(' ',2, clean_packet)
    *  end;
    *  end
    *  else
    *  begin
    *  if ( timer_end_watches_update.enabled ) then
    *  begin
    *  timer_end_watches_update.enabled := false;
    *  form_watches.eltree_watches.items.EndUpdate;
    *  end;
    *  statusbar_main.Panels[3].Text := last_variable +' = '+PacketRestStr(' ',2, clean_packet);
    *  end;
    *  end
    *  { A list of watch sub items  ------------------------------------------------}
    *  else if ( PacketParamStr(' ',0, clean_packet) = 'OBJTAGLISTOUT' ) then
    *  begin
    *  watch_id := strtoint(PacketParamStr(' ',1, clean_packet));
    *  if ( watch_id <> -1 ) then
    *  begin
    *  { now find this watch }
    *  watch_index := watch_list.FindwatchViaID( watch_id );
    *  if ( watch_index <> -1 ) then
    *  begin
    *  watch_list.AddSubWatches( watch_list[watch_index], PacketRestStr(' ',2, clean_packet) );
    *  system_expand := True;
    *  try
    *  watch_list[watch_index].ELTreeItem.Expanded := True;
    *  finally
    *  system_expand := False;
    *  end;
    *  end;
    *  end;
    *  end;
    *  end;
    */

   // don't call this method directly...it is for sending RAW commands
   // to TGE. Instead call a command method which will call this method.
   /**
    *  Description of the Method
    *
    *@param  s  Description of the Parameter
    */
   public void rawCommand(String s) {
      if (!initialized)
      {
         return;
      }
      w.println(s);
      listener.rawInput(s);
   }


   /**
    *  Description of the Method
    */
   public void destroy() {
      // if we launched we could tell it to shutdown cleanly...but it shuts down
      // faster if we destroy the process...so lets just always do that

      // also before we destroy lets disconnect

      // disconnect
      try
      {
         onDestroy();
      }
      catch (Exception e)
      {
         // we are destroying...if socket close fails or some such
         // just ignore it
      }
   }


   /**
    *  Description of the Method
    */
   public void continueExecution() {
      rawCommand("CONTINUE");
   }


   /**
    *  Description of the Method
    */
   public void stepIn() {
      rawCommand("STEPIN");
   }


   /**
    *  Description of the Method
    */
   public void stepOver() {
      rawCommand("STEPOVER");
   }


   /**
    *  Description of the Method
    */
   public void stepOut() {
      rawCommand("STEPOUT");
   }


   // get breakpoint list for given filename...return null if none
   /**
    *  Gets the breakPointList attribute of the TorqueDebug object
    *
    *@param  fileName  Description of the Parameter
    *@return           The breakPointList value
    */
   public ArrayList getBreakPointList(String fileName) {
      ArrayList breakList = (ArrayList) files.get(fileName);
      return breakList;
   }


   // toggle breakpoint for given file at given line
   /**
    *  Description of the Method
    *
    *@param  fileName    Description of the Parameter
    *@param  lineNumber  Description of the Parameter
    *@return             Description of the Return Value
    */
   public boolean toggleBreakPoint(String fileName, int lineNumber) {
      ArrayList breakList = getBreakPointList(fileName);
      if (breakList == null)
      {
         return false;
      }
      return toggleBreakPoint(fileName, breakList, lineNumber);
   }


   /**
    *  Description of the Method
    *
    *@param  fileName    Description of the Parameter
    *@param  breakList   Description of the Parameter
    *@param  lineNumber  Description of the Parameter
    *@return             Description of the Return Value
    */
   public boolean toggleBreakPoint(String fileName, ArrayList breakList, int lineNumber) {
      // first make sure such a line is breakable
      TorqueBreakPoint bp = findBreakPoint(fileName, breakList, lineNumber);
      if (bp == null)
      {
         return false;
      }

      // make toggle this breakpoint from active/not
      setActive(bp, !bp.active);

      // success
      return true;
   }


   // evaluate this variable
   /**
    *  Description of the Method
    *
    *@param  variable  Description of the Parameter
    */
   public void evaluate(String variable) {
      // save this reference
      evaluationVariable = variable;

      // fire off a command to evaluate this using ID == -1
      rawCommand("EVAL -1 0 " + variable);
   }
}

