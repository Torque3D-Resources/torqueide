/*
 * JEditLineSource.java
 *
 * Copyright (c) 1999 George Latkiewicz, 2000 Andre Kaplan
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


import org.gjt.sp.jedit.Buffer;

import org.gjt.sp.util.Log;


/**
 * Implements the functionality specified by the LineSource interface using
 * the set of lines made availble by a jEdit view and adds a method to return
 * the StartOffset.
 */
public class JEditLineSource implements TideBrowseParser.LineSource
{
    private Buffer buffer;   // jEdit specific
    private int    start;
    private int    lastLine; // last line that was read


    public JEditLineSource(Buffer buffer) {
        this.buffer = buffer;
        this.reset();
    }


    /**
     * Setup to become a newly initialized LineSource for the current buffer.
     */
    public void reset() {
        this.start = 0;
        this.lastLine = -1;
    }


    public final String getName() { return this.buffer.getName(); }


    public final String getPath() { return this.buffer.getPath(); }


    public final Object getObject() { return this.buffer; }


    public final Object createPosition(int offs) {
        return buffer.createPosition(offs);
    }


    public final String getLine(int lineIndex) {
        // ??? Note this should be cleaned up. Currently rely on returning empty
        // string when source is exhausted. Should actually throw exception in
        // the second case.
        // Probably should have an indexed line source vs. sequential line
        // source. The second would only allow calls to getNextLine and it
        // would keep track of the line number for the client.
        String lineString = "";

        // Sanity check
        if (lineIndex > this.buffer.getLineCount() - 1) {
            Log.log(Log.WARNING, this,
                "Argument to getLine() is bad: " + lineIndex
            );
            return ""; // source has been exhausted
        }

        lastLine = lineIndex;

        start = buffer.getLineStartOffset(lineIndex);
        lineString = buffer.getText(
            start, buffer.getLineEndOffset(lineIndex) - start - 1
        );

        return lineString;
    }


    public final boolean isExhausted() {
        return (lastLine >= this.buffer.getLineCount() - 1);
    }


    // this is specific to a JEditLineSource, should get rid of it ???
    public final int getStartOffset() { return this.start; }
}

