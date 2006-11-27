/*
 * class changed by Stefan "Beffy" Moises to work with Torque script files
 *
 * ModelType.java
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


import javax.swing.ImageIcon;


public class ModelType
{
    // used to represent Torque functions
    public static final ModelType FUNCTION       = new ModelType("function",
            new ImageIcon(ModelType.class.getResource("/com/garagegames/torque/tidebrowse/icons/Function.gif")));

    // used to represent Torque datablocks
    public static final ModelType DATABLOCK   = new ModelType("datablock",
            new ImageIcon(ModelType.class.getResource("/com/garagegames/torque/tidebrowse/icons/Datablock.gif")));

    // used to represent Torque objects
    public static final ModelType OBJECT   = new ModelType("object",
            new ImageIcon(ModelType.class.getResource("/com/garagegames/torque/tidebrowse/icons/Object.gif")));

    public static final ModelType ERROR       = new ModelType("ERROR",
            new ImageIcon(ModelType.class.getResource("/com/garagegames/torque/tidebrowse/icons/Error.gif")));



    // use POSSIBLE_VALUES to build an iterator
    public static final ModelType[] POSSIBLE_VALUES = {
        FUNCTION, DATABLOCK, OBJECT, ERROR
    };

    protected String label = null;
    transient protected ImageIcon icon = null;


    private ModelType(String label, ImageIcon icon) {
        this.label = label;
        this.icon = icon;
    }


    public ImageIcon getIcon() { return icon; }


    public String toString() { return label.toString(); }
}

