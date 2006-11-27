/*
 * DisplayOptions.java - Immutable display options for TideBrowse
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


package com.garagegames.torque.tidebrowse.options;


/**
 * Interface for accessing Display options for TideBrowse
 * @author George Latkiewicz
 * @author Andre Kaplan
 * @version $Id: DisplayOptions.java,v 1.1.1.1 2002/07/22 12:37:18 beffy Exp $
**/
public interface DisplayOptions
{
    // Display Style options (HOW)

    // constants - for styleIndex
    int STYLE_FIRST  = 0;
    int STYLE_UML    = 0;
    int STYLE_JAVA   = 1;
    int STYLE_CUSTOM = 2;
    int STYLE_LAST   = 2;


    boolean getShowArguments();
    boolean getShowArgumentNames();
    boolean getShowNestedName();
    boolean getShowIconKeywords();
    boolean getShowMiscMod();
    boolean getShowLineNum();


    int getStyleIndex();


    boolean getVisSymbols();
    boolean getAbstractItalic();
    boolean getStaticUlined();
    boolean getTypeIsSuffixed();


    DisplayOptions getInverseOptions();
}

