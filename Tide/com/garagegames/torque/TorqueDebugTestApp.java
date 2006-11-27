// copyright (c) 2002, Paul Dana.
// Distributed under the GNU GPL: http://www.fsf.org/copyleft/gpl.html
package com.garagegames.torque;

// TorqueDebugTestApp

// test application for developing components for a debugger
// for Torque Game Engine script. See www.garagegames.com for
// information on TGE script. tests the TorqueDebug component

import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class TorqueDebugTestApp extends JApplet
         implements ActionListener, TorqueDebugListener
{
   static final String CommConnected = "Connected";
   static final String CommDisconnected = "Disconnected";
   static final String StatusRunning = "Running...";
   static final String StatusPaused = "Paused...";

   // if we are connected torqueDebug is non-null
   TorqueDebug torqueDebug;
   TorqueDebugOptions torqueOptions;

   // UI
   JLabel commLabel;
   JLabel statusLabel;
   JTextArea consoleTextArea;
   JTextArea callstackTextArea;
   JTextArea rawInputTextArea;
   JTextArea rawOutputTextArea;
   JButton playButton;
   JButton stopButton;
   JButton pauseButton;
   JButton stepInButton;
   JButton stepOverButton;
   JButton stepOutButton;
   JButton evalButton;

   public TorqueDebugTestApp()
   {
      // hardcode to my game folder
      torqueOptions = new TorqueDebugOptions();
      torqueOptions.gamePath =
         new File("e:/torque_content/TreasureHunt/TreasureHunt_DEBUG.exe");

      // does this game need patching
      TorqueDebugPatcher patcher = new TorqueDebugPatcher(torqueOptions.gamePath);
      try
      {
         if (!patcher.hasDebugOptions())
         {
            if (JOptionPane.showConfirmDialog(null,"Patch Game?","Patcher",
                                              JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
            {
               patcher.patchGame(this.getClass(),null);
            }
         }
      }
      catch(Exception e)
      {
         JOptionPane.showMessageDialog(null,"Error: " + e.getLocalizedMessage());
         System.exit(-1);
      }
   }

   public static void main(String[] argv)
   {
      JFrame frame = new JFrame( "TorqueDebug Test Application" );
      frame.addWindowListener( new WindowAdapter(){
                                  public void windowClosing( WindowEvent e ){
                                     System.exit( 0 );
                                  }
                               }
                             );

      JApplet applet = new TorqueDebugTestApp();
      frame.getContentPane().add( BorderLayout.CENTER, applet );
      applet.init();
      frame.setSize( 750, 560);
      frame.show();
   }

   public void init()
   {
      // main panels for UI
      Box mainPanel = new Box(BoxLayout.Y_AXIS);
      getContentPane().setLayout(new BorderLayout());
      getContentPane().add(mainPanel,"Center");

      // helper vars
      JPanel panel;
      JScrollPane scroll;

      // top panel
      Box topPanel = new Box(BoxLayout.X_AXIS);
      mainPanel.add(topPanel);

      // raw input
      panel = new JPanel();
      topPanel.add(panel);
      panel.add(new JLabel("Raw Input:"));
      rawInputTextArea = new JTextArea();
      rawInputTextArea.setPreferredSize(new Dimension(340,200));
      rawInputTextArea.setEditable(false);
      scroll = new JScrollPane(rawInputTextArea);
      scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      scroll.setAutoscrolls(true);
      panel.add(scroll);

      // raw output
      panel = new JPanel();
      topPanel.add(panel);
      panel.add(new JLabel("Raw Output:"));
      rawOutputTextArea = new JTextArea();
      rawOutputTextArea.setPreferredSize(new Dimension(340,200));
      rawOutputTextArea.setEditable(false);
      scroll = new JScrollPane(rawOutputTextArea);
      scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      scroll.setAutoscrolls(true);
      panel.add(scroll);

      // bottom panel
      Box bottomPanel = new Box(BoxLayout.X_AXIS);
      mainPanel.add(bottomPanel);

      // console
      panel = new JPanel();
      bottomPanel.add(panel);
      panel.add(new JLabel("Console:"));
      consoleTextArea = new JTextArea();
      consoleTextArea.setPreferredSize(new Dimension(340,200));
      consoleTextArea.setEditable(false);
      scroll = new JScrollPane(consoleTextArea);
      scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      scroll.setAutoscrolls(true);
      panel.add(scroll);

      // callstack
      panel = new JPanel();
      bottomPanel.add(panel);
      panel.add(new JLabel("Callstack:"));
      callstackTextArea = new JTextArea();
      callstackTextArea.setPreferredSize(new Dimension(340,200));
      callstackTextArea.setEditable(false);
      scroll = new JScrollPane(callstackTextArea);
      scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      scroll.setAutoscrolls(true);
      panel.add(scroll);

      // button for "Pause" and for "Play"
      JPanel buttonPanel = new JPanel();
      getContentPane().add(buttonPanel,"South");

      playButton = new JButton("Play");
      playButton.addActionListener(this);
      buttonPanel.add(playButton);

      stopButton = new JButton("Stop");
      stopButton.addActionListener(this);
      buttonPanel.add(stopButton);

      pauseButton = new JButton("Pause");
      pauseButton.addActionListener(this);
      buttonPanel.add(pauseButton);

      stepInButton = new JButton("StepIn");
      stepInButton.addActionListener(this);
      buttonPanel.add(stepInButton);

      stepOutButton = new JButton("StepOut");
      stepOutButton.addActionListener(this);
      buttonPanel.add(stepOutButton);

      stepOverButton = new JButton("StepOver");
      stepOverButton.addActionListener(this);
      buttonPanel.add(stepOverButton);

      evalButton = new JButton("Eval...");
      evalButton.addActionListener(this);
      buttonPanel.add(evalButton);

      // status
      Box statePanel = new Box(BoxLayout.Y_AXIS);
      getContentPane().add(statePanel,"North");

      // comm
      panel = new JPanel();
      statePanel.add(panel);
      panel.add(new JLabel("Connected: "));
      commLabel = new JLabel(CommDisconnected);
      commLabel.setForeground(Color.blue);
      panel.add(commLabel);

      // status
      panel = new JPanel();
      statePanel.add(panel);
      panel.add(new JLabel("Status: "));
      statusLabel = new JLabel(StatusRunning);
      statusLabel.setForeground(Color.blue);
      panel.add(statusLabel);

      // set GUI right off
      setGUI(false, false);
   }

   // set GUI state depending on paused status
   public void setGUI(boolean isConnected, boolean isPaused)
   {
      if (!isConnected)
      {
         playButton.setEnabled(true);
         stopButton.setEnabled(false);
         pauseButton.setEnabled(false);
         stepInButton.setEnabled(false);
         stepOutButton.setEnabled(false);
         stepOverButton.setEnabled(false);
         evalButton.setEnabled(false);
         commLabel.setText(CommDisconnected);
         statusLabel.setText("");
      }
      else
      {
         commLabel.setText(CommConnected);
         playButton.setEnabled(isPaused);
         stopButton.setEnabled(true);
         pauseButton.setEnabled(!isPaused);
         stepInButton.setEnabled(isPaused);
         stepOutButton.setEnabled(isPaused);
         stepOverButton.setEnabled(isPaused);
         evalButton.setEnabled(isPaused);
         if (isPaused)
            statusLabel.setText(StatusPaused);
         else
            statusLabel.setText(StatusRunning);
      }
   }

   // any button hit
   public void actionPerformed(ActionEvent e)
   {
      Object source = e.getSource();

      if (source == playButton)
      {
         System.out.println("playbutton");
         // if we are not connected
         if (torqueDebug == null)
         {
            int btn = JOptionPane.showConfirmDialog(null,"Launch Game?","Run Type",
                                                    JOptionPane.YES_NO_OPTION);

            // if we want local host?
            if (btn == JOptionPane.YES_OPTION)
            {
               torqueOptions.launch = true;
               torqueOptions.host = TorqueDebugOptions.LocalHost;
            }
            else
            {
               // we still use local host but in this case I better
               // have launched it myself
               torqueOptions.launch = false;
               torqueOptions.host = TorqueDebugOptions.LocalHost;
            }

            // ok now try to establish connection and optionally launch the game
            torqueDebug = TorqueDebug.create(this,torqueOptions);
         }
         else
         {
            if (torqueDebug.isPaused())
               torqueDebug.continueExecution();
         }

      }
      if (source == stopButton)
      {
         System.out.println("stopbutton");

         // this will invoke preDestroy() on listener which will
         // give us a chance to null our reference
         torqueDebug.destroy();
      }
      if (source == pauseButton)
      {
         System.out.println("pausebutton");
         torqueDebug.stepIn();
      }
      else if (source == stepInButton)
      {
         System.out.println("stepinbutton");
         torqueDebug.stepIn();
      }
      else if (source == stepOverButton)
      {
         System.out.println("stepoverbutton");
         torqueDebug.stepOver();
      }
      else if (source == stepOutButton)
      {
         System.out.println("stepoutbutton");
         torqueDebug.stepOut();
      }
      else if (source == evalButton)
      {
         System.out.println("evalbutton");
         String v = JOptionPane.showInputDialog(null,"Enter variable: ");
         if (v != null)
            torqueDebug.evaluate(v);
      }
   }

   // called when connection itself failed
   public void createFailed(Exception e)
   {
      // tell user to run game before running this app
      System.out.println("TorqueDebug Create Error: " + e.getLocalizedMessage());
      String msg = "This error received connecting to Torque Debugger:\n\n"+
                   "    " + e.getLocalizedMessage() + "\n";
      JOptionPane.showMessageDialog(this,msg);
      torqueDebug = null;
   }

   // called when an error occurs (other than connection failure)
   public void error(Exception e)
   {
      // tell user to run game before running this app
      System.out.println("TorqueDebug Error: " + e.getLocalizedMessage());
      String msg = "This error received from TorqueDebug:\n\n"+
                   "    " + e.getLocalizedMessage() + "\n";
      JOptionPane.showMessageDialog(this,msg);
   }

   // called when a new output string is received from TGE
   public void rawOutput(String s)
   {
      System.out.println ("RAW OUTPUT:"+s);
      rawOutputTextArea.append(s+"\n");
   }

   // called when a new input string is sent to TGE
   public void rawInput(String s)
   {
      System.out.println ("RAW INPUT:"+s);
      rawInputTextArea.append(s+"\n");
   }

   // called when program quits or connection is lost
   // the TorqueDebug object reference should be
   // nulled by the implementor of this interface when
   // this method is called.
   public void preDestroy()
   {
      // either the program quit or the telnet debug connection was broken
      setGUI(false,false);

      // as per interface docs we must null our reference here...
      // this also marks us as "not connected"
      torqueDebug = null;
   }

   // called when connection has been established
   public void connected()
   {
      setGUI(true, torqueDebug.isPaused());
   }

   // called when script execution paused
   public void paused(String fileName, int lineNumber )
   {
      setGUI(torqueDebug!=null, true);
   }

   // called when script execution is running
   public void running()
   {
      setGUI(torqueDebug!=null, false);
      callstackTextArea.setText("");
   }

   // called when a script file list received from TGE
   public void fileList(String[] list)
   {
      // as a test lets append this to the console
      //for (int i=0; i<list.length; i++)
      //  consoleTextArea.append("FILELIST[" + i + "]="+list[i]+"\n");
   }

   // called when new callstack available
   // NOTE: paused() is called *before* this method
   public void callStack(String[] filestack, int[] numberstack,
                         String[] functionstack)
   {
      // clear and add to callstack
      callstackTextArea.setText("");
      for (int i=0; i<filestack.length; i++)
         callstackTextArea.append(filestack[i]+ " (" + numberstack[i] + "): " +
                                  functionstack[i] + "\n");
   }

   // console output
   public void consoleOutput(String s)
   {
      consoleTextArea.append(s+"\n");
   }

   // called when breakpoint list has been updated
   public void breakPointListChanged(String fileName, Collection breakList)
   {
      //System.out.println("BreakPoint data changed:");
      Iterator iter = breakList.iterator();
      while(iter.hasNext())
      {
         TorqueBreakPoint bp = (TorqueBreakPoint)iter.next();
         //System.out.println("  f="+bp.fileName+" #="+bp.lineNumber+" a="+bp.active);
      }
   }

   public void evaluationReady(String variable, String value)
   {
      System.out.println("Evaluation ready: " + variable + "=" + value);
   }


}

