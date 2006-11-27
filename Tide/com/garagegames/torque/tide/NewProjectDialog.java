/*
 * NewProjectDialog.java
 * Copyright (c) 2002 Paul Dana
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 */

package com.garagegames.torque.tide;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

import com.garagegames.torque.*;
import projectviewer.*;

public class NewProjectDialog extends JDialog
         implements ActionListener
{
   // dialog is modal
   private static NewProjectDialog dialog = null;
   private static NewProjectOptions value = null;

   JPanel centerPanel = new JPanel();
   JPanel bottomPanel = new JPanel();
   final JButton setButton = new JButton();
   JButton cancelButton = new JButton();
   JTextField projectName = new JTextField();
   JLabel jLabel1 = new JLabel();
   JLabel jLabel2 = new JLabel();
   JTextField gameExecutable = new JTextField();
   JButton projectBrowse = new JButton();
   JLabel jLabel3 = new JLabel();
   JTextField host = new JTextField();
   JLabel jLabel4 = new JLabel();
   JCheckBox local = new JCheckBox();
   JLabel jLabel5 = new JLabel();
   JTextField port = new JTextField();
   JLabel jLabel6 = new JLabel();
   JTextField password = new JTextField();
   GridBagLayout gridBagLayout1 = new GridBagLayout();

   private void jbInit() throws Exception
   {
      cancelButton.addActionListener(this);
      setButton.addActionListener(this);
      getRootPane().setDefaultButton(setButton);
      projectBrowse.addActionListener(this);

      setButton.setText("OK");
      cancelButton.setText("Cancel");
      projectName.setColumns(30);
      jLabel1.setText("Project Name:");
      centerPanel.setLayout(gridBagLayout1);
      jLabel2.setText("Game Executable:");
      gameExecutable.setColumns(30);
      projectBrowse.setText("...");
      centerPanel.setMinimumSize(new Dimension(485, 200));
      centerPanel.setPreferredSize(new Dimension(485, 200));
      jLabel3.setText("Launch Locally:");
      host.setColumns(30);
      jLabel4.setText("Host:");
      jLabel5.setText("Port:");
      port.setColumns(30);
      jLabel6.setText("Password:");
      password.setColumns(30);
      this.getContentPane().add(centerPanel, BorderLayout.CENTER);
      centerPanel.add(jLabel2,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                      ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(16, 11, 0, 0), 4, 0));
      centerPanel.add(projectName,  new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
                      ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(13, 0, 0, 0), -9, 0));
      centerPanel.add(gameExecutable,  new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
                      ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(13, 0, 0, 0), -9, 0));
      centerPanel.add(jLabel1,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                      ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(13, 33, 0, 0), 4, 0));
      centerPanel.add(projectBrowse,  new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
                      ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(15, 9, 0, 15), -11, -6));
      bottomPanel.add(setButton, null);
      bottomPanel.add(cancelButton, null);
      centerPanel.add(host,  new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0
                      ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(11, 0, 0, 0), -9, 0));
      centerPanel.add(jLabel4,  new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
                      ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(14, 81, 0, 0), 5, 0));
      centerPanel.add(local,    new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
                      ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 0, 0, 298), 3, 0));
      centerPanel.add(jLabel5,  new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
                      ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(14, 86, 0, 0), 4, 0));
      centerPanel.add(port,  new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0
                      ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(11, 0, 0, 0), -9, 0));
      centerPanel.add(jLabel6,  new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
                      ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(17, 52, 22, 0), 4, 0));
      centerPanel.add(password,  new GridBagConstraints(1, 5, 1, 1, 1.0, 0.0
                      ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(14, 0, 22, 0), -9, 0));
      centerPanel.add(jLabel3,  new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
                      ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(13, 27, 0, 0), 3, 0));
      this.getContentPane().add(bottomPanel, BorderLayout.SOUTH);

      pack();
   }

   // private constructor...only one dialog around at a time
   // for now public...change back later
   public NewProjectDialog(Frame frame, String title)
   {
      super(frame, title, true);

      try
      {
         jbInit();
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }

   // show modal dialog and return options or null if canel button pressed
   public static NewProjectOptions showDialog(Component comp, String title)
   {
      NewProjectOptions options = new NewProjectOptions();
      NewProjectOptions retValue = showDialog(comp,title,options);
      return retValue;
   }

   // show modal dialog and return options or null if canel button pressed
   public static NewProjectOptions showDialog(Component comp, String title,
         NewProjectOptions opt)
   {
      NewProjectOptions options = new NewProjectOptions(opt);

      // initialize if we never have been
      if (dialog == null)
      {
         Frame frame = JOptionPane.getFrameForComponent(comp);
         dialog = new NewProjectDialog(frame, title);
      }
      dialog.setValue(options);
      dialog.setLocationRelativeTo(comp);
      dialog.setVisible(true);

      return value;
   }

   // set the value we are editing
   private void setValue(NewProjectOptions newValue)
   {
      if (newValue == null)
         return;

      // this is our value now
      value = newValue;

      // setup UI from this...
      projectName.setText(value.projectName);
      gameExecutable.setText(value.options.gamePath.getAbsolutePath());
      local.setSelected(value.options.launch);
      host.setText(value.options.host);
      port.setText(""+value.options.port);
      password.setText(value.options.password);
   }

   // convenience...always returns null
   private NewProjectOptions errorMsg(String msg)
   {
      JOptionPane.showMessageDialog(null,msg,"Error",JOptionPane.ERROR_MESSAGE);
      return null;
   }

   // read the values back from the UI
   // if invalid give warning and return null
   private NewProjectOptions validateValue()
   {
      // read from UI and return this somebitch
      NewProjectOptions options = new NewProjectOptions();

      // get values from UI
      options.projectName = projectName.getText();
      if (options.projectName.equals(""))
         return errorMsg("You must specify a project name");

      // if this project name already exists
      if (ProjectManager.getInstance().hasProject(options.projectName))
         return errorMsg("A project by that name already exists. You must specify a unique name");

      File file = new File(gameExecutable.getText());
      if (!file.exists())
         return errorMsg("The game executable you specified does not exist");
      options.options.gamePath = file;

      options.options.launch = local.isSelected();

      String s;
      s = getHostName(host.getText());
      if (s == null)
         return errorMsg("The host name you specified is invalid. Host name must be\n"+
                         "of the form X.X.X.X");
      options.options.host = s;

      s = getPort(port.getText());
      if (s == null)
         return errorMsg("The port you specified is invalid. The port must be a positive number\n");
      options.options.port = Integer.parseInt(s);

      s = getPassword(password.getText());
      if (s == null)
         return errorMsg("The password you specified is invalid. The password cannot be blank and must contain letters and numbers only.");
      options.options.password = s;

      // if we are launching locally but the host is not 127.0.0.1...trouble!
      if (options.options.launch)
      {
         if (!options.options.host.equals(TorqueDebugOptions.LocalHost))
            return errorMsg("If you Launch the game Locally then the host must be: " + TorqueDebugOptions.LocalHost);

      }

      // return this
      return options;
   }

   // return true if the given string is a positive integer
   public boolean isNum(String s)
   {
      try
      {
         int i = Integer.parseInt(s);
         if (i < 0)
            return false;
      }
      catch (NumberFormatException e)
      {
         return false;
      }

      return true;
   }

   // return the string if this looks like a valid host name, else null
   public String getHostName(String s)
   {
      String host = s.trim();
      StringTokenizer st = new StringTokenizer(host,".");
      if (st.countTokens() != 4)
         return null;
      String a = st.nextToken();
      String b = st.nextToken();
      String c = st.nextToken();
      String d = st.nextToken();

      if (!isNum(a) || !isNum(b) || !isNum(c) || !isNum(d))
         return null;

      return host;
   }

   public String getPort(String s)
   {
      String port = s.trim();
      if (!isNum(port))
         return null;
      return port;
   }

   public String getPassword(String s)
   {
      String pass = s.trim();
      if (pass.length() < 1)
         return null;
      for (int i=0; i<pass.length(); i++)
      {
         char c = pass.charAt(i);
         if (c >= 'A' && c <= 'Z')
            continue;
         if (c >= 'a' && c <= 'z')
            continue;
         if (c >= '0' && c <= '9')
            continue;

         return null;
      }

      return pass;
   }

   // return string if this looks like valid port, else null

   // any menu item or button pressed
   public void actionPerformed(ActionEvent e)
   {
      if (e.getSource() == cancelButton)
      {
         value = null;
         dialog.setVisible(false);
      }
      else if (e.getSource() == setButton)
      {
         value = validateValue();
         if (value != null)
            dialog.setVisible(false);
      }
      else if (e.getSource() == projectBrowse)
      {
         browseForProjectOptions();
      }
   }

   void browseForProjectOptions()
   {
      // initialize with the game executable path
      String s = gameExecutable.getText();

      // display the 'open folder' dialog and have user select the game exe
      JFileChooser fc = new JFileChooser();
      fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
      if (s != null)
         fc.setSelectedFile(new File(s));
      int returnVal = fc.showOpenDialog(this);
      if (returnVal != JFileChooser.APPROVE_OPTION)
         return;
      File file = fc.getSelectedFile();

      // user chosen game executable
      gameExecutable.setText(file.getAbsolutePath());
   }

   String trimName(String path)
   {
      // filename off of absolute path
      return path;
   }
}

