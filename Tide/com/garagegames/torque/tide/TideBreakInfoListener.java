/*
 * TideBreakInfoListner.java
 * Copyright (c) 2002 Paul Dana
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 */

package com.garagegames.torque.tide;

import com.garagegames.torque.*;

// listen to changes in tide break info
public interface TideBreakInfoListener
{
   // called when break point info has changed for a given file
   public void breakInfoChanged(TideFileInfo fileInfo);
}


