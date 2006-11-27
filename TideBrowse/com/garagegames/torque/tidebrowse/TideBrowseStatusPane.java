/*
 * TideBrowseStatusPane.java - TideBrowse status panel
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


import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.garagegames.torque.tidebrowse.model.ModelType;


/**
 * TideBrowse status panel
 * @author George Latkiewicz
 * @author Andre Kaplan
 * @version $Id: TideBrowseStatusPane.java,v 1.4 2003/12/23 11:31:06 cvsuser Exp $
**/
public class TideBrowseStatusPane extends JPanel
{
    private JLabel classLabel;
    private JLabel interfaceLabel;
    private JLabel attributeLabel;
    //private JLabel methodLabel;


    public TideBrowseStatusPane() {
        this.setLayout(new GridLayout(1, 4, 0, 1));
        classLabel     = new JLabel(ModelType.FUNCTION.getIcon());
        interfaceLabel = new JLabel(ModelType.DATABLOCK.getIcon());
        attributeLabel = new JLabel(ModelType.OBJECT.getIcon());
        //methodLabel    = new JLabel(ModelType.METHOD.getIcon());

        classLabel.setIconTextGap(2);
        interfaceLabel.setIconTextGap(2);
        attributeLabel.setIconTextGap(2);
        //methodLabel.setIconTextGap(2);

        classLabel.setToolTipText("functions");
        interfaceLabel.setToolTipText("datablocks");
        attributeLabel.setToolTipText("objects");
        //methodLabel.setToolTipText("methods");

        classLabel.setBorder(BorderFactory.createEtchedBorder());
        interfaceLabel.setBorder(BorderFactory.createEtchedBorder());
        attributeLabel.setBorder(BorderFactory.createEtchedBorder());
        //methodLabel.setBorder(BorderFactory.createEtchedBorder());

        Font monoFont = new Font("Monospaced", Font.PLAIN, 11);

        classLabel.setFont(monoFont);
        interfaceLabel.setFont(monoFont);
        attributeLabel.setFont(monoFont);
        //methodLabel.setFont(monoFont);

        this.add(classLabel);
        this.add(interfaceLabel);
        this.add(attributeLabel);
        //this.add(methodLabel);
    }


    public void showResults(TideBrowseParser.Results results) {
        if (results != null) { // required until File parser is implemented?
            // Update Status Bar
            this.classLabel.setText(    "" + results.getFunctionCount());
            this.interfaceLabel.setText("" + results.getInterfaceCount());
            this.attributeLabel.setText(
                "" + (results.getObjAttrCount() + results.getPrimAttrCount())
            );
            //this.methodLabel.setText(   "" + results.getMethodCount() );
        }
    }
}


/*
 * ChangeLog:
 * $Log: TideBrowseStatusPane.java,v $
 * Revision 1.4  2003/12/23 11:31:06  cvsuser
 * no message
 *
 * Revision 1.3  2003/12/16 22:29:12  cvsuser
 * no message
 *
 * Revision 1.2  2003/12/16 13:40:34  cvsuser
 * Removed some more unnecessary files.
 *
 * Revision 1.1.1.1  2002/07/22 12:37:18  beffy
 * Initial plugin import
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
