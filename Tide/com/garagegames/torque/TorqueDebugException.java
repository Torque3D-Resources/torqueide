// copyright (c) 2002, Paul Dana.
// Distributed under the GNU GPL: http://www.fsf.org/copyleft/gpl.html                                 *

// TorqueDebugException

// Errors from TorqueDebugControl

package com.garagegames.torque;

import javax.swing.*;
import org.gjt.sp.jedit.*;

public class TorqueDebugException extends Exception
{
   public TorqueDebugException(String s)
   {
      super(s);
      JOptionPane.showMessageDialog(jEdit.getActiveView(), s);
   }
}
