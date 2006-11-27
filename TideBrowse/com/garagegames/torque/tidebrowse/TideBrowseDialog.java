/*
 * TideBrowseDialog.java
 * Copyright (c) 2000 Andre Kaplan
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


import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JPanel;

import org.gjt.sp.jedit.View;

import org.gjt.sp.util.Log;


/**
 * The class that defines the non-modal dialog window that provides the gui
 * for the TideBrowse plugin.
 * @author Andre Kaplan
 * @version $Id: TideBrowseDialog.java,v 1.1.1.1 2002/07/22 12:37:18 beffy Exp $
**/
public class TideBrowseDialog extends JDialog
{
    private TideBrowse TideBrowse;


    public TideBrowseDialog(View view)
    {
        // third option of false (i.e. non-modal) is the default for JDialog
        // constructor
        super(view, "TideBrowse", false);

        this.TideBrowse = new TideBrowse(view, new ResizeActionListener());

        Container contentPane = (JPanel) getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(BorderLayout.CENTER, this.TideBrowse);

        this.addWindowListener(
            new WindowAdapter() {
                public void windowClosed(WindowEvent e) {
                    TideBrowseDialog.this.TideBrowse.dispose();
                }
            }
        );

        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Determine size and position of the GUI
        Dimension screen = getToolkit().getScreenSize();
        pack();
        this.TideBrowse.validate();
        this.setPreferredSize();
        setLocation((screen.width - getSize().width) / 2,
                (screen.height - getSize().height) / 2);

        //show();
this.setVisible(true);
    }


    public Dimension getPreferredSize() {
        //this.tidebrowse.validate();
        Dimension d = this.TideBrowse.getPreferredSize();
        d.height = Math.max(d.height, this.getSize().height);
        return d;
    }


    public void setPreferredSize() {
        setSize(this.getPreferredSize());
        paintAll(getGraphics());
    }


    private class ResizeActionListener
        implements ActionListener
    {
        public ResizeActionListener() {}

        public void actionPerformed(ActionEvent evt) {
            setPreferredSize();
        }
    }
}


/*
 * ChangeLog:
 * $Log: TideBrowseDialog.java,v $
 * Revision 1.1.1.1  2002/07/22 12:37:18  beffy
 * Initial plugin import
 *
 * Revision 1.2  2001/05/30 23:55:37  akaplan
 * Updated TideBrowse package in java files
 *
 * Revision 1.1  2001/05/30 23:41:12  akaplan
 * Moved java source files to the TideBrowse package
 *
 * Revision 1.4  2001/01/18 18:04:33  akaplan
 * Updated for jEdit 3.0, converted to UNIX line endings, some bugs corrected, TideBrowse.Activator and JEditActivator removed
 *
 * Revision 1.4  2001/01/18 14:23:06  andre
 * TideBrowse.Activator and JEditActivator removed
 *
 * Revision 1.3  2001/01/08 15:26:33  andre
 * Code cleanings
 *
 * Revision 1.2  2001/01/08 14:32:26  andre
 * Converted line endings to UNIX
 *
 * Revision 1.1.1.1  2000/11/12 14:31:21  andre
 * TideBrowse 1.3.1 initial import
 *
 * Revision 1.2  2000/09/02 22:51:46  akaplan
 * TideBrowse 1.2: added sort option, automatic reparse feature, important internal changes, see Change_History.html for details
 *
*/
