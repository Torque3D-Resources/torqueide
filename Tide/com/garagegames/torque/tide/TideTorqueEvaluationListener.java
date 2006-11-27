/*
 * TideTorqueEvaluationListner.java
 * Copyright (c) 2002 Paul Dana
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 */

package com.garagegames.torque.tide;

import com.garagegames.torque.*;

// listen to an evaluation that is ready
public interface TideTorqueEvaluationListener
{
   // called when an evaluation is ready
   public void evaluationReady(String variable, String value);
}


