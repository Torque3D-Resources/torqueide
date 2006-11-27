/*
 * MutableFilterOptions.java - Filter options for TideBrowse
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
 * TideBrowse Filter options
 * @author George Latkiewicz
 * @author Andre Kaplan
 * @version $Id: MutableFilterOptions.java,v 1.1.1.1 2002/07/22 12:37:18 beffy Exp $
**/
public class MutableFilterOptions implements FilterOptions
{
    // Filter options (WHAT)

    private boolean showAttributes;
    private boolean showPrimitives;
    private boolean showGeneralizations;
    private boolean showThrows;

    private int topLevelVisIndex = 0;
    private int memberVisIndex   = 0;


    public final boolean getShowAttributes()      { return showAttributes; }
    public final boolean getShowPrimitives()      { return showPrimitives; }
    public final boolean getShowGeneralizations() { return showGeneralizations; }
    public final boolean getShowThrows()          { return showThrows; }


    public final int getTopLevelVisIndex()   { return topLevelVisIndex; }
    public final int getMemberVisIndex()     { return memberVisIndex; }


    public final void setShowAttributes(boolean flag) {
        showAttributes = flag;
    }


    public final void setShowPrimitives(boolean flag) {
        showPrimitives = flag;
    }


    public final void setShowGeneralizations(boolean flag) {
        showGeneralizations = flag;
    }


    public final void setShowThrows(boolean flag) {
        showThrows = flag;
    }


    public final void setTopLevelVisIndex(int level) {
        topLevelVisIndex = level;
    }


    public final void setMemberVisIndex(int level) {
        memberVisIndex = level;
    }


    public String toString() {
        return (
              "What to include:"
            + "\n\tshowAttributes      = " + showAttributes
            + "\n\tshowPrimitives      = " + showPrimitives
            + "\n\tshowGeneralizations = " + showGeneralizations
            + "\n\tshowThrows          = " + showThrows

            + "\n\ttopLevelVisIndex    = " + topLevelVisIndex
            + "\n\tmemberVisIndex      = " + memberVisIndex
        );
    }
}

