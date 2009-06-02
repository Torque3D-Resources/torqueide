/*
 *  Tide.java
 *  Copyright (c) 2002 Paul Dana
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 */
package com.garagegames.torque.tide;

import com.garagegames.torque.*;
import com.garagegames.torque.tidedebug.TideDebugCallstackViewer;
import com.garagegames.torque.tidedebug.CallstackEntryListModel;
import com.garagegames.torque.tidedebug.TideDebugLog;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;
import javax.swing.*;

import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.buffer.BufferAdapter;
import org.gjt.sp.jedit.buffer.BufferChangeListener;
import org.gjt.sp.jedit.buffer.BufferListener;
import org.gjt.sp.jedit.buffer.JEditBuffer;
import org.gjt.sp.jedit.gui.DockableWindowManager;
import org.gjt.sp.jedit.io.VFSManager;
import org.gjt.sp.jedit.msg.BufferUpdate;
import org.gjt.sp.jedit.msg.EditPaneUpdate;
import org.gjt.sp.jedit.msg.PositionChanging;
import org.gjt.sp.jedit.msg.ViewUpdate;
import org.gjt.sp.jedit.pluginmgr.*;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.jedit.textarea.Selection;
import org.gjt.sp.jedit.textarea.TextArea;
import org.gjt.sp.util.Log;

import projectviewer.*;
import projectviewer.config.*;
import projectviewer.importer.*;
import projectviewer.vpt.*;

// handles common low level Tide functionality
// including interfacing with ProjectViewer
// and launching the game executable
// and communicating with the Torque telnet debugger
/**
 *  Description of the Class
 *
 *@author     beffy
 *@created    27. November 2006
 */
