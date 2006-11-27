/*
 * NewProjectOptions.java
 * Copyright (c) 2002 Paul Dana
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 */

package com.garagegames.torque.tide;

import java.io.*;

import com.garagegames.torque.*;

public class NewProjectOptions
{
  // some stupid defaults for testing....
  public static final String DefaultProjectName = "Torque Project";

  public String projectName;            // name of project
  public TorqueDebugOptions options;    // project options;

  // construct
  public NewProjectOptions()
  {
    this.projectName = DefaultProjectName;
    this.options = new TorqueDebugOptions();
  }

  // create one project options from another...
  public NewProjectOptions(NewProjectOptions opt)
  {
    this.projectName = new String(opt.projectName);
    this.options = new TorqueDebugOptions(opt.options);
  }
}
