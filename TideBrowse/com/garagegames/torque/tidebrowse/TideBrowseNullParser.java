/*
 * TideBrowseNullParser.java
 *
 * Copyright (c) 2000-2001 Andre Kaplan
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


public class TideBrowseNullParser implements TideBrowseParser
{
    private TideBrowseParser.LineSource source;


    public TideBrowseNullParser() {}


    public TideBrowseParser.Results parse() {
        return new TideBrowseParser.Results();
    }


    public TideBrowseParser.LineSource getSource() {
        return this.source;
    }


    public void setSource(TideBrowseParser.LineSource source) {
        this.source = source;
    }


    public String getSourceName() {
        if (this.source != null) {
            return this.source.getName();
        } else {
            return null;
        }
    }


    public String getSourcePath() {
        if (this.source != null) {
            return this.source.getPath();
        } else {
            return null;
        }
    }


    public void setRootNode(ModelTree.Node root) {
        root.setName(this.getSourceName() + " (NON-Torque file)");
    }


    public boolean isReady() { return true; }
}

