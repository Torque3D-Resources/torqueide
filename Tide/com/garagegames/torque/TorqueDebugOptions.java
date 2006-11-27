// copyright (c) 2002, Paul Dana.
// Distributed under the GNU GPL: http://www.fsf.org/copyleft/gpl.html                                 *

// TorqueDebugOptions

// Simple public class that defines data needed to establish/aintain a
// debug session with Torque telnet debugger.

package com.garagegames.torque;

import java.io.*;
import java.net.*;
import java.util.*;

public class TorqueDebugOptions
{
   // defaults
   public static final String  LocalHost = "127.0.0.1";
   public static final boolean DefaultLaunch = true;
   public static final String  DefaultHost = LocalHost;
   public static final int     DefaultPort = 28040;
   public static final String  DefaultPassword = "password";
   public static final File    DefaultGamePath =
      new File("c:/torque/example/torqueDemo_DEBUG.exe");
   public static final long    DefaultConnectTimeout = 60000;  // try for 60 sec
   public static final long    DefaultReconnectInterval = 1000;// retry every sec

   // data members are public for direct access
   public boolean  launch;             // if true launch game locally
   public String   host;               // IP this game is hosted on
   public int      port;               // debugging port this game is using
   public String   password;           // debugging password this game is using
   public File     gamePath;           // path to local copy of the game

   // data members NOT in the constructor, just set directly as needed (public)
   public long     connectTimeout;     // wait this # msec for a connect
   public long     reconnectInterval;  // how long to wait between connects

   // constructors
   public TorqueDebugOptions(boolean launch, String host, int port,
                             String password, File gamePath)
   {
      this.launch = launch;
      this.host = host;
      this.port = port;
      this.password = password;
      this.gamePath = gamePath;

      // set datamembers not in constructor to defaults
      connectTimeout = DefaultConnectTimeout;
      reconnectInterval = DefaultReconnectInterval;
   }

   public TorqueDebugOptions(TorqueDebugOptions opt)
   {
      this(opt.launch, opt.host, opt.port, opt.password, opt.gamePath);
   }

   public TorqueDebugOptions()
   {
      this(DefaultLaunch,DefaultHost,DefaultPort,DefaultPassword,DefaultGamePath);
   }
}
