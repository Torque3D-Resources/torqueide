/*
 *  BreakpointHighlight.java - show breakpoints in the textarea gutter
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

import javax.swing.*;

import org.gjt.sp.jedit.*;
import org.gjt.sp.util.Log;
import org.gjt.sp.jedit.buffer.JEditBuffer;
import org.gjt.sp.jedit.textarea.*;

import com.garagegames.torque.tide.*;
import com.garagegames.torque.*;

/**
 *  Highlighter for breakpoints.
 *
 *@author     beffy
 *@created    26. August 2004
 */
public class BreakpointHighlight extends TextAreaExtension
       implements TideTorqueStateListener, TideBreakInfoListener {

   // TODO: make breakpoint color configurable
   private final static Color colActive = Color.red.brighter();
   private final static Color colNotActive = Color.lightGray;

   private Tide tide;
   private JEditTextArea textArea;

   private ImageIcon possibleBreakpoint;
   private ImageIcon activeBreakpoint;
   private ImageIcon disabledBreakpoint;


   /**
    *  Constructor for the BreakpointHighlight object
    *
    *@param  tide      Description of the Parameter
    *@param  textArea  Description of the Parameter
    */
   public BreakpointHighlight(Tide tide, JEditTextArea textArea) {
      this.tide = tide;
      this.textArea = textArea;
      tide.addTorqueStateListener(this);
      tide.addBreakInfoListener(this);

      possibleBreakpoint = new ImageIcon(getClass().getResource("/com/garagegames/torque/tidedebug/icons/TidePossibleBreakpoint24.gif"));
      activeBreakpoint = new ImageIcon(getClass().getResource("/com/garagegames/torque/tidedebug/icons/TideActiveBreakpoint24.gif"));
      disabledBreakpoint = new ImageIcon(getClass().getResource("/com/garagegames/torque/tidedebug/icons/TideDisabledBreakpoint24.gif"));
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
	   //Buffer buffer = textArea.getBuffer();
	   Buffer buffer = textArea.getView().getBuffer();
	   if (!buffer.isLoaded()) {
         return;
      }

      //TorqueBreakPoint bp = tide.findBreakPoint(buffer.getFile().getAbsolutePath(),
      TorqueBreakPoint bp = tide.findBreakPoint(buffer.getPath(),
            physicalLine + 1);

      // no break on this line we got nuttin to do
      if (bp == null) {
         return;
      }

      // if we are not connected then we dont show the blue dots :)
      int tideState = tide.getState();

      int lineHeight = textArea.getPainter().getFontMetrics().getHeight();
      int gutterWidth = textArea.getGutter().getWidth();

      /*
       *  if (bp.active)
       *  gfx.setColor(colActive);
       *  else
       *  gfx.setColor(colNotActive);
       *  gfx.fillRect(0, y + 2, gutterWidth - 5, lineHeight - 4);
       */
      if (bp.illegal) {
         gfx.drawImage(activeBreakpoint.getImage(), 0, y + 2, gutterWidth - 5,
               lineHeight - 4, disabledBreakpoint.getImageObserver());
      } else if (bp.active) {
         gfx.drawImage(activeBreakpoint.getImage(), 0, y + 2, gutterWidth - 5,
               lineHeight - 4, activeBreakpoint.getImageObserver());
      } else {
         gfx.drawImage(possibleBreakpoint.getImage(), 0, y + 2, gutterWidth - 5,
               lineHeight - 4, possibleBreakpoint.getImageObserver());
      }
   }


   /**
    *  Description of the Method
    */
   protected void finalize() {
      // FIXME: this may be too late. Maybe EditPaneUpdate.DESTROYED is better
      tide.removeTorqueStateListener(this);
      tide.removeBreakInfoListener(this);
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
      textArea.invalidateLineRange(physicalFirst, physicalLast);

   }


   // called when state changes...including disconnect
   /**
    *  Description of the Method
    *
    *@param  state  Description of the Parameter
    */
   public void stateChanged(int state) {
      // do nothing
   }


   // called when break point info has changed for a given file
   /**
    *  Description of the Method
    *
    *@param  fileInfo  Description of the Parameter
    */
   public void breakInfoChanged(TideFileInfo fileInfo) {
      // just a repaint
      redraw();
   }

}

