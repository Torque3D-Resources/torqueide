/*
 *  TideBrowsePlugin.java - Java Browser Plugin
 *
 *  Copyright (c) 1999-2000 George Latkiewicz, Andre Kaplan
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
package com.garagegames.torque.tidebrowse;

import com.garagegames.torque.tide.*;

import java.awt.Cursor;

import java.util.Vector;
import javax.swing.*;
import org.gjt.sp.jedit.EBPlugin;

import org.gjt.sp.jedit.EditPlugin;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.gui.OptionsDialog;
import org.gjt.sp.jedit.jEdit;

import org.gjt.sp.util.Log;

/**
 *  This class implements the jEdit's EBPlugin interface for the TideBrowse
 *  plugin. Responsible for creating dockable TideBrowse.
 *
 *@author     beffy
 *@created    15. Dezember 2003
 *@version    $Id: TideBrowsePlugin.java,v 1.3 2003/12/15 20:58:20 cvsuser Exp $
 */
public class TideBrowsePlugin extends EBPlugin {
   /**
    *  Description of the Method
    */
   public void start() { }


   /**
    *  Description of the Method
    */
   public void stop() { }


   /**
    *  Description of the Method
    *
    *@param  menuItems  Description of the Parameter
    */
   public void createMenuItems(Vector menuItems) {

      JMenu menu = GUIUtilities.loadMenu("TideBrowse-menu");
      JMenu tideMenu = Tide.getTideMenu();
      if(tideMenu != null) {
         tideMenu.add(menu);
         tideMenu.add(new JSeparator());
      }
      else {
         menuItems.addElement(menu);
      }
   }


   /**
    *  Adds the TideBrowse options the the Global Options dialog
    *
    *@param  od  Description of the Parameter
    */
   public void createOptionPanes(OptionsDialog od) {
      od.addOptionPane(TideBrowseOptionPane.getInstance());
   }


   /**
    *  Gets the name attribute of the TideBrowsePlugin object
    *
    *@return    The name value
    */
   public String getName() {
      return "TideBrowse";
   }


   /**
    *  Description of the Method
    *
    *@param  view  Description of the Parameter
    */
   public static void openTideBrowseFor(View view) {
      Cursor savedCursor = null;

      try {
         // Check if the current buffer is a java buffer
         try {
            String bufferName = view.getBuffer().getName();
            if(!(bufferName.toLowerCase().endsWith(".cs") || bufferName.toLowerCase().endsWith(".gui") || bufferName.toLowerCase().endsWith(".mis"))) {
               GUIUtilities.error(
                     view,
                     "TideBrowse.msg.notAJavaBuffer",
                     new Object[]{bufferName}
                     );
               // No need to go further
               return;
            }
         }
         catch(NullPointerException npe) {
            Log.log(Log.ERROR, TideBrowsePlugin.class, npe);
         }

         // Set Wait Cursor
         savedCursor = view.getCursor();
         view.setCursor(new Cursor(Cursor.WAIT_CURSOR));

         // Build and Display a TideBrowse GUI
         // Keep track of TideBrowse <-> view assoc?
         new TideBrowseDialog(view);

      }
      catch(Exception e) {
         Log.log(Log.DEBUG, TideBrowsePlugin.class, e);
      }
      finally {
         if(savedCursor != null) {
            view.setCursor(savedCursor);
         }
         else {
            view.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
         }
      }
   }
}

