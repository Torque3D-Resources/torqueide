/*
 *  CurrentLineHighlight.java - highlight the current execution line
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

import java.awt.*;
import java.awt.event.MouseEvent;

import org.gjt.sp.jedit.*;
import org.gjt.sp.util.Log;
import org.gjt.sp.jedit.textarea.*;

import com.sun.jdi.Location;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;

import com.garagegames.torque.tide.*;

/**
 *  Highlighter for the current execution line, if the debugger is suspended.
 *
 *@author     beffy
 *@created    20. Juni 2004
 */
public class CurrentLineHighlight extends TextAreaExtension implements
      TideTorqueStateListener, TideTorqueLineListener {
   // TODO: make current line color configurable
   private final static Color currentLineColor = new Color(128, 0, 0);

   private Tide tide;
   private JEditTextArea textArea;
   private boolean suspended = false;
   private int currentLine = -1;
   private String currentFilename;

   // -1 means no line number requested...otherwise this is the
   // line number we want to scroll to
   private int requestedLineNumber = -1;


   /**
    *  Constructor for the CurrentLineHighlight object
    *
    *@param  tide      Description of the Parameter
    *@param  textArea  Description of the Parameter
    */
   public CurrentLineHighlight(Tide tide, JEditTextArea textArea) {
      this.tide = tide;
      this.textArea = textArea;
      tide.addTorqueStateListener(this);
      tide.addTorqueLineListener(this);
   }


   /**
    *  Description of the Method
    *
    *@param  gfx           Description of the Parameter
    *@param  screenLine    Description of the Parameter
    *@param  physicalLine  Description of the Parameter
    *@param  start         Description of the Parameter
    *@param  end           Description of the Parameter
    *@param  y             Description of the Parameter
    */
   public void paintValidLine(Graphics2D gfx, int screenLine,
         int physicalLine, int start, int end, int y) {
      Buffer buffer = textArea.getView().getBuffer();

      // CRITICAL: do only as much tests as necessary, otherwise it
      // would slow down cursor movement
      if (!buffer.isLoaded()) {
         return;
      }

      if (suspended && requestedLineNumber != -1) {
         if (tide.scrollTo(requestedLineNumber)) {
            requestedLineNumber = -1;
         }
      }

      if (!suspended || currentLine == -1) {
         return;
      }

      if (physicalLine + 1 != currentLine) {
         return;
      }

      // replace backslashes with forward slashes
      String s = buffer.getPath().replace('\\', '/');
      if (!s.endsWith(currentFilename)) {
         return;
      }

      FontMetrics fm = textArea.getPainter().getFontMetrics();
      int lineHeight = fm.getHeight();
      int descent = fm.getDescent();

      gfx.setColor(currentLineColor);
      gfx.drawRect(0, y, textArea.getWidth() - 1, lineHeight - 1);
   }


   /**
    *  Description of the Method
    */
   protected void finalize() {
      // FIXME: this may be too late. Maybe EditPaneUpdate.DESTROYED is better
      tide.removeTorqueStateListener(this);
      tide.removeTorqueLineListener(this);
   }


   /**
    *  Description of the Method
    */
   private void redraw() {

	   /*
       *  FoldVisibilityManager foldVisibilityMgr = textArea.getFoldVisibilityManager();
       *  int physicalFirst = foldVisibilityMgr.getFirstVisibleLine();
       *  int physicalLast  = foldVisibilityMgr.getLastVisibleLine();
       *  textArea.invalidateLineRange(physicalFirst, physicalLast);
       */
      // JEdit 4.2 pre
      DisplayManager displayMgr = textArea.getDisplayManager();
      int physicalFirst = displayMgr.getFirstVisibleLine();
      int physicalLast = displayMgr.getLastVisibleLine();
      Log.log(Log.DEBUG, this, "physicalFirst: " + physicalFirst + " physicalLast: " + physicalLast);
      try
      {
    	  textArea.invalidateLineRange(physicalFirst, physicalLast);
      }
      catch(Exception ex){}
   }


   // called when state changes...including disconnect
   /**
    *  Description of the Method
    *
    *@param  state  Description of the Parameter
    */
   public void stateChanged(int state) {
      // remember suspended status
      suspended = (state == Tide.PAUSED);
   }


   // called when current line has changed while paused
   /**
    *  Description of the Method
    *
    *@param  fileName    Description of the Parameter
    *@param  lineNumber  Description of the Parameter
    */
   public void lineChanged(String fileName, int lineNumber) {

      // remember current line and current filename
      currentLine = lineNumber;
      currentFilename = fileName;

      // make sure current filename is open
      if (!tide.openAndScrollTo(fileName, lineNumber)) {
         // ok the scroll to part above might have to be delayed so put in a
         // request here to update the current line
         requestedLineNumber = lineNumber;
      }

      // cause an update so our line highlight will get drawn in correct location
      redraw();
   }
}

