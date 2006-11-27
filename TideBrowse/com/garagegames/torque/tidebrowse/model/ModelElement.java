/*
 * ModelElement.java
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


public class ModelElement
{
    // The class which all elements that are to be displayed in a ModelTree
    // must inherit from.

    protected String name = null;
    protected ModelType type = null;
    protected int mod = 0;
    protected ModelElement parent = null;
    protected int line = -1;


    ModelElement(String name, ModelType type, int mod, ModelElement parent, int line) {
        this.name   = name;
        this.type   = type;
        this.mod    = mod;
        this.parent = parent;
        this.line   = line;
    }


    ModelElement(String name) { this.name = name; }


    public final ModelType getElementType() { return (ModelType) this.type; }
    public final void setElementType(ModelType type) { this.type = type; }


    public final ModelElement getParentElement() { return (ModelElement) parent; }
    public final void setParentElement(ModelElement e) { this.parent = parent; }


    public final ModelType getParentElementType() {
        if (parent == null) {
            return null;
        } else {
            return this.parent.getElementType();
        }
    }


    public int getLine() { return this.line; }


    public int getMod() { return this.mod; }


    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }


    public String toString(DisplayOptions displayOpt) { return this.name; }
    public String toString() { return this.name; }


    public boolean isVisible(FilterOptions filterOpt) {
        return true;
    }

    // "Interface" means a TGE "datablock"
    public final boolean isDatablock() {
        return MutableModifier.isDatablock(this.mod);
    }


    // "Function" means a TGE "function"
    public final boolean isFunction() {
        return MutableModifier.isFunction(this.mod);
    }
}

