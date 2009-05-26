/*
 * ModelTreeSorter - Sorts an ModelTree
 * Copyright (c) 1999, 2000 Andre Kaplan
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


package com.garagegames.torque.tidebrowse;


import java.util.Arrays;
import java.util.Comparator;

import org.gjt.sp.jedit.MiscUtilities;


/**
 * A class to sort UML trees
 * @author Andre Kaplan
**/
public class ModelTreeSorter
{
    /**
     * Sort siblings of a UML Tree Node.
     * <OL>
     *     <LI>All siblings are put in an array
     *     <LI>Removed from the current node
     *     <LI>The array is sorted
     *     <LI>The sorted nodes are put back into the tree
     * </OL>
    **/
    public static void sort(ModelTree.Node n, Comparator c)
    {
        ModelTree.Node[] a = new ModelTree.Node[n.getChildCount()];

        for (int i = 0; i < n.getChildCount(); i++) {
            a[i] = (ModelTree.Node) n.getChildAt(i);
        }

        n.removeAllChildren();

        /* JDK1.2: Arrays.sort() */
        //MiscUtilities.quicksort(a, c);
        Arrays.sort(a, c);

        for (int i = 0; i < a.length; i++) {
            n.add(a[i]);
        }
    }
}


/*
 * ChangeLog:
 * $Log: ModelTreeSorter.java,v $
 * Revision 1.1  2003/12/16 13:41:36  cvsuser
 * Renamed some classes from UML... to Model....
 *
 * Revision 1.1.1.1  2002/07/22 12:37:18  beffy
 * Initial plugin import
 *
 * Revision 1.2  2001/05/30 23:55:37  akaplan
 * Updated TideBrowse package in java files
 *
 * Revision 1.1  2001/05/30 23:41:12  akaplan
 * Moved java source files to the TideBrowse package
 *
 * Revision 1.3  2001/01/18 18:04:33  akaplan
 * Updated for jEdit 3.0, converted to UNIX line endings, some bugs corrected, TideBrowse.Activator and JEditActivator removed
 *
 * Revision 1.4  2001/01/08 15:26:33  andre
 * Code cleanings
 *
 * Revision 1.3  2001/01/08 14:32:27  andre
 * Converted line endings to UNIX
 *
 * Revision 1.2  2000/11/19 12:49:17  andre
 * Fixed sort bug with JDK1.3 and Windows; changing the sort order doesn't collapse the TideBrowse tree anymore
 *
 * Revision 1.1.1.1  2000/11/12 14:31:21  andre
 * TideBrowse 1.3.1 initial import
 *
 * Revision 1.1  2000/09/02 22:41:20  akaplan
 * Sort UML Tree siblings
 *
*/

