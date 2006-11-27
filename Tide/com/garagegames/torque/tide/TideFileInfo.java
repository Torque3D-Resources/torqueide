// copyright (c) 2002, Paul Dana.
// Distributed under the GNU GPL: http://www.fsf.org/copyleft/gpl.html

// TideFileInfo

// simple class for managing per-file info for USER DEFINED breakpoints
package com.garagegames.torque.tide;

import com.garagegames.torque.*;

import java.util.*;

// NOTE: The class com.garagegames.toruqe.TorqueBreakPoint contains information
//       from Torque about which lines in a file are 'breakable' and also which
//       breakpoints are currently active.
//
//       *This* class contains per-file breakpoint information...meaning each
//       source file that has breakpoint information has an object of this
//       class associated with it. It contains a list of TorqueBreakPoint
public class TideFileInfo
{
   String         name;                       // file name of script file this break info is for
   Hashtable      hash = new Hashtable();     // hashtable of break info keyed on line number

   public TideFileInfo(String name)
   {
      this.name = name;
   }

   // add break point. return previous breakpoint saved at this line number
   // or null if there was none
   public synchronized TorqueBreakPoint addBreak(TorqueBreakPoint bp)
   {
      // return false if null input or breakpoint already added to hash table
      if (bp == null)
         return null;
      return (TorqueBreakPoint)hash.put(bp.key,bp);
   }

   // remove break info...return breakpoint for this line number
   // or null if there was none
   public synchronized TorqueBreakPoint removeBreak(int lineNumber)
   {
      return removeBreak(Integer.toString(lineNumber));
   }
   public synchronized TorqueBreakPoint removeBreak(TorqueBreakPoint bp)
   {
      return removeBreak(bp.key);
   }
   public synchronized TorqueBreakPoint removeBreak(String key)
   {
      if (key == null)
         return null;
      return (TorqueBreakPoint)hash.remove(key);
   }

   // get break info by line number
   public synchronized TorqueBreakPoint getBreak(int lineNumber)
   {
      return getBreak(Integer.toString(lineNumber));
   }
   public synchronized TorqueBreakPoint getBreak(String key)
   {
      return (TorqueBreakPoint)hash.get(key);
   }

   // get Enumeration for breakpoints
   public Enumeration elements()
   {
      return hash.elements();
   }

   // remove all "breakable" lines...meaning remove any breakpoint that
   // was not asked for by the user...these are the breakpoints that
   // mark lines that are 'breakable' but that we never asked for breakpoints on
   public synchronized void removeDisabled()
   {
      Vector disabled = new Vector();

      // add all disabled into a vector
      for (Enumeration e = hash.elements(); e.hasMoreElements(); )
      {
         TorqueBreakPoint bp = (TorqueBreakPoint)e.nextElement();
         if (!bp.enabled)
            disabled.add(bp.key);
      }

      // now remove all these
      for (Iterator iter=disabled.iterator(); iter.hasNext(); )
         hash.remove((String)iter.next());
   }

   // remove all "breakable" lines and clear any illegal
   // flags on any enaabled breakpoins
   public synchronized void removeDisabledClearIllegal()
   {
      Vector disabled = new Vector();

      // add all disabled into a vector and clear illegal
      // on any enabled
      for (Enumeration e = hash.elements(); e.hasMoreElements(); )
      {
         TorqueBreakPoint bp = (TorqueBreakPoint)e.nextElement();
         if (bp.enabled)
            bp.illegal = false;
         else
            disabled.add(bp.key);
      }

      // now remove all these
      for (Iterator iter=disabled.iterator(); iter.hasNext(); )
         hash.remove((String)iter.next());
   }

   // merge in fresh information from TorqueDebug about which lines are breakable
   // first we clear any prevoiusly 'breakable' lines
   // then we mark all user defined (enabled) breakpoins as ILLEGAL
   // then we merge in the new 'breakable' line information
   // if a user defined breakpoint is found to be on a 'breakable' line then we
   // mark it as LEGAL.
   public synchronized void mergeNewBreakPointInfo(Collection breakList)
   {
      // first remove any disabled we might already have
      removeDisabled();

      // ok now assume that if it's not scottish...it's crap!
      for (Enumeration e = elements(); e.hasMoreElements(); )
      {
         TorqueBreakPoint bp = (TorqueBreakPoint)e.nextElement();
         bp.illegal = true;
      }

      // now run through the new list and turn back to legal if it is...
      for (Iterator iter = breakList.iterator(); iter.hasNext(); )
      {
         TorqueBreakPoint bp = (TorqueBreakPoint)iter.next();

         // see if if we have a breakpoint at this line
         TorqueBreakPoint ourBreak = getBreak(bp.key);
         if (ourBreak == null)
         {
            // add this non-user defined break
            TorqueBreakPoint newBP = new TorqueBreakPoint(bp.fileName,bp.lineNumber);
            addBreak(newBP);
         }
         else
         {
            // well, well...it's scottish after all!
            ourBreak.illegal = false;
         }
      }

      // if it's illegal then it is not active either!
      for (Enumeration e = elements(); e.hasMoreElements(); )
      {
         TorqueBreakPoint bp = (TorqueBreakPoint)e.nextElement();
         if (bp.illegal)
            bp.active = false;
      }
   }
}
