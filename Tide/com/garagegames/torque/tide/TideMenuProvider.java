package com.garagegames.torque.tide;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.menu.DynamicMenuProvider;
import org.gjt.sp.util.Log;
/**
 * An implementation of jEdit's DynamicMenuProvider to supply menu items at
 * runtime for the Tide plugin.
 */
public class TideMenuProvider implements DynamicMenuProvider
{
	//Constructors
	public TideMenuProvider() {
		super();
	}
	
	/**
	 * Re-create the Tide menu hierarchy.
	 */
	public void update(JMenu templatesMenu) {
		Log.log(Log.DEBUG,this,"... TideMenuProvider.update()");
		//templatesMenu.removeAll();
		templatesMenu.addSeparator();
		JMenu mi;
		mi = GUIUtilities.loadMenu("tidedebug.menu");
		templatesMenu.add(mi);
		templatesMenu.addSeparator();
		mi = GUIUtilities.loadMenu("tidebrowse.menu");
		templatesMenu.add(mi);

		/*		templatesMenu.addSeparator();
		mi = GUIUtilities.loadMenuItem("Templates.save-template");
		templatesMenu.add(mi);
		templatesMenu.addSeparator();*/
	}
	
	/**
	 * The Templates menu will not be updated every time it is shown.
	 * @return Always returns <code>false</code>.
	 */
	public boolean updateEveryTime()
	{
		return false;
	}
	
}
