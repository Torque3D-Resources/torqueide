/*
 * GeneralOptions.java - Options for TideBrowse
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

import com.garagegames.torque.tidebrowse.PropertyAccessor;

/**
 * TideBrowse General Options
 * @author George Latkiewicz
 * @author Andre Kaplan
 * @version $Id: GeneralOptions.java,v 1.2 2003/12/23 11:31:06 cvsuser Exp $
**/
public class GeneralOptions
{
    private boolean showStatusBar;
    private boolean automaticParse;
    private boolean autocomplete;
    private boolean sort;

    private MutableFilterOptions  filterOpt;  // (WHAT to display)
    private MutableDisplayOptions displayOpt; // (HOW  to display)


    public GeneralOptions() {
        this.filterOpt  = new MutableFilterOptions();
        this.displayOpt = new MutableDisplayOptions();
    }


    /**
     * The method that sets the option object's state to reflect the values
     * specified by the passed PropertyAccessor.
     */
    public void load(PropertyAccessor props) {
        // General Options
        setShowStatusBar(
            props.getBooleanProperty("TideBrowse.showStatusBar", true));
        setAutomaticParse(
            props.getBooleanProperty("TideBrowse.automaticParse", false));
        setSort(
            props.getBooleanProperty("TideBrowse.sort", false));
        setAutocomplete(
            props.getBooleanProperty("TideBrowse.autocomplete", false));


        // Display Options
        displayOpt.setShowIconKeywords(
                props.getBooleanProperty("TideBrowse.showIconKeywords"));
        displayOpt.setShowLineNum(
                props.getBooleanProperty("TideBrowse.showLineNums"));

    }


    /**
     * The method that sets the passed PropertyAccessor's state to reflect
     * the current state of this Options object.
     */
    public void save(PropertyAccessor props)
    {
        // General Options
        //----------------
        props.setBooleanProperty("TideBrowse.showStatusBar", getShowStatusBar());
        props.setBooleanProperty("TideBrowse.automaticParse", getAutomaticParse());
        props.setBooleanProperty("TideBrowse.sort", getSort());
        props.setBooleanProperty("TideBrowse.autocomplete", getAutocomplete());


        // Display Options
        //----------------
        props.setBooleanProperty("TideBrowse.showIconKeywords",
                displayOpt.getShowIconKeywords());
        props.setBooleanProperty("TideBrowse.showLineNums",
                displayOpt.getShowLineNum());

    }


    public final boolean getShowStatusBar() { return showStatusBar; }


    public final void setShowStatusBar(boolean flag) {
        showStatusBar = flag;
    }


    public final boolean getAutomaticParse() { return automaticParse; }


    public final void setAutomaticParse(boolean flag) {
        automaticParse = flag;
    }


    public final boolean getSort() { return sort; }


    public final void setSort(boolean flag) {
        sort = flag;
    }

    public final boolean getAutocomplete() { return autocomplete; }


    public final void setAutocomplete(boolean flag) {
        autocomplete = flag;
    }

    public final MutableFilterOptions  getFilterOptions()  { return filterOpt; }
    public final MutableDisplayOptions getDisplayOptions() { return displayOpt; }


    public final String toString() {
        return (filterOpt.toString() + "\n" + displayOpt.toString());
    }
}

