/*
 *  TideDebug.java
 *  Copyright (c) 2002 Paul Dana
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 */
package com.garagegames.torque.tidedebug;

import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.msg.EditPaneUpdate;
import org.gjt.sp.jedit.msg.ViewUpdate;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.util.Log;

import com.garagegames.torque.tide.*;

import javax.swing.*;

// singleton for the TideDebug plugin
/**
 *  Description of the Class
 *
 *@author     beffy
 *@created    26. August 2004
 */
public class TideDebug {
   /**
    *  Description of the Field
    */
   public final static String version = "0.14";

   // the tidedebug plugin itself that created us
   /**
    *  Description of the Field
    */
   protected TideDebugPlugin tideDebugPlugin;

   // we are a singleton
   /**
    *  Description of the Field
    */
   protected static TideDebug instance;

   // the tide singleton
   /**
    *  Description of the Field
    */
   protected Tide tide;


   // private constructor
   /**
    *  Constructor for the TideDebug object
    *
    *@param  plugin  Description of the Parameter
    *@param  tide    Description of the Parameter
    */
   private TideDebug(TideDebugPlugin plugin, Tide tide) {
      this.tideDebugPlugin = plugin;
      this.tide = tide;
   }


   // create code has package level access...called from the plugin
   /**
    *  Description of the Method
    *
    *@param  plugin  Description of the Parameter
    *@param  tide    Description of the Parameter
    *@return         Description of the Return Value
    */
   static TideDebug createTideDebug(TideDebugPlugin plugin, Tide tide) {
      if (instance == null) {
         instance = new TideDebug(plugin, tide);
      }
      return instance;
   }


   // we are singleton...only way to get access publicly:
   /**
    *  Gets the instance attribute of the TideDebug class
    *
    *@return    The instance value
    */
   public static TideDebug getInstance() {
      return instance;
   }


   // about box
   /**
    *  Description of the Method
    *
    *@param  aView  Description of the Parameter
    */
   public static void about(View aView) {
      String version = jEdit.getProperty("plugin.com.garagegames.torque.tidedebug.TideDebugPlugin.version");
      String author = jEdit.getProperty("plugin.com.garagegames.torque.tidedebug.TideDebugPlugin.author");
      JOptionPane.showMessageDialog(aView, "TideDebug ver " + version + " by " + author + "\n\n");
   }


   // provide control over currently opened tide project
   /**
    *  Description of the Method
    *
    *@param  aView  Description of the Parameter
    */
   public static void start(View aView) {
      Tide tide = Tide.getInstance();
      if (tide == null) {
         return;
      }
      tide.start(aView);
   }


   /**
    *  Description of the Method
    *
    *@param  aView    Description of the Parameter
    *@param  aBuffer  Description of the Parameter
    */
   public static void recompileFile(View aView, Buffer aBuffer) {
      Tide tide = Tide.getInstance();
      if (tide == null) {
         return;
      }
      String filePath = aBuffer.getPath();
      if (filePath != null) {
         tide.recompileFile(filePath);
      }
   }


   /**
    *  Description of the Method
    */
   public static void stop() {
      Tide tide = Tide.getInstance();
      if (tide == null) {
         return;
      }
      tide.stop();
   }


   /**
    *  Description of the Method
    */
   public static void pause() {
      Tide tide = Tide.getInstance();
      if (tide == null) {
         return;
      }
      tide.pause();
   }


   /**
    *  Description of the Method
    */
   public static void stepIn() {
      Tide tide = Tide.getInstance();
      if (tide == null) {
         return;
      }
      tide.stepIn();
   }


   /**
    *  Description of the Method
    */
   public static void stepOut() {
      Tide tide = Tide.getInstance();
      if (tide == null) {
         return;
      }
      tide.stepOut();
   }


   /**
    *  Description of the Method
    */
   public static void stepOver() {
      Tide tide = Tide.getInstance();
      if (tide == null) {
         return;
      }
      tide.stepOver();
   }


   /**
    *  Description of the Method
    *
    *@param  view  Description of the Parameter
    */
   public static void runToCursor(View view) {
      Tide tide = Tide.getInstance();
      if (tide == null) {
         return;
      }
      tide.runToCursor(view);
   }


   /**
    *  Description of the Method
    *
    *@param  view  Description of the Parameter
    */
   public static void toggleBreakpoint(View view) {
      Log.log(Log.ERROR, null, "Toggle it!!!");
      Tide tide = Tide.getInstance();
      if (tide == null) {
         Log.log(Log.ERROR, null, "TIDE is null!!!");
         return;
      }
      tide.toggleBreakPoint(view);
   }
}


