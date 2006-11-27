/*
 * ModelFolderMember.java
 *
 * Copyright (c) 1999-2001 George Latkiewicz, Andre Kaplan
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/


package com.garagegames.torque.tidebrowse.model;


import com.garagegames.torque.tidebrowse.MutableModifier;
import com.garagegames.torque.tidebrowse.options.DisplayOptions;
import com.garagegames.torque.tidebrowse.options.FilterOptions;


// A top-level function, datablock or object - a "folder", so to speak...
public class ModelFolderMember extends ModelElement
{
    public ModelFolderMember(String name, ModelType type, int mod, int line) {
        super(name, type, mod, null, line);
    }


    public String toString(DisplayOptions displayOpt) {
        return (
              ((displayOpt.getShowLineNum()) ? ((line + 1) + ":") : "")
            + MutableModifier.toString(mod, displayOpt)
            + name
        );
    }


    public boolean isVisible(FilterOptions filterOpt) {
        boolean rVal = true;
        return rVal;
    }
}

