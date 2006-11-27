/*
 * TideBrowseActionPane.java
 *
 * Copyright (c) 1999-2000 George Latkiewicz, Andre Kaplan
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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.garagegames.torque.tidebrowse.model.ModelType;


/**
 * The TideBrowse action panel (or toolbar) on top of the TideBrowse panel.
 * @author George Latkiewicz
 * @author Andre Kaplan
 * @version $Id: TideBrowseActionPane.java,v 1.3 2003/12/16 13:40:34 cvsuser Exp $
*/
public class TideBrowseActionPane extends JPanel
{
    private JButton parseBtn  = null;
    private JButton resizeBtn = null;
    private JButton configBtn = null;
    private JButton searchBtn = null;
    private JButton helpBtn   = null;

    private JLabel errorLabel = null;


    private TideBrowseActionPane() {}


    public TideBrowseActionPane(TideBrowse TideBrowse) {
        Insets zeroMargin = new Insets(0, 0, 0, 0);

        // Parse button
        this.parseBtn = this.createButton("/com/garagegames/torque/tidebrowse/icons/Parse.gif", "Parse");
        this.parseBtn.setMargin(zeroMargin);
        this.parseBtn.setToolTipText("Parse the buffer");
        this.parseBtn.addActionListener(TideBrowse.getParseAction());

        // Resize button
        if (TideBrowse.getResizeAction() != null) {
            this.resizeBtn = this.createButton("/com/garagegames/torque/tidebrowse/icons/Resize.gif", "Resize");
            this.resizeBtn.setMargin(zeroMargin);
            this.resizeBtn.setToolTipText("Adjust width");
            this.resizeBtn.addActionListener(TideBrowse.getResizeAction());
        }

        // Options button
        this.configBtn = this.createButton("/com/garagegames/torque/tidebrowse/icons/Config.gif", "Config");
        this.configBtn.setMargin(zeroMargin);
        this.configBtn.setToolTipText("Set Options");
        this.configBtn.addActionListener(TideBrowse.getShowOptionsAction());
        
        // Search button 
        this.searchBtn = this.createButton("/com/garagegames/torque/tidebrowse/icons/Find.gif", "Find");
        this.searchBtn.setMargin(zeroMargin);
        this.searchBtn.setToolTipText("Search for selection ...");
        this.searchBtn.addActionListener(TideBrowse.getFindInProjectAction());
        
        // Help button
        this.helpBtn = this.createButton("/com/garagegames/torque/tidebrowse/icons/Help.gif", "Help");
        this.helpBtn.setMargin(zeroMargin);
        this.helpBtn.setToolTipText("Help");
        this.helpBtn.addActionListener(TideBrowse.getShowHelpAction());

        // Error Indicator
        this.errorLabel = new JLabel(ModelType.ERROR.getIcon());
        this.errorLabel.setIconTextGap(2);
        this.errorLabel.setFont(new Font("Helvetica", Font.PLAIN, 11));

        // Build Top Panel for BorderLayout
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 1, 1));

        leftPanel.add(this.parseBtn);
        if (this.resizeBtn != null) {
            leftPanel.add(this.resizeBtn);
        }
        leftPanel.add(this.configBtn);
        leftPanel.add(this.searchBtn);
        leftPanel.add(this.errorLabel);

        rightPanel.add(this.helpBtn);

        this.setLayout(new BorderLayout(0, 0));
        this.add(leftPanel,  BorderLayout.WEST);
        this.add(rightPanel, BorderLayout.EAST);
    }


    public void setErrorText(String errorText) {
        this.errorLabel.setText(errorText);
    }


    public void setErrorVisible(boolean visible) {
        this.errorLabel.setVisible(visible);
    }


    private JButton createButton(String image, String text) {
        java.net.URL url = this.getClass().getResource(image);
        if (url == null) {
            return new JButton(text);
        } else {
            return new JButton(new javax.swing.ImageIcon(url));
        }
    }
}


/*
 * ChangeLog:
 * $Log: TideBrowseActionPane.java,v $
 * Revision 1.3  2003/12/16 13:40:34  cvsuser
 * Removed some more unnecessary files.
 *
 * Revision 1.2  2002/07/23 21:49:33  cvsuser
 * no message
 *
 * Revision 1.1.1.1  2002/07/22 12:37:18  beffy
 * Initial plugin import
 *
 * Revision 1.4  2001/05/31 00:15:47  akaplan
 * Changed path to icons
 *
 * Revision 1.3  2001/05/31 00:06:21  akaplan
 * Action pane icons were not loaded
 *
 * Revision 1.2  2001/05/30 23:55:37  akaplan
 * Updated TideBrowse package in java files
 *
 * Revision 1.1  2001/05/30 23:41:12  akaplan
 * Moved java source files to the TideBrowse package
 *
 * Revision 1.4  2001/05/27 15:53:45  akaplan
 * Refactored UML.java:
 * - UML class replaced by uml package
 * - each class in the package has public visibility
 * - necessary changes in other classes
 *
 * Revision 1.3  2001/01/18 18:04:33  akaplan
 * Updated for jEdit 3.0, converted to UNIX line endings, some bugs corrected, TideBrowse.Activator and JEditActivator removed
 *
 * Revision 1.2  2001/01/08 15:26:33  andre
 * Code cleanings
 *
 * Revision 1.1.1.1  2000/11/12 14:31:21  andre
 * TideBrowse 1.3.1 initial import
 *
 * Revision 1.1  2000/09/02 22:34:57  akaplan
 * Action and Status panes moved outside TideBrowse
 *
*/
