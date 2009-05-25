/*
 *  DebugToolbar.java - debugger toolbar
 *  Copyright (c) 2002 Paul Dana
 *  some GUI code patterned after the java Debugger plugin by Dirk Moebius
 *
 *  :tabSize=4:indentSize=4:noTabs=false:maxLineLen=0:
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.garagegames.torque.tidedebug;

import java.awt.Container;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.util.Log;

import com.garagegames.torque.tide.*;
import java.awt.Frame;

/**
 *  A toolbar for the debugger.
 *
 *@author     Paul Dana...GUI cribbed from java Debugger plugin by Dirk Moebius
 *@created    20. Juni 2004
 */
public class TideDebugToolbar extends JToolBar
       implements TideTorqueStateListener {
   private View view;
   private JButton start;
   private JButton stop;
   private JButton pause;
   private JButton stepInto;
   private JButton stepOver;
   private JButton stepOut;
   private JButton runToCursor;
   private JButton toggleBreakpoint;
   private JButton addWatch;
   private JButton removeWatch;

   private Tide tide;
   private TideDebug tideDebug;


   /**
    *  Constructor for the TideDebugToolbar object
    *
    *@param  view       Description of the Parameter
    *@param  tide       Description of the Parameter
    *@param  tideDebug  Description of the Parameter
    */
   public TideDebugToolbar(View view, Tide tide, TideDebug tideDebug) {
      super();
      this.view = view;
      this.tide = tide;
      this.tideDebug = tideDebug;

      //JToolBar toolbar = GUIUtilities.loadToolBar("tidedebug.toolbar");
      //Box toolbar = GUIUtilities.loadToolBar("tidedebug.toolbar");
      JPanel toolbar = (JPanel)GUIUtilities.loadToolBar("tidedebug.toolbar");

      /*
       *  start = (JButton) toolbar.getComponentAtIndex(0);
       *  pause = (JButton) toolbar.getComponentAtIndex(1);
       *  stop = (JButton) toolbar.getComponentAtIndex(2);
       *  stepInto = (JButton) toolbar.getComponentAtIndex(4);
       *  stepOver = (JButton) toolbar.getComponentAtIndex(5);
       *  runToCursor = (JButton) toolbar.getComponentAtIndex(6);
       *  stepOut = (JButton) toolbar.getComponentAtIndex(7);
       *  toggleBreakpoint = (JButton) toolbar.getComponentAtIndex(9);
       *  addWatch = (JButton) toolbar.getComponentAtIndex(10);
       *  removeWatch = (JButton) toolbar.getComponentAtIndex(11);
       */
      try {
         start = (JButton) ((JToolBar)toolbar.getComponent(0)).getComponent(0);
         pause = (JButton) ((JToolBar)toolbar.getComponent(0)).getComponent(1);
         stop = (JButton) ((JToolBar)toolbar.getComponent(0)).getComponent(2);
         stepInto = (JButton) ((JToolBar)toolbar.getComponent(0)).getComponent(4);
         stepOver = (JButton) ((JToolBar)toolbar.getComponent(0)).getComponent(5);
         runToCursor = (JButton) ((JToolBar)toolbar.getComponent(0)).getComponent(6);
         stepOut = (JButton) ((JToolBar)toolbar.getComponent(0)).getComponent(7);
         toggleBreakpoint = (JButton) ((JToolBar)toolbar.getComponent(0)).getComponent(9);
         addWatch = (JButton) ((JToolBar)toolbar.getComponent(0)).getComponent(10);
         removeWatch = (JButton) ((JToolBar)toolbar.getComponent(0)).getComponent(11);
      } catch (Exception ex) {
         System.err.println("Error getting components in TIDEDebug Toolbar: " + ex.getMessage());
         ex.printStackTrace();
      }

      try {
         // we load icons here
         toggleBreakpoint.setIcon(new ImageIcon(getClass().getResource("/com/garagegames/torque/tidedebug/icons/TideToggleBreakpoint24.gif")));
         start.setIcon(new ImageIcon(getClass().getResource("/com/garagegames/torque/tidedebug/icons/TidePlay24.gif")));
         stop.setIcon(new ImageIcon(getClass().getResource("/com/garagegames/torque/tidedebug/icons/TideStop24.gif")));
         pause.setIcon(new ImageIcon(getClass().getResource("/com/garagegames/torque/tidedebug/icons/TidePause24.gif")));
         stepOver.setIcon(new ImageIcon(getClass().getResource("/com/garagegames/torque/tidedebug/icons/TideStepOver24.gif")));
         stepInto.setIcon(new ImageIcon(getClass().getResource("/com/garagegames/torque/tidedebug/icons/TideStepIn24.gif")));
         stepOut.setIcon(new ImageIcon(getClass().getResource("/com/garagegames/torque/tidedebug/icons/TideStepOut24.gif")));
         runToCursor.setIcon(new ImageIcon(getClass().getResource("/com/garagegames/torque/tidedebug/icons/TideRunToCursor24.gif")));
      } catch (Exception ex) {
         System.err.println("Error setting icons in TIDEDebug Toolbar: " + ex.getMessage());
         ex.printStackTrace();
      }

      try {
         addWatch.setEnabled(false);
         // FIXME: disabled until implemented
         removeWatch.setEnabled(false);
         // FIXME: disabled until implemented

         add(start);
         add(pause);
         add(stop);
         addSeparator();
         add(stepInto);
         add(stepOver);
         add(runToCursor);
         add(stepOut);
         addSeparator();
         add(toggleBreakpoint);
         add(addWatch);
         add(removeWatch);
         add(Box.createGlue());
         setFloatable(false);
         putClientProperty("JToolBar.isRollover", Boolean.TRUE);

         updateButtons(Tide.NOTCONNECTED);

         // we are a torque state listener
         if (tide != null) {
            tide.addTorqueStateListener(this);
         }
      } catch (Exception ex) {
         System.err.println("Error in TIDEDebug Toolbar: " + ex.getMessage());
         ex.printStackTrace();
      }
   }


   /**
    *  Description of the Method
    *
    *@param  state  Description of the Parameter
    */
   private void updateButtons(int state) {
	   System.err.print("updateButtons: " + state);
      if (state == Tide.NOTCONNECTED) {
         start.setEnabled(true);
         pause.setEnabled(false);
         stop.setEnabled(false);
         stepInto.setEnabled(false);
         stepOver.setEnabled(false);
         stepOut.setEnabled(false);
         runToCursor.setEnabled(false);
      } else if (state == Tide.RUNNING) {
         start.setEnabled(false);
         pause.setEnabled(true);
         stop.setEnabled(true);
         stepInto.setEnabled(false);
         stepOver.setEnabled(false);
         stepOut.setEnabled(false);
         runToCursor.setEnabled(false);
      } else {
         // if(state == Tide.PAUSED)

         start.setEnabled(true);
         pause.setEnabled(false);
         stop.setEnabled(true);
         stepInto.setEnabled(true);
         stepOver.setEnabled(true);
         stepOut.setEnabled(true);
         runToCursor.setEnabled(true);
      }
   }


   /**
    *  Invoked by AWT when the panel is shown.
    */
   public void addNotify() {
      super.addNotify();
      // not much to do
   }


   /**
    *  Invoked by AWT when the panel is disposed.
    */
   public void removeNotify() {
      super.removeNotify();
      // not much to do
   }


   //
   // TIDE TORQUE LISTENER interaface...
   //

   // called when state changes...including disconnect
   /**
    *  Description of the Method
    *
    *@param  state  Description of the Parameter
    */
   public void stateChanged(int state) {
      // if we are becoming paused...bring jedit to the front
      if (state == Tide.PAUSED) {
         view.toFront();
      }

      updateButtons(state);
   }
}

