/*
 * ComparatorFactory.java
 * Copyright (c) 1999-2000 Andre Kaplan
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


import java.util.Hashtable;
import org.gjt.sp.jedit.MiscUtilities;

import com.garagegames.torque.tidebrowse.model.ModelType;


/**
 * A comparator factory
 * @author Andre Kaplan
 * @version $Id: ComparatorFactory.java,v 1.3 2003/12/23 11:31:06 cvsuser Exp $
**/
public class ComparatorFactory
{
    private static MiscUtilities.Compare defaultComparator =
        new ModelTreeDefaultComparator();
    private static MiscUtilities.Compare lineComparator =
         new ModelTreeLineComparator();


    public static MiscUtilities.Compare createModelTreeDefaultComparator() {
        return defaultComparator;
    }


    public static MiscUtilities.Compare createModelTreeLineComparator() {
        return lineComparator;
    }


    /**
     * Compares line numbers of two ModelTree nodes
    **/
    private static class ModelTreeLineComparator
        implements MiscUtilities.Compare
    {
        public int compare(Object o1,
                           Object o2)
        {
            int l1 = ModelTree.Node.getElement(o1).getLine();
            int l2 = ModelTree.Node.getElement(o2).getLine();

            if (l1 == l2) {
                return 0;
            } else if (l1 > l2) {
                return 1;
            } else {
                return -1;
            }
        }
    }


    /**
     * Compares names of two ModelTree nodes lexicographically
    **/
    private static class ModelTreeNameComparator
        implements MiscUtilities.Compare
    {
        public int compare(Object o1,
                           Object o2)
        {
            String s1 = ModelTree.Node.getElement(o1).getName();
            String s2 = ModelTree.Node.getElement(o2).getName();

            return s1.compareTo(s2);
        }
    }


    /**
     * Compares types of two ModelTree nodes.
     * Here is the default type hierarchy (from most to less important):
     * <ol>
     *   <li>DATABLOCK;</li>
     *   <li>FUNCTION;</li>
     *   <li>ERROR.</li>
     * </ol>
    **/
    private static class ModelTreeTypeComparator
        implements MiscUtilities.Compare
    {
        public int compare(Object o1,
                           Object o2)
        {
            Object io1 = nodeTypes.get((Object)ModelTree.Node.getElement(o1).getElementType());
            Object io2 = nodeTypes.get((Object)ModelTree.Node.getElement(o2).getElementType());

            int i1 = (io1 == null) ? nodeTypes.size() : ((Integer)io1).intValue();
            int i2 = (io2 == null) ? nodeTypes.size() : ((Integer)io2).intValue();

            if (i1 == i2) {
                return 0;
            } else if (i1 > i2) {
                return 1;
            } else {
                return -1;
            }
        }


        static private Hashtable nodeTypes = new Hashtable();

        static {
            int order = 0;
            nodeTypes.put(ModelType.DATABLOCK,   new Integer(order++));
            nodeTypes.put(ModelType.FUNCTION,       new Integer(order++));
            nodeTypes.put(ModelType.ERROR,       new Integer(order++));
        }
    }



    /**
     * Default comparator to sort ModelTree nodes. Multi-criteria sort order:
     * <ol>
     *  <li>By type (interface, classes, extensions, implementations, methods,
     *      inner or nested class, attributes, throws)</li>
     *  <li>By lexicographic order</li>
     *  <li>By access (public, package, protected, private)</li>
     * </ol>
    **/
    private static class ModelTreeDefaultComparator
        implements MiscUtilities.Compare
    {
        public int compare(Object o1,
                           Object o2)
        {
            return this.cmp.compare(o1, o2);
        }


        private MiscUtilities.Compare cmp =
            new CompoundComparator(
                new MiscUtilities.Compare[] {
                     new ModelTreeTypeComparator()
                    ,new ModelTreeNameComparator()
                }
            );
    }


    /**
     * Comparator to build multi-criteria comparators
    **/
    private static class CompoundComparator
        implements MiscUtilities.Compare
    {
        private MiscUtilities.Compare[] cmps = null;


        private CompoundComparator() {}


        public CompoundComparator(MiscUtilities.Compare[] comparators) {
            this.cmps = comparators;
        }


        public int compare(Object o1,
                           Object o2)
        {
            int cmp = 0;
            for (int i = 0; i < this.cmps.length; i++) {
                cmp = cmps[i].compare(o1, o2);
                if (cmp != 0) { return cmp; }
            }
            return cmp;
        }
    }
}

