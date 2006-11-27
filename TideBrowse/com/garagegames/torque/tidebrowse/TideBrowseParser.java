/*
 * TideBrowseParser.java - Java Parser interface for TideBrowse
 *
 * Copyright (c) 1999 George Latkiewicz, 2000-2001 Andre Kaplan
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


import javax.swing.tree.TreePath;


/**
 * The interface that all TideBrowse parsers must implement, independent of how
 * they obtain their data and the type of source that parser analyzes.
 */
public interface TideBrowseParser
{
    /**
     * The method that preforms the actual parsing. This is the method which builds
     * the tree model that reflects the heirarchical structure of the Java code that
     * the parser has been configured to analyze.
     */
    public TideBrowseParser.Results parse();
    // this should latter throw a parser not ready exception.


    public LineSource getSource();


    public void setSource(LineSource source);


    public String getSourceName();


    public String getSourcePath();


    public void setRootNode(ModelTree.Node root);


    public boolean isReady();


    public static final String MISSING_LABEL = "<missing>";


    public static class Results {
        // Parse Counters
        private int functionCount     = 0;
        private int interfaceCount = 0;
        private int methodCount    = 0;
        private int objAttrCount   = 0;
        private int primAttrCount  = 0;
        private int errorCount     = 0;

        // Top-level (non-nested) public class/interface (or null)
        private TreePath topLevelPath = null;


        // Accessor Methods
        public int getFunctionCount() { return functionCount; }
        public int getInterfaceCount() { return interfaceCount; }
        public int getMethodCount() { return methodCount; }
        public int getObjAttrCount() { return objAttrCount; }
        public int getPrimAttrCount() { return primAttrCount; }
        public int getErrorCount() { return errorCount; }


        public TreePath getTopLevelPath() { return topLevelPath; }


        void setErrorCount(int count) {
            errorCount = count;
        }


        public void setTopLevelPath(TreePath path)
        {
            topLevelPath = path;
        }


        // Increment
        public void incFunctionCount() {
            functionCount++;
        }


        public void incInterfaceCount() {
            interfaceCount++;
        }


        public void incMethodCount() {
            methodCount++;
        }


        public void incObjAttrCount() {
            objAttrCount++;
        }


        public void incPrimAttrCount() {
            primAttrCount++;
        }


        public void incErrorCount() {
            errorCount++;
        }


        /**
         * This method resets all the result variables to their initial state,
         * i.e. all counts to 0, in anticipation of performing a new parse
         * which will use the result object to count what it finds.
          */
        void reset() {
            functionCount     = 0;
            interfaceCount = 0;
            methodCount    = 0;
            objAttrCount   = 0;
            primAttrCount  = 0;
            errorCount     = 0;

            topLevelPath = null;
        }
    }


    public static interface LineSource {
        public boolean isExhausted();


        // Returns the lines with the specified index from the associated line source,
        // or null if the index is out of range.
        public String getLine(int lineIndex);


        // Returns the offset of this line from the start
        public int getStartOffset();


        // Returns the name for this LineSource (e.g. file name)
        public String getName();


        // Returns the complete path for this LineSource
        public String getPath();


        // Returns the underlying object for this LineSource
        public Object getObject();


        // Setup to become a newly initialized LineSource for the current buffer
        public void reset();


        // Returns an object representing the position in the source
        // represented by the integer argument (offset)
        public Object createPosition(int offset);
    }
}