public class Tide
       implements TorqueDebugListener, EBComponent {
   // state
   /**
    *  Description of the Field
    */
   public final static int NOTCONNECTED = 0;
   /**
    *  Description of the Field
    */
   public final static int RUNNING = 1;
   /**
    *  Description of the Field
    */
   public final static int PAUSED = 2;

   // messages
   /**
    *  Description of the Field
    */
   public final static String PatchMessage =
         "The game you specified needs to be patched\n" +
         "to support the -dbgEnable command line option.\n" +
         "If you do not patch this game then Tide will be\n" +
         "unable to automatically launch your game from.\n" +
         "the debugger. Do you wish to patch your game now?";
   /**
    *  Description of the Field
    */
   public final static String PatchTitle = "Patch Game?";

   // the tide plugin itself that created us
   /**
    *  Description of the Field
    */
   protected TidePlugin tidePlugin;

   // we are a singleton
   /**
    *  Description of the Field
    */
   protected static Tide instance;

   // if this is null then we are not connected
   /**
    *  Description of the Field
    */
   protected TorqueDebug torqueDebug;

   // this is the currently open tide project. if null we dont have one
   /**
    *  Description of the Field
    */
   protected static VPTProject currentProject;

   // these are the current project options (for now just torquedebug options)
   /**
    *  Description of the Field
    */
   protected TorqueDebugOptions projectOptions;

   // we keep track of breakpoint info with hashtable of TideFileInfo keyed by filename
   /**
    *  Description of the Field
    */
   protected Hashtable files = new Hashtable();

   // these are the listeners
   /**
    *  Description of the Field
    */
   protected Vector torqueStateListeners = new Vector();
   /**
    *  Description of the Field
    */
   protected Vector torqueLineListeners = new Vector();
   /**
    *  Description of the Field
    */
   protected Vector torqueEvaluationListeners = new Vector();
   /**
    *  Description of the Field
    */
   protected Vector breakInfoListeners = new Vector();

   // when we are evaluating NOW we use these ...
   /**
    *  Description of the Field
    */
   protected Thread currentThread;
   /**
    *  Description of the Field
    */
   protected String evaluateNowVariable;
   /**
    *  Description of the Field
    */
   protected String evaluateNowValue;
   
   private BufferChangeListener bufferListener;
   private boolean addedBufferChangeHandler;
   private int lineToScrollTo = 0;

   // private constructor
   /**
    *  Constructor for the Tide object
    *
    *@param  plugin  Description of the Parameter
    */
   private Tide(TidePlugin plugin) {
      this.tidePlugin = plugin;
      
      this.bufferListener = new BufferChangeListener();
      EditBus.addToBus(this);
   }

   void dispose()
   {
	   EditBus.removeFromBus(this);
   }
   
   // create code has package level access...called from the plugin
   /**
    *  Description of the Method
    *
    *@param  plugin  Description of the Parameter
    *@return         Description of the Return Value
    */
   static Tide createTide(TidePlugin plugin) {
      if(instance == null) {
         instance = new Tide(plugin);
      }
      return instance;
   }


   // we are singleton...only way to get access publicly:
   /**
    *  Gets the instance attribute of the Tide class
    *
    *@return    The instance value
    */
   public static Tide getInstance() {
      return instance;
   }


   // get tide menu
   /**
    *  Gets the tideMenu attribute of the Tide class
    *
    *@return    The tideMenu value
    */
   public static JMenu getTideMenu() {
      if(instance == null || instance.tidePlugin == null) {
         return null;
      }
      return instance.tidePlugin.getTideMenu();
   }


   // add a state listener
   /**
    *  Adds a feature to the TorqueStateListener attribute of the Tide object
    *
    *@param  listener  The feature to be added to the TorqueStateListener
    *      attribute
    */
   public void addTorqueStateListener(TideTorqueStateListener listener) {
      if(listener != null) {
         torqueStateListeners.add(listener);
      }
   }


   // remove a state listener
   /**
    *  Description of the Method
    *
    *@param  listener  Description of the Parameter
    */
   public void removeTorqueStateListener(TideTorqueStateListener listener) {
      if(listener != null) {
         torqueStateListeners.remove(listener);
      }
   }


   // tell listeners about a change of state
   /**
    *  Description of the Method
    *
    *@param  state  Description of the Parameter
    */
   public void fireStateChanged(int state) {
      for(int i = 0; i < torqueStateListeners.size(); i++) {
         TideTorqueStateListener listener = (TideTorqueStateListener) torqueStateListeners.get(i);
         if(listener != null) {
            listener.stateChanged(state);
         }
      }
   }


   // add an evaluation listener
   /**
    *  Adds a feature to the TorqueEvaluationListener attribute of the Tide
    *  object
    *
    *@param  listener  The feature to be added to the TorqueEvaluationListener
    *      attribute
    */
   public void addTorqueEvaluationListener(TideTorqueEvaluationListener listener) {
      if(listener != null) {
         torqueEvaluationListeners.add(listener);
      }
   }


   // remove an evaluation listener
   /**
    *  Description of the Method
    *
    *@param  listener  Description of the Parameter
    */
   public void removeTorqueEvaluationListener(TideTorqueEvaluationListener listener) {
      if(listener != null) {
         torqueEvaluationListeners.remove(listener);
      }
   }


   // tell listeners evaluation is ready
   /**
    *  Description of the Method
    *
    *@param  variable  Description of the Parameter
    *@param  value     Description of the Parameter
    */
   public void fireEvaluationReady(String variable, String value) {
      for(int i = 0; i < torqueEvaluationListeners.size(); i++) {
         TideTorqueEvaluationListener listener = (TideTorqueEvaluationListener)
               torqueEvaluationListeners.get(i);
         if(listener != null) {
            listener.evaluationReady(variable, value);
         }
      }
   }


   // add a state listener
   /**
    *  Adds a feature to the TorqueLineListener attribute of the Tide object
    *
    *@param  listener  The feature to be added to the TorqueLineListener
    *      attribute
    */
   public void addTorqueLineListener(TideTorqueLineListener listener) {
      if(listener != null) {
         torqueLineListeners.add(listener);
      }
   }


   // remove a state listener
   /**
    *  Description of the Method
    *
    *@param  listener  Description of the Parameter
    */
   public void removeTorqueLineListener(TideTorqueLineListener listener) {
      if(listener != null) {
         torqueLineListeners.remove(listener);
      }
   }


   // tell listeners about a new line number
   /**
    *  Description of the Method
    *
    *@param  fileName    Description of the Parameter
    *@param  lineNumber  Description of the Parameter
    */
   public void fireLineChanged(String fileName, int lineNumber) {
      for(int i = 0; i < torqueLineListeners.size(); i++) {
         TideTorqueLineListener listener = (TideTorqueLineListener) torqueLineListeners.get(i);
         if(listener != null) {
            listener.lineChanged(fileName, lineNumber);
         }
      }
   }


   // add a break info listener
   /**
    *  Adds a feature to the BreakInfoListener attribute of the Tide object
    *
    *@param  listener  The feature to be added to the BreakInfoListener
    *      attribute
    */
   public void addBreakInfoListener(TideBreakInfoListener listener) {
      if(listener != null) {
         breakInfoListeners.add(listener);
      }
   }


   // remove a break info listener
   /**
    *  Description of the Method
    *
    *@param  listener  Description of the Parameter
    */
   public void removeBreakInfoListener(TideBreakInfoListener listener) {
      if(listener != null) {
         breakInfoListeners.remove(listener);
      }
   }


   // tell listeners about a change in break info for a given file
   /**
    *  Description of the Method
    *
    *@param  fileInfo  Description of the Parameter
    */
   public void fireBreakInfoChanged(TideFileInfo fileInfo) {
      for(int i = 0; i < breakInfoListeners.size(); i++) {
         TideBreakInfoListener listener = (TideBreakInfoListener) breakInfoListeners.get(i);
         if(listener != null) {
            listener.breakInfoChanged(fileInfo);
         }
      }
   }


   // get the current state of the currently open tide project
   /**
    *  Gets the state attribute of the Tide object
    *
    *@return    The state value
    */
   public int getState() {
      if(torqueDebug == null) {
         return NOTCONNECTED;
      }
      if(torqueDebug.isPaused()) {
         return PAUSED;
      }
      return RUNNING;
   }


   // get buffered reader for this file which exists as a resource
   // in the same folder as the class files. jarFile and parentFolder are
   // provided as a failsafe incase for some reason we cannot
   // get to the file as a resource. In this case the file be searched for
   // explicitly in the parent folder of the given .jar file
   /**
    *  Gets the bufferedReader attribute of the Tide object
    *
    *@param  parent         Description of the Parameter
    *@param  name           Description of the Parameter
    *@param  jarFile        Description of the Parameter
    *@return                The bufferedReader value
    *@exception  Exception  Description of the Exception
    */
   private BufferedReader getBufferedReader(String parent, String name, ZipFile jarFile)
      throws Exception {
      return getBufferedReader(parent + "/" + name, jarFile);
   }


   /**
    *  Gets the bufferedReader attribute of the Tide object
    *
    *@param  name           Description of the Parameter
    *@param  jarFile        Description of the Parameter
    *@return                The bufferedReader value
    *@exception  Exception  Description of the Exception
    */
   private BufferedReader getBufferedReader(String name, ZipFile jarFile)
      throws Exception {
      Log.log(Log.DEBUG, this, "Trying to read file:" + name);
      BufferedReader br = null;
      InputStream istream = getClass().getResourceAsStream(name);
      if(istream == null) {
         // if we fail to get it as a resource...read from .jar directly
         String entryName;
         if(name.startsWith("/")) {
            entryName = name.substring(1);
         }
         else {
            entryName = name;
         }
         ZipEntry entry = null;
         if(jarFile != null) {
            entry = jarFile.getEntry(entryName);
         }
         if(entry == null) {
            // as a completely silly fallback just read from current folder
            br = new BufferedReader(new FileReader("." + name));
         }
         else {
            // otherwise use the entry
            br = new BufferedReader(new InputStreamReader(jarFile.getInputStream(entry)));
         }
      }
      else {
         br = new BufferedReader(new InputStreamReader(istream));
      }

      return br;
   }


   // get reference to our own .jar file
   /**
    *  Gets the tideJarFile attribute of the Tide object
    *
    *@return    The tideJarFile value
    */
   public ZipFile getTideJarFile() {
      // get jedit folder
      //String jeditFolder = System.getProperty("jedit.home");
      String jeditFolder = jEdit.getJEditHome();
      if(jeditFolder == null) {
         return null;
      }

      // get it as zip file
      ZipFile file = null;
      try {
         file = new ZipFile(new File(jeditFolder, "jars/Tide.jar"));
      }
      catch(IOException ioe) {
      }

      return file;
   }


   // get array of strings each representing a page of info to display
   /**
    *  Gets the alphaTestingInfoFromFile attribute of the Tide object
    *
    *@return    The alphaTestingInfoFromFile value
    */
   public String[] getAlphaTestingInfoFromFile() {
      Vector infoVector = new Vector();

      String parent = "/com/garagegames/torque/tide";
      String[] name = new String[2];
      name[0] = "TideAlphaTestingInfoA.txt";
      name[1] = "TideAlphaTestingInfoB.txt";

      // as a failsafe we can search in our jar specifically
      ZipFile jar = getTideJarFile();

      for(int i = 0; i < name.length; i++) {
         String info = new String();
         try {
            BufferedReader br = getBufferedReader(parent, name[i], jar);
            String line;
            while((line = br.readLine()) != null) {
               info = info + line + "\n";
            }
         }
         catch(IOException ioe) {
         }
         catch(Exception e) {
         }
         infoVector.add(info);
      }

      String[] infoArray = new String[infoVector.size()];
      for(int i = 0; i < infoVector.size(); i++) {
         infoArray[i] = (String) infoVector.get(i);
      }

      return infoArray;
   }


   /**
    *  Description of the Method
    *
    *@param  view  Description of the Parameter
    */
   public static void displayAlphaTestingInfo(View view) {
      Tide tide = getInstance();
      String[] info = null;
      if(tide != null) {
         info = tide.getAlphaTestingInfoFromFile();
      }

      if(info == null) {
         info = new String[1];
         info[0] = "Cannot find TIDE Information";
      }

      // display it
      for(int i = 0; i < info.length; i++) {
         JOptionPane.showMessageDialog(view, info[i]);
      }
   }


   /**
    *  Description of the Method
    *
    *@param  aView  Description of the Parameter
    */
   public static void about(View aView) {
      String version = jEdit.getProperty("plugin.com.garagegames.torque.tide.TidePlugin.version");
      String author = jEdit.getProperty("plugin.com.garagegames.torque.tide.TidePlugin.author");
      int option = JOptionPane.showConfirmDialog(aView, "Tide ver " + version + " by " + author + "\n\n" +
            "Do you want to see TIDE Information?", "About", JOptionPane.YES_NO_OPTION);
      if(option != JOptionPane.YES_OPTION) {
         return;
      }
      displayAlphaTestingInfo(aView);
   }


   // provide control over currently opened tide project
   /**
    *  Description of the Method
    *
    *@param  aView  Description of the Parameter
    */
   public void start(View aView) {
      if(!refreshCurrentProject()) {
         return;
      }

      // if we are not connected
      if(torqueDebug == null) {
         // first we must save all open buffers that belong to our project
         // this will cause the correct files to be recompiled when running
         // this (potentially changed) version of the game...

         // ok now try to establish connection and optionally launch the game
         torqueDebug = TorqueDebug.create(this, projectOptions);
      }
      else {
         if(torqueDebug.isPaused()) {
            torqueDebug.continueExecution();
         }
      }

   }


   /**
    *  Description of the Method
    */
   public void stop() {
      if(!refreshCurrentProject()) {
         return;
      }

      // this will invoke preDestroy() on listener which will
      // give us a chance to null our reference
      torqueDebug.destroy();
   }


   /**
    *  Description of the Method
    */
   public void pause() {
      if(!refreshCurrentProject()) {
         return;
      }
      torqueDebug.stepIn();
   }


   /**
    *  Description of the Method
    */
   public void stepIn() {
      if(!refreshCurrentProject()) {
         return;
      }
      torqueDebug.stepIn();
   }


   /**
    *  Description of the Method
    */
   public void stepOut() {
      if(!refreshCurrentProject()) {
         return;
      }
      torqueDebug.stepOut();
   }


   /**
    *  Description of the Method
    */
   public void stepOver() {
      if(!refreshCurrentProject()) {
         return;
      }
      torqueDebug.stepOver();
   }


   /**
    *  Description of the Method
    *
    *@param  aView  Description of the Parameter
    */
   public void runToCursor(View aView) {
      if(!refreshCurrentProject()) {
         return;
      }

      JOptionPane.showMessageDialog(aView, "This features is not yet implemented.\n");
      // i think this is implemented as a
      // 'clear when reached' breakpoint
      // we need to know about the currently open file and
      // the carat position to do this
   }


   // if file does not belong to project root then return null
   // otherwise convert to unix style separator and
   // if present remove project root from front of filename
   /**
    *  Description of the Method
    *
    *@param  name  Description of the Parameter
    *@return       Description of the Return Value
    */
   public String makeUnixRelative(String name) {
      if(currentProject == null) {
         return null;
      }

      String s = name.replace('\\', '/').toLowerCase();
      //String root = currentProject.getRoot().getPath();
      String root = currentProject.getRootPath();
      root = root.replace('\\', '/').toLowerCase();
      if(!s.startsWith(root)) {
         return null;
      }

      s = s.substring(root.length());
      if(s.startsWith("/")) {
         s = s.substring(1);
      }
      return s;
   }


   // toggle breakpoint at line...if the given filename is absolute
   // then make it relative to the project root
   /**
    *  Description of the Method
    *
    *@param  view  Description of the Parameter
    *@return       Description of the Return Value
    */
   public boolean toggleBreakPoint(View view) {
      if(!refreshCurrentProject()) {
         return false;
      }

      // get current file and line number
      Buffer buffer = view.getBuffer();
      String absName = buffer.getPath();
      //.getFile().getAbsolutePath();
      int lineNumber = view.getTextArea().getCaretLine() + 1;

      // get this filename relative to project root
      // if this file does not belong to the project at all
      // then this will return null and we should disallow setting breakpoint
      String fileName = makeUnixRelative(absName);
      if(fileName == null) {
         return false;
      }

      // toggle that breakpoint
      if(toggleBreakPoint(fileName, lineNumber)) {
         view.getTextArea().invalidateLine(lineNumber - 1);
         return true;
      }

      return false;
   }


   // toggle breakpoint at given filename/linenumber
   /**
    *  Description of the Method
    *
    *@param  fileName    Description of the Parameter
    *@param  lineNumber  Description of the Parameter
    *@return             Description of the Return Value
    */
   public boolean toggleBreakPoint(String fileName, int lineNumber) {
      // update our notion of this breakpoint
      TideFileInfo fileInfo = getFileInfo(fileName);
      TorqueBreakPoint bp = fileInfo.getBreak(lineNumber);
      if(bp == null) {
         bp = new TorqueBreakPoint(fileName, lineNumber);
         fileInfo.addBreak(bp);
      }

      // if we are connected...only toggle if its a valid line...
      if(torqueDebug != null) {
         // if torque says this aint a valid line...then do nothing
         if(!torqueDebug.toggleBreakPoint(fileName, lineNumber)) {
            // make a 'ding' noise here?
            return false;
         }
      }

      // toggle OUR breakpoint (remember they are separate from TorqueDebug's breakpoint list
      bp.active = !bp.active;

      // if active breakpoint went inactive....
      if(!bp.active) {
         // if we are not connected just remove it...
         if(torqueDebug == null) {
            fileInfo.removeBreak(bp.key);
         }
         else {
            // just as failsafe...if this is not a breakable line then remove it
            if(torqueDebug.findBreakPoint(fileName, lineNumber) == null) {
               fileInfo.removeBreak(bp.key);
            }
            else {
               bp.enabled = false;
            }
         }
      }
      else {
         bp.enabled = true;
      }

      // tell listeners that we have a change of breakpoint status
      fireBreakInfoChanged(fileInfo);

      return true;
   }


   // retreive file info or make a new one and return that
   /**
    *  Gets the fileInfo attribute of the Tide object
    *
    *@param  fileName  Description of the Parameter
    *@return           The fileInfo value
    */
   public TideFileInfo getFileInfo(String fileName) {
      TideFileInfo fileInfo = (TideFileInfo) files.get(fileName);
      if(fileInfo == null) {
         fileInfo = new TideFileInfo(fileName);
         files.put(fileName, fileInfo);
      }
      return fileInfo;
   }


   // find breakpoint (if any) in this file at this line number
   /**
    *  Description of the Method
    *
    *@param  absName     Description of the Parameter
    *@param  lineNumber  Description of the Parameter
    *@return             Description of the Return Value
    */
   public TorqueBreakPoint findBreakPoint(String absName, int lineNumber) {
      // if we dont have a current project then quickly say NO
      if(currentProject == null) {
         return null;
      }

      // make this unix relative...if this file dont belong to project...quickly say NO
      String fileName = makeUnixRelative(absName);
      if(fileName == null) {
         return null;
      }

      // if we dont have breakpoint info on this file then just say NO quick
      TideFileInfo fileInfo = (TideFileInfo) files.get(fileName);
      if(fileInfo == null) {
         return null;
      }

      // find breakpoint
      TorqueBreakPoint bp = fileInfo.getBreak(lineNumber);
      return bp;
   }

   public static boolean hasProjectChanged() {
      // is there a project viewer?
      ProjectViewer viewer = ProjectViewer.getViewer(jEdit.getActiveView());
      if(viewer == null) {
         return false;
      }

      //Project project = viewer.getCurrentProject();
      View actView = jEdit.getActiveView();
      VPTProject project = ProjectViewer.getActiveProject(actView);
      if(project == null && ProjectViewer.getViewer(actView).getSelectedNode() != null)
    	  project = VPTNode.findProjectFor(ProjectViewer.getViewer(actView).getSelectedNode());

      if(project == null)
		  return false;

      if(currentProject != project)
      {
    	  currentProject = project;
    	  return true;
      }
	      
      return false;
   }
   
   // update our concept of the current tide project
   // if necessary force the current tide project to be the current project
   // return true upon success
   /**
    *  Description of the Method
    *
    *@return    Description of the Return Value
    */
   public boolean refreshCurrentProject() {
      // is there a project viewer?
      ProjectViewer viewer = ProjectViewer.getViewer(jEdit.getActiveView());
      if(viewer == null) {
		 JOptionPane.showMessageDialog(null,"Please open the Project Manager and select a project first!");
         return false;
      }

      //Project project = viewer.getCurrentProject();
      View actView = jEdit.getActiveView();
      VPTProject project = ProjectViewer.getActiveProject(actView);
      if(project == null && ProjectViewer.getViewer(actView).getSelectedNode() != null)
    	  project = VPTNode.findProjectFor(ProjectViewer.getViewer(actView).getSelectedNode());

      if(project == null)
      {
    	  if(currentProject == null)
    	  {
    		  JOptionPane.showMessageDialog(null,"Please select a project first!");
    		  return false;
    	  }
      }
      if(currentProject != project)
      {
    	  currentProject = project;
      }

      File projProps = new File(project.getRootPath(), "TideProject.properties");
      if(!readProjectProperties(project, projProps)) {
		  JOptionPane.showMessageDialog(null,"No TIDE project selected!");
		  return false; // no TIDE project!
      }
      
      /*
      // if we have not opened a project...try to read from
      // the top project viewer project
      if(currentProject == null) {
         // we dont know what current project is and there
         // is no projects defined
         if(project == null) {
            return false;
         }

         // we dont have a current project attempt to read options from this one
         File projProps = new File(project.getRootPath(), "TideProject.properties");
         if(!readProjectProperties(project, projProps)) {
            return false;
         }

         // this is now the current project
         currentProject = project;
      }
      else if(!currentProject.equals(project)) {
         // if the user has changed projects on us then
         // let's change it back to what WE think should be current
         //beffy: viewer.setCurrentProject( project );
         //viewer.setProject(project);
    	  viewer.setRootNode(project);
      }
      */

      return true;
   }


   /**
    *  Gets the projectViewerInstance attribute of the Tide class
    *
    *@param  view  Description of the Parameter
    *@return       The projectViewerInstance value
    */
   public static ProjectViewer getProjectViewerInstance(View view) {
      // if there is one already...return that
      ProjectViewer viewer = ProjectViewer.getViewer(view);
      if(viewer != null) {
         return viewer;
      }

      // otherwise we make one!
      viewer = new ProjectViewer(view);
      return viewer;
   }


   /**
    *  Description of the Method
    *
    *@param  aView  Description of the Parameter
    */
   public static void newProject(View aView) {
      if(Tide.instance == null) {
         return;
      }
      // is there a project viewer?
      ProjectViewer viewer = getProjectViewerInstance(aView);
      if(viewer == null) {
         return;
      }
      Tide.instance.newProject(viewer);
   }


   /**
    *  Description of the Method
    *
    *@param  viewer  Description of the Parameter
    */
   public void newProject(ProjectViewer viewer) {
      // create a new project
      NewProjectOptions opt = NewProjectDialog.showDialog(null, "New Project");
      if(opt == null) {
         return;
      }

      // get my jar file
      ZipFile jarFile = getTideJarFile();

      // does this game need patching
      TorqueDebugPatcher patcher = new TorqueDebugPatcher(opt.options.gamePath);
      try {
         if(!patcher.hasDebugOptions()) {
            if(JOptionPane.showConfirmDialog(null, PatchMessage, PatchTitle,
                  JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
               patcher.patchGame(this.getClass(), jarFile);
            }
         }
      }
      catch(Exception e) {
         //JOptionPane.showMessageDialog(null,"Error: " + e.getLocalizedMessage());
    	 Log.log(Log.ERROR, this, "Error: " + e.getLocalizedMessage());
         return;
      }

      createProject(viewer, opt);
   }


   // create a new project
   /**
    *  Description of the Method
    *
    *@param  viewer  Description of the Parameter
    *@param  opt     Description of the Parameter
    */
   public void createProject(ProjectViewer viewer, NewProjectOptions opt) {
      // new way is to create a properties file right in the game path
      File projProps = new File(opt.options.gamePath.getParent(),
            "TideProject.properties");

      // now write our info into this file
      if(!writeProjectProperties(projProps, opt.options)) {
    	 Log.log(Log.ERROR, this, "Error creating project properites file: " + projProps.getAbsolutePath());
         return;
      }

      // these are our options now
      projectOptions = new TorqueDebugOptions(opt.options);

      // now make a new project
      /*
       *  projectviewer.NewProjectOptions newOptions = new projectviewer.NewProjectOptions();
       *  newOptions.setPromptForImport(false);
       *  newOptions.setPromptForOptions(false);
       *  newOptions.setProjectName(opt.projectName);
       *  newOptions.setRoot(opt.options.gamePath.getParentFile());
       *  / we want to config this project's allowed file types
       *  ProjectViewerConfig config = ProjectViewerConfig.getInstance();
       *  String savedExts = config.getImportExts();
       *  config.setImportExts("cs cfg dml gui hfl mis txt log");
       *  currentProject = viewer.createProject(newOptions);
       *  config.setImportExts(savedExts);
       */
      //beffy: new project
      VPTGroup tmpGroup = (VPTGroup) ((viewer.getSelectedNode() == null) ? viewer.getRoot() : viewer.getSelectedNode());//;//new VPTGroup("All Projects");
      VPTProject tmpProject = new VPTProject(opt.projectName);
      tmpProject.setRootPath(opt.options.gamePath.getParent());
      currentProject = ProjectOptions.run(tmpProject);
      if(currentProject != null) {
         ProjectManager.getInstance().addProject(currentProject, tmpGroup);
         RootImporter ipi = new RootImporter(currentProject, null, viewer, jEdit.getActiveView());
         ipi.doImport();
         //viewer.setProject(currentProject);
         viewer.setRootNode(currentProject);
         try
         {
        	 ProjectManager.getInstance().saveProjectList();
        	 //ProjectManager.getInstance().save();
         }
         catch(Exception ioex)
         {
        	 Log.log(Log.ERROR, this, ioex.getMessage());
         }
      }
   }


   // create torque debug options from properties
   // return null on error
   /**
    *  Gets the optionsFromProperties attribute of the Tide object
    *
    *@param  projPath  Description of the Parameter
    *@param  props     Description of the Parameter
    *@return           The optionsFromProperties value
    */
   public TorqueDebugOptions getOptionsFromProperties(String projPath, Properties props) {
      String gameName = props.getProperty("gameExecutable");
      String launch = props.getProperty("launch");
      String host = props.getProperty("host");
      String port = props.getProperty("port");
      String password = props.getProperty("password");
      if(gameName == null || launch == null || host == null || port == null || password == null) {
         return null;
      }

      TorqueDebugOptions opt = new TorqueDebugOptions();
      opt.gamePath = new File(projPath, gameName);
      opt.host = host;
      opt.port = Integer.parseInt(port);
      if(launch.trim().equalsIgnoreCase("true")) {
         opt.launch = true;
      }
      else {
         opt.launch = false;
      }
      opt.password = password;

      return opt;
   }


   // read project properties file...at some point read everything with this
   // return true if we have sucessfully set the data member 'projectOptions'
   // only call when currentProject is valid
   /**
    *  Description of the Method
    *
    *@param  project  Description of the Parameter
    *@param  f        Description of the Parameter
    *@return          Description of the Return Value
    */
   public boolean readProjectProperties(VPTProject project, File f) {
      if(f == null) {
         return false;
      }
      if(!f.exists()) {
         return false;
      }

      Properties props = new Properties();

      // read file
      try {
         FileInputStream fis = new FileInputStream(f);
         props.load(fis);
      }
      catch(FileNotFoundException fexception) {
         return false;
      }
      catch(IOException fexception) {
         return false;
      }

      if(project == null) {
         return false;
      }
      String dir = project.getRootPath();
      if(dir == null) {
         return false;
      }

      // create options...for now we only have torquedebug options
      projectOptions = getOptionsFromProperties(dir, props);
      if(projectOptions == null) {
         return false;
      }

      return true;
   }


   // create properties from options
   /**
    *  Gets the propertiesFromOptions attribute of the Tide object
    *
    *@param  opt  Description of the Parameter
    *@return      The propertiesFromOptions value
    */
   public Properties getPropertiesFromOptions(TorqueDebugOptions opt) {
      Properties props = new Properties();

      props.setProperty("gameExecutable", opt.gamePath.getName());
      props.setProperty("launch", opt.launch ? "true" : "false");
      props.setProperty("host", opt.host);
      props.setProperty("port", Integer.toString(opt.port));
      props.setProperty("password", opt.password);

      return props;
   }


   // write project properties file
   // at some point we will write all info into this...not just proj props
   /**
    *  Description of the Method
    *
    *@param  f    Description of the Parameter
    *@param  opt  Description of the Parameter
    *@return      Description of the Return Value
    */
   public boolean writeProjectProperties(File f, TorqueDebugOptions opt) {
      // write this information out...we assume the game executable and the
      // file we are writing are in the same parent folder so we only write
      // the NAME of the executable..not the full path
      Properties props = getPropertiesFromOptions(opt);

      try {
         FileOutputStream fos = new FileOutputStream(f);
         props.store(fos, "TideProject");
      }
      catch(FileNotFoundException fexception) {
         return false;
      }
      catch(IOException fexception) {
         return false;
      }

      return true;
   }


   // make sure given file is opened and that given line number is in view
   /**
    *  Description of the Method
    *
    *@param  fileName    Description of the Parameter
    *@param  lineNumber  Description of the Parameter
    *@return             Description of the Return Value
    */
   public boolean openAndScrollTo(String fileName, final int lineNumber) {
      // is there a project viewer?
      /*
       *  ProjectViewer viewer = ProjectViewer.getViewer(jEdit.getActiveView());
       *  if (viewer == null)
       *  return false;
       */
      // now update our concept of the current project
      if(refreshCurrentProject()) {
    	  // TODO: fix scrolling!!!
    	 this.lineToScrollTo = lineNumber;
         File tmpFile = new File(currentProject.getRootPath(), fileName);
         View actView = jEdit.getActiveView();

         // check if this buffer is already open
         Buffer _buffer = jEdit.getBuffer(tmpFile.getPath());
         if(_buffer != null)
         {
        	 actView.setBuffer(_buffer, false);
        	 scrollTo(lineNumber);
        	 return true;
         }

         Buffer lastBuf = null;
         lastBuf = jEdit.openFile(actView, tmpFile.getPath());
         // Wait for the buffer to load
         if(!lastBuf.isLoaded())
        	 VFSManager.waitForRequests();
         
         if(lastBuf != null) {
            if(lineNumber > -1) {
               EditPane ep = actView.getEditPane();
               if(ep != null) {
            	  actView.setBuffer(lastBuf, false);
            	  ep.setBuffer(lastBuf, true);
            	  scrollTo(lineNumber);
            	  /*
            	  lastBuf.addBufferListener(new BufferListener(){

					public void bufferLoaded(JEditBuffer buf) {
						Log.log(Log.DEBUG, this, "Buffer loaded: " + buf.getLineText(1));
					}

					public void contentInserted(JEditBuffer arg0, int arg1,
							int arg2, int arg3, int arg4) {
						// TODO Auto-generated method stub
						
					}

					public void contentRemoved(JEditBuffer arg0, int arg1,
							int arg2, int arg3, int arg4) {
						// TODO Auto-generated method stub
						
					}

					public void foldHandlerChanged(JEditBuffer arg0) {
						// TODO Auto-generated method stub
						
					}

					public void foldLevelChanged(JEditBuffer arg0, int arg1,
							int arg2) {
						// TODO Auto-generated method stub
						
					}

					public void preContentInserted(JEditBuffer arg0, int arg1,
							int arg2, int arg3, int arg4) {
						// TODO Auto-generated method stub
						
					}

					public void preContentRemoved(JEditBuffer arg0, int arg1,
							int arg2, int arg3, int arg4) {
						// TODO Auto-generated method stub
						
					}

					public void transactionComplete(JEditBuffer arg0) {
						// TODO Auto-generated method stub
						
					}
            	  });
            	  */
               }
               else {
            	   Log.log(Log.ERROR, this, "No active EditPane!?");
            	   return false;
               }
            }
         }
         else {
        	 Log.log(Log.ERROR, this, "No active buffer!?");
        	 return false;
         }
      }
      return true;
   }


   public boolean scrollTo(int lineNumber) {
	   return scrollTo(lineNumber, null);
   }
   /**
    *  Description of the Method
    *
    *@param  lineNumber  Description of the Parameter
    *@return             Description of the Return Value
    */
   public boolean scrollTo(int lineNumber, Selection.Range selRange) {
      // is there a project viewer?

      ProjectViewer viewer = ProjectViewer.getViewer(jEdit.getActiveView());
      if(viewer == null) {
     	 Log.log(Log.ERROR, this, "No project active?!");
         return false;
      }

      View actView = jEdit.getActiveView();
      JEditTextArea textArea = actView.getTextArea();
      if(textArea != null) {
         int height = textArea.getLineCount();

         Buffer currBuf = actView.getBuffer();
		 int charsToLine = currBuf.getLineStartOffset(lineNumber - 1);	
         Log.log(Log.DEBUG, this, "Scrolling TextArea to: " + lineNumber + " height: " + height);
         if(lineNumber < height) 
         {
        	if(selRange != null)
        	{
	        	textArea.setSelection(selRange);
	            textArea.moveCaretPosition(selRange.getStart());
        	}
        	else
        	{
	            textArea.moveCaretPosition(charsToLine);
        	}
            textArea.scrollTo(lineNumber, 0, true);
            Log.log(Log.DEBUG, this, "Successfully scrolled to: " + lineNumber);
            return true;
         }
      }
      else {
    	  Log.log(Log.ERROR, this, "No active textarea!?");
      }
      return false;
   }

   // evaluate a variable...result will be reported to evaluation listener
   /**
    *  Description of the Method
    *
    *@param  variable  Description of the Parameter
    */
   public void evaluate(String variable) {
      if(torqueDebug == null) {
         return;
      }

      torqueDebug.evaluate(variable);
   }


   // evaluate a variable and BLOCK (for up to 2 seconds) waiting for the
   // result to be ready...return result when ready (or null if error)
   // we can do this because WE are a listener as well
   /**
    *  Description of the Method
    *
    *@param  variable  Description of the Parameter
    *@return           Description of the Return Value
    */
   public String evaluateNow(String variable) {
      if(torqueDebug == null) {
         return null;
      }

      // flag that this is an IMMEDIATE evaluation that is not to be
      // reported to the listeners
      evaluateNowValue = null;
      evaluateNowVariable = new String(variable);

      // evaluate this
      torqueDebug.evaluate(evaluateNowVariable);

      // now block for up to 2 seconds waiting for the result to come back
      currentThread = Thread.currentThread();
      // sleep for up to 2 seconds awaiting the reply
      try {
         Thread.sleep(2000);
      }
      catch(InterruptedException e) {
         // we have been interrupted by the listener thread
         // telling us that the info is ready!
      }

      // get value
      String value = null;
      if(evaluateNowValue != null) {
         value = new String(evaluateNowValue);
      }
      evaluateNowValue = null;
      evaluateNowVariable = null;

      // return this value
      return value;
   }


   // we have connected so we want to correctly setup the breakpoint
   // info now that we know which lines are valid breakpoint lines
   /**
    *  Sets the connectedBreakpointInfo attribute of the Tide object
    */
   public void setConnectedBreakpointInfo() {
      if(torqueDebug == null) {
         return;
      }

      // lets try telling torque about our ACTIVE breakpoints
      for(Enumeration e = files.elements(); e.hasMoreElements(); ) {
         TideFileInfo info = (TideFileInfo) e.nextElement();
         // tell tide about our active breakpoints
         torqueDebug.resendBreakPoints(info.name, info.hash.elements(), false);
         // ask for break list info for this file
         torqueDebug.rawCommand("BREAKLIST " + info.name);
      }
   }


   /**
    *  recompileFile recompiles the current buffer
    *
    *@param  filePath  file system path
    */
   public void recompileFile(String filePath) {
      if(torqueDebug == null) {
         return;
      }
      if(filePath != null) {
         // strip path
         String projPath = projectOptions.gamePath.getParent();
         if(projPath != null && filePath.indexOf(projPath) != -1) {
            filePath = filePath.substring(filePath.indexOf(projPath) + projPath.length() + 1, filePath.length());
         }
         // replace backslashes
         filePath = torqueDebug.packetClean(filePath);
         Log.log(Log.DEBUG, this, "Trying to recompile file: " + filePath);
         // send the compile command to the engine
         torqueDebug.rawCommand("CEVAL compile(\"" + filePath + "\");");
      }
   }

   /**
    * Sends a console command to the engine
    * @param cmd The full command including a ";"
    */
   public void sendConsoleCmd(String cmd) {
      if(torqueDebug == null) {
         return;
      }
      if(cmd != null && cmd.length() > 0 && cmd.endsWith(";")) {
         // send the command to the engine
         torqueDebug.rawCommand("CEVAL " + cmd);
      }
   }
   
   // we have disconnected so we want to correctly setup the breakpoint
   // info by removing breakpoints not set by user and clearing all
   // 'illegal' flags from user-set breakpoints
   /**
    *  Sets the disconnectedBreakpointInfo attribute of the Tide object
    */
   public void setDisconnectedBreakpointInfo() {
      for(Enumeration e = files.elements(); e.hasMoreElements(); ) {
         TideFileInfo info = (TideFileInfo) e.nextElement();
         info.removeDisabledClearIllegal();
         fireBreakInfoChanged(info);
      }
   }


   //
   //
   // TORQUE DEBUG LISTENER interface...
   //

   // called if creation itself fails
   // the TorqueDebug object reference should be
   // nulled by the implementor of this interface when
   // this method is called.
   /**
    *  Description of the Method
    *
    *@param  e  Description of the Parameter
    */
   public void createFailed(Exception e) {
      // tell user to run game before running this app
	  Log.log(Log.ERROR, this, "Tide: TorqueDebug Create Error: " + e.getLocalizedMessage());
      String msg = "This error received connecting to Torque Debugger:\n\n" +
            "    " + e.getLocalizedMessage() + "\n";
      JOptionPane.showMessageDialog(null, msg);
      torqueDebug = null;
   }


   // called when an error occurs
   /**
    *  Description of the Method
    *
    *@param  e  Description of the Parameter
    */
   public void error(Exception e) {
      // tell user to run game before running this app
	  Log.log(Log.ERROR, this, "Tide: TorqueDebug Error: " + e.getLocalizedMessage());
      String msg = "This error received from TorqueDebug:\n\n" +
            "    " + e.getLocalizedMessage() + "\n";
      JOptionPane.showMessageDialog(null, msg);
   }


   // called when a new output string is received from TGE
   /**
    *  Description of the Method
    *
    *@param  s  Description of the Parameter
    */
   public void rawOutput(String s) {
   }


   // called when a new input string is sent to TGE
   /**
    *  Description of the Method
    *
    *@param  s  Description of the Parameter
    */
   public void rawInput(String s) {
   }


   // called when program quits or connection is lost
   // the TorqueDebug object reference should be
   // nulled by the implementor of this interface when
   // this method is called.
   /**
    *  Description of the Method
    */
   public void preDestroy() {
      // as per interface docs we must null our reference here...
      // this also marks us as "not connected"
      torqueDebug = null;

      // we are disconnected so we should remove all 'transient'
      // breakpoints and remove all 'illegal' flags from user set
      // breakpoints...they will be set again when next run
      setDisconnectedBreakpointInfo();

      // tell listener the state has changed
      fireStateChanged(Tide.NOTCONNECTED);
   }


   // called when connection has been established
   /**
    *  Description of the Method
    */
   public void connected() {
      // ok we have connected for the first time...we
      // must tell torque about where we want breakpoints
      // and we must findout about which lines are valid for breakpoints
      setConnectedBreakpointInfo();

      // tell listener the state has changed
      fireStateChanged(Tide.RUNNING);
   }


   // called when script execution paused
   /**
    *  Description of the Method
    *
    *@param  fileName    Description of the Parameter
    *@param  lineNumber  Description of the Parameter
    */
   public void paused(String fileName, int lineNumber) {
      // tell listener the state has changed
      fireStateChanged(Tide.PAUSED);

      // tell listeners about change of line number/filename
      fireLineChanged(fileName, lineNumber);
   }


   // called when script execution is running
   /**
    *  Description of the Method
    */
   public void running() {
      // tell listener the state has changed
      fireStateChanged(Tide.RUNNING);
   }


   // called when a script file list received from TGE
   // all the files in this list are "active" as far as
   // TGE is concerned
   /**
    *  Description of the Method
    *
    *@param  list  Description of the Parameter
    */
   public void fileList(String[] list) {
   }


   // called when new callstack available
   // NOTE: paused() is called *before* this method
   /**
    *  Description of the Method
    *
    *@param  filestack      Description of the Parameter
    *@param  numberstack    Description of the Parameter
    *@param  functionstack  Description of the Parameter
    */
   public void callStack(String[] filestack, int[] numberstack,
         String[] functionstack) {
	   
	   //DockableWindowManager wm = jEdit.getActiveView().getDockableWindowManager();
	   //TideDebugCallstackViewer callStackViewer = (TideDebugCallstackViewer)wm.getDockable("tide-callstack-viewer");
	   CallstackEntryListModel listModel = (CallstackEntryListModel)TideDebugCallstackViewer.getCallstackEntryListModel();
	   listModel.removeAll();
	   for(int i=0; i<functionstack.length; i++)
	   {
		   String path = "" + filestack[i];
		   listModel.addElement(new TideDebugCallstackViewer.CallstackEntry(path, functionstack[i], numberstack[i]));
	   }
   }


   // console output
   /**
    *  Description of the Method
    *
    *@param  s  Description of the Parameter
    */
   public void consoleOutput(String s) {
	   TideDebugLog.log(Log.NOTICE, this, s);
   }


   // called when breakpoint list has been updated
   /**
    *  Description of the Method
    *
    *@param  fileName   Description of the Parameter
    *@param  breakList  Description of the Parameter
    */
   public void breakPointListChanged(String fileName, Collection breakList) {
      // update out notion of what is legal, etc
      TideFileInfo fileInfo = getFileInfo(fileName);

      // merge in fresh information from TorqueDebug about which lines are breakable
      // first we clear any prevoiusly 'breakable' lines
      // then we mark all user defined (enabled) breakpoins as ILLEGAL
      // then we merge in the new 'breakable' line information
      // if a user defined breakpoint is found to be on a 'breakable' line then we
      // mark it as LEGAL.
      fileInfo.mergeNewBreakPointInfo(breakList);

      // tell listeners about it
      this.fireBreakInfoChanged(fileInfo);
   }


   // evaluated variable is ready
   /**
    *  Description of the Method
    *
    *@param  variable  Description of the Parameter
    *@param  value     Description of the Parameter
    */
   public void evaluationReady(String variable, String value) {
      // if this was an IMMEDIATE evaluation then just set this
      // fact and do NOT report to listeners
      if(variable.equals(evaluateNowVariable)) {
         evaluateNowValue = value;

         // wake up the thread that is waiting for this info
         if(currentThread != null) {
            currentThread.interrupt();
         }
         return;
      }

      fireEvaluationReady(variable, value);
   }

	private void handleBufferUpdate(BufferUpdate bmsg)
	{
		if (bmsg.getWhat() == BufferUpdate.LOADED) 
		{
			//scrollTo(this.lineToScrollTo);
		}
		
	}
	public void handleMessage(EBMessage msg) {
		if(msg instanceof BufferUpdate)
			handleBufferUpdate((BufferUpdate)msg);
		
	}

	private void addBufferChangeListener(Buffer buffer)
	{
		if(!addedBufferChangeHandler)
		{
			buffer.addBufferListener(bufferListener = new BufferChangeListener());
			addedBufferChangeHandler = true;
		}
	}
	private void removeBufferChangeListener(Buffer buffer)
	{
		if(addedBufferChangeHandler)
		{
			buffer.removeBufferListener(bufferListener);
			addedBufferChangeHandler = false;
		}
	}   
	
	class BufferChangeListener extends BufferAdapter {

		@Override
		public void bufferLoaded(JEditBuffer buffer) {
			// TODO Auto-generated method stub
			super.bufferLoaded(buffer);
		}

		public void contentInserted(JEditBuffer buffer, int startLine, int offset, int numLines, int length)
		{
		}

		public void contentRemoved(JEditBuffer buffer, int startLine, int offset, int numLines, int length)
		{
		}
		
	
	}   
}


