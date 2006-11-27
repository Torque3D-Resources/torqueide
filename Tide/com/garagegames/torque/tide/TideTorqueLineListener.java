/*
 * TideTorqueLineListner.java
 * Copyright (c) 2002 Paul Dana
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 */

package com.garagegames.torque.tide;

import com.garagegames.torque.*;

// listen to changes in the currently executing line
public interface TideTorqueLineListener
{
   // called when current line has changed while paused
   public void lineChanged(String filename, int lineNumber);
}


