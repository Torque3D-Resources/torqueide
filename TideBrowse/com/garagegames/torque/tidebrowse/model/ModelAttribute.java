/*
 * ModelAttribute.java
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


public class ModelAttribute extends ModelElement
{
    private String type;

    static final String PRIMITIVE_TYPES =
            ":boolean:char:byte:short:int:long:float:double:";


    public ModelAttribute(String name, String type, int mod, ModelElement parent, int line) {
        super(name, ModelType.OBJECT, mod, parent, line);

        this.type = type;
    }


    public final boolean isPrimitive() {
        if (    (name.indexOf("[") == -1)
             && (PRIMITIVE_TYPES.indexOf(":" + type + ":") != -1)
        ) {
            return true;
        } else {
            return false;
        }
    }


    public boolean isVisible(FilterOptions filterOpt) {
        boolean rVal = true;
        return rVal;
    }


    public String toString() {
        return ("" + (line + 1) + ":" + name + " : " + type);
    }


    public String toString(DisplayOptions displayOpt) {
        return (
              (displayOpt.getShowLineNum() ? ((line + 1) + ":") : "")
            + MutableModifier.toString(mod, displayOpt)
            + (displayOpt.getTypeIsSuffixed() ? (name + " : " + type) : (type + " " + name))
        );
    }
}

