/*
 * TideSearch.java
 *
 * Created on 18. Juli 2002, 23:19
 */

package com.garagegames.torque.tidebrowse;

import org.gjt.sp.jedit.search.*;
/**
 *
 * @author  Administrator
 */
public class TideSearch {
    
    /** Creates a new instance of TideSearch */
    public TideSearch() {
    }
    
    public boolean doSearch(String search, String dir)
    {
        
        SearchAndReplace snr = new SearchAndReplace();
        snr.setSearchFileSet(new DirectoryListSet(dir, "*.{cs,cc,h}", true));
        return true;
    }
}
