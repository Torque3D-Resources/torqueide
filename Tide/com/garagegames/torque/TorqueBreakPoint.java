// copyright (c) 2002, Paul Dana.
// Distributed under the GNU GPL: http://www.fsf.org/copyleft/gpl.html

// TorqueBreakPoint

// simple class for managing per breakpoint information
package com.garagegames.torque;

/**
 *  Description of the Class
 *
 *@author     Paul Dana
 *@created    15. Dezember 2003
 */
public class TorqueBreakPoint {
   /**
    *  file containing break (redundant since breakpoints are stored in a per-file list)
    */
   public String fileName;
   /**
    *  line number of break
    */
   public int lineNumber;
   /**
    *  line number is used for key in hashtable lookup
    */
   public String key;
   /**
    *  if true then its a breakpoint the user asked for
    */
   public boolean enabled;
   /**
   * if true then we are actually breaking at this point
   * if a breakpoint is listed but has enabled FALSE then it just represents a line
   * that is 'breakable' but not one we currently want to break on
    */
   public boolean active;
   /**
    *  if true then this is a user defined breakpoint that is NOT on a breakable line
    */
   public boolean illegal;
   /**
    *  automatically make the break not active after it has been hit
    */
   public boolean disableAfterHit;
   /**
    *  not sure what this is...was in tribal ide...may use it yet
    */
   public int passCount;
   /**
    *  for conditional breaks...not implemented yet
    */
   public String condition;
   /**
    *  not sure what this is...was in tribal ide...may use it yet
    */
   public String group;


   /**
    *  Constructor for the TorqueBreakPoint object
    *
    *@param  fileName    Description of the Parameter
    *@param  lineNumber  Description of the Parameter
    */
   public TorqueBreakPoint(String fileName, int lineNumber) {
      this(fileName, lineNumber, false, "", 0, "");
   }


   /**
    *  Constructor for the TorqueBreakPoint object
    *
    *@param  fileName    Description of the Parameter
    *@param  lineNumber  Description of the Parameter
    *@param  active      Description of the Parameter
    *@param  condition   Description of the Parameter
    *@param  passCount   Description of the Parameter
    *@param  group       Description of the Parameter
    */
   public TorqueBreakPoint(String fileName, int lineNumber, boolean active,
                           String condition, int passCount, String group) {
      this.fileName = fileName;
      this.lineNumber = lineNumber;
      this.key = Integer.toString(lineNumber);
      this.active = active;
      this.enabled = false;
      this.illegal = false;
      this.disableAfterHit = false;
      this.passCount = passCount;
      this.condition = condition;
      this.group = group;
   }
}

