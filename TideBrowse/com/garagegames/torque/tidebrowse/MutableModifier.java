/*
 * MutableModifier.java - Read/Write extension to java.lang.reflect.Modifier
 *
 * Copyright (c) 1999 George Latkiewicz	(georgel@arvotek.net)
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
 *
*/

package com.garagegames.torque.tidebrowse;

import com.garagegames.torque.tidebrowse.options.DisplayOptions;

/**
 * Read/Write extension to java.lang.reflect.Modifier.
 */
public class MutableModifier
{
    private static final int FUNCTION = 1;
    private static final int DATABLOCK = 2;
    private static final int TGEOBJECT = 4;

    public static final boolean isFunction(int mod)
    {
        return (mod & FUNCTION) > 0;
    }
    public static final boolean isDatablock(int mod)
    {
        return (mod & DATABLOCK) > 0;
    }
    public static final boolean isTGEObject(int mod)
    {
        return (mod & TGEOBJECT) > 0;
    }

    // Accessors - set bit ON
    public static final int setFunction(int mod) { return (mod | FUNCTION); }
    public static final int setDatablock(int mod) { return (mod | DATABLOCK); }
    public static final int setTGEObject(int mod) { return (mod | TGEOBJECT); }

    // Accessors - set bit ON/OFF by parameter
    public static final int setFunction(int mod, boolean setFlag)
    {
        return (setFlag) ? (mod | FUNCTION) : (mod & ~FUNCTION) ;
    }
    public static final int setTGEObject(int mod, boolean setFlag)
    {
        return (setFlag) ? (mod | TGEOBJECT) : (mod & ~TGEOBJECT) ;
    }
    public static final int setDatablock(int mod, boolean setFlag)
    {
        return (setFlag) ? (mod | DATABLOCK) : (mod & ~DATABLOCK) ;
    }

    public static String toString(int mod, DisplayOptions displayOpt)
    {
        String rVal = "";

        if ( isTGEObject(mod) )           rVal += " ";

        if ( displayOpt.getShowIconKeywords() ) {
            if ( isFunction(mod) ) {
                rVal += "function ";
            } else if ( isDatablock(mod) ) {
                rVal += "datablock ";
            }
        }

        return rVal;

    }


}

