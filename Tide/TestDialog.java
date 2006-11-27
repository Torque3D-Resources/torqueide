/*
 * TestDialog.java
 * test the NewProject dialog
 */


import java.io.File;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.*;

import com.garagegames.torque.tide.*;

// code to test the com.pfizer.dtc.plot package
public class TestDialog
{
   public static void main( String[] argv )
   {
      NewProjectOptions opt = NewProjectDialog.showDialog(null,"New Project");

      if (opt == null)
         JOptionPane.showMessageDialog(null,"you hit cancel!");
      else
         JOptionPane.showMessageDialog(null,"You hit OK!");
   }
}
