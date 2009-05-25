/*
 * TideBrowseOptionPane.java - TideBrowse options panel
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


import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

import com.garagegames.torque.tidebrowse.options.DisplayOptions;
import com.garagegames.torque.tidebrowse.options.MutableDisplayOptions;
import com.garagegames.torque.tidebrowse.options.MutableFilterOptions;
import com.garagegames.torque.tidebrowse.options.GeneralOptions;

import org.gjt.sp.jedit.AbstractOptionPane;

import org.gjt.sp.util.Log;


/**
 * TideBrowse option pane
 * @author George Latkiewicz
 * @author Andre Kaplan
 * @version $Id: TideBrowseOptionPane.java,v 1.2 2003/12/23 11:31:06 cvsuser Exp $
**/
public class TideBrowseOptionPane extends AbstractOptionPane
{
    // protected members inherited from AbstractOptionPane: y, gridBag

    // private state
    boolean isInitGui;
    boolean isInitModel;

    // private gui components

    // general options
    private JCheckBox cbxStatusBar;
    private JCheckBox cbxAutomaticParse;
    private JCheckBox cbxSort;
    private JCheckBox cbxAutocomplete;

    // display options
    private JCheckBox cbxShowIconKeywords;
    private JCheckBox cbxShowLineNum;

    private JComboBox cmbStyle;
    private int styleIndex = DisplayOptions.STYLE_UML;

    private JCheckBox cbxVisSymbols;
    private JCheckBox cbxAbstractItalic;
    private JCheckBox cbxStaticUlined;
    private JCheckBox cbxTypeIsSuffixed;

    // Options object
    private GeneralOptions        options    = new GeneralOptions();
    private MutableFilterOptions  filterOpt  = options.getFilterOptions();
    private MutableDisplayOptions displayOpt = options.getDisplayOptions();

    // Property Accessor
    private PropertyAccessor props;

    private boolean batchUpdate = false;

    // Listeners
    private ActionListener defaultAction       = null;
    private ActionListener updateOptionsAction = null;
    private ActionListener setOptionsAction    = null;

   // we are a singleton so that we can get the options from any class
   protected static TideBrowseOptionPane instance;
    public static TideBrowseOptionPane getInstance()
    {
      if (instance == null)
         instance = new TideBrowseOptionPane();
      return instance;
    }


    public TideBrowseOptionPane() {
        this("TideBrowse");
    }


    private TideBrowseOptionPane(String title) {
        super(title);
        setLayout(gridBag = new GridBagLayout());

        // It is the instantiating code's responsibility to call:
        // initGui(), initModel(), and setOptions() before displaying
        // Also either addDefaultListeners or addTideBrowseListeners must be
        // called to allow the GUI to correctly respond to user interaction
    }


    /**
     * AbstractOptionPane implementation (_init and _save)
    **/
    public void _init() {
        this.setPropertyAccessor(new JEditPropertyAccessor());
        options.load(props);

        initGui();   // setup display from property values
        initModel(); // set GUI to model (as defined in Options object)
    }


    /**
     * AbstractOptionPane implementation (_init and _save)
     * The method called by the File->Plugin Options save button for
     * setting the TideBrowse plugin options for all future sessions.
     * It saves the current view state to the associated property values.
    **/
    public void _save() {
        options.save(props);
    }


    PropertyAccessor getPropertyAccessor() { return props; }


    void setPropertyAccessor(PropertyAccessor props) {
        this.props = props;
    }


    public boolean isInitGui() { return isInitGui; }


    public boolean isInitModel() { return isInitModel; }


    /**
     * Sets this OptionPane's options object to the state specified by the
     * the OptionPane's associated PropertyAccessor.
    **/
    public void load() {
        batchUpdate = true;
        options.load(props);
        batchUpdate = false;
    }


    /**
     * Setup the GUI (with no current state).
     * This should only be called once in the constructor for this
     * TideBrowseOptionPane.
    **/
    private void initGui() {
        // -----
        // Title
        // -----
        JLabel titleLabel = new JLabel(
                props.getProperty("options." + getName() + ".panel_label") + ":",
                        JLabel.LEFT );
        titleLabel.setFont(new Font("Helvetica", Font.BOLD + Font.ITALIC, 13));

        addComponent(titleLabel);

        // ---------------
        // General Options
        // ---------------
        JPanel generalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 0));
        cbxStatusBar = new JCheckBox(
                props.getProperty("options.TideBrowse.showStatusBar"));
        generalPanel.add(cbxStatusBar);

        cbxAutomaticParse = new JCheckBox(
                props.getProperty("options.TideBrowse.automaticParse"));
        generalPanel.add(cbxAutomaticParse);

        cbxSort = new JCheckBox(
                props.getProperty("options.TideBrowse.sort"));
        generalPanel.add(cbxSort);

        cbxAutocomplete = new JCheckBox(
                props.getProperty("options.TideBrowse.autocomplete"));
        generalPanel.add(cbxAutocomplete);

        
        addComponent(generalPanel);

        // ---------------
        // Display Options
        // ---------------
        OptionPanel displayPanel = new OptionPanel();
        displayPanel.setBorder(this.createOptionBorder(
            " " + props.getProperty("options.TideBrowse.displayOptions") + " "
        ));

        /* class/interface modifiers */
        cbxShowIconKeywords = new JCheckBox(
                props.getProperty("options.TideBrowse.showIconKeywords"));
        displayPanel.addComponent(cbxShowIconKeywords);

        /* Line Numbers */
        cbxShowLineNum = new JCheckBox(
                props.getProperty("options.TideBrowse.showLineNums"));
        displayPanel.addComponent(cbxShowLineNum);

        addComponent(displayPanel);

        this.addDefaultListeners();

        isInitGui = true;
    }


    /**
     * This method sets the GUI representation of the model to the state
     * specified  by the current option object's state.
    **/
    public void initModel() {
        Log.log(Log.DEBUG, this, "initModel: " + this.getName());
        batchUpdate = true;

        // General Options
        cbxStatusBar.getModel().setSelected( options.getShowStatusBar() );
        cbxAutomaticParse.getModel().setSelected( options.getAutomaticParse() );
        cbxSort.getModel().setSelected( options.getSort() );
        cbxAutocomplete.getModel().setSelected( options.getAutocomplete() );
        

        cbxShowIconKeywords.getModel().setSelected(  displayOpt.getShowIconKeywords() );
        cbxShowLineNum.getModel().setSelected(       displayOpt.getShowLineNum() );


        isInitModel = true;
        batchUpdate = false;
    }


    public void addDefaultListeners() {
        ActionListener defaultListener = this.getDefaultAction();

        // general options
        this.cbxStatusBar.addActionListener(defaultListener);
        this.cbxStatusBar.addActionListener(defaultListener);
        this.cbxAutomaticParse.addActionListener(defaultListener);
        this.cbxSort.addActionListener(defaultListener);
        this.cbxAutocomplete.addActionListener(defaultListener);

        this.cbxShowIconKeywords.addActionListener(defaultListener);
        this.cbxShowLineNum.addActionListener(defaultListener);

    }


    public void removeDefaultListeners() {
        ActionListener defaultListener = this.getDefaultAction();

        // general options
        this.cbxStatusBar.removeActionListener(defaultListener);
        this.cbxStatusBar.removeActionListener(defaultListener);
        this.cbxAutomaticParse.removeActionListener(defaultListener);
        this.cbxSort.removeActionListener(defaultListener);
        this.cbxAutocomplete.removeActionListener(defaultListener);

        this.cbxShowIconKeywords.removeActionListener(defaultListener);
        this.cbxShowLineNum.removeActionListener(defaultListener);
    }


    /**
     * Allows this option pane to reflect any user interaction directly to
     * TideBrowse
    **/
    public void addTideBrowseListeners(TideBrowse TideBrowse) {
        // general options
        try
        {
           ActionListener statusBarOptionAction =
               this.createAction(TideBrowse.getStatusBarOptionAction());
           if(statusBarOptionAction != null)
              this.cbxStatusBar.addActionListener(statusBarOptionAction);

           ActionListener resizeAction =
               this.createAction(TideBrowse.getResizeAction());
           if(resizeAction != null)
              this.cbxStatusBar.addActionListener(resizeAction);
   
           if(this.getDefaultAction() != null)
              this.cbxAutomaticParse.addActionListener(this.getDefaultAction());
   
           ActionListener sortOptionAction =
               this.createAction(TideBrowse.getSortOptionAction());
           if(sortOptionAction != null)
              this.cbxSort.addActionListener(sortOptionAction);

           ActionListener autocompleteOptionAction =
               this.createAction(TideBrowse.getAutocompleteOptionAction());
           if(autocompleteOptionAction != null)
              this.cbxAutocomplete.addActionListener(autocompleteOptionAction);
           
           // display options
           ActionListener displayOptionAction =
               this.createAction(TideBrowse.getDisplayOptionAction());
           if(displayOptionAction != null)
           {
              this.cbxShowIconKeywords.addActionListener(displayOptionAction);
              this.cbxShowLineNum.addActionListener(displayOptionAction);
              this.cmbStyle.addActionListener(displayOptionAction);
           }
        }
        catch(Exception ex)
        {
           Log.log(Log.ERROR, this, "addTideBrowseListeners(): " + ex.getMessage());
        }
    }


    private ActionListener getDefaultAction() {
        if (this.defaultAction == null) {
            this.defaultAction = this.createDefaultAction();
        }
        return this.defaultAction;
    }


    private ActionListener getUpdateOptionsAction() {
        if (this.updateOptionsAction == null) {
            this.updateOptionsAction = new UpdateOptionsAction();
        }
        return this.updateOptionsAction;
    }


    private ActionListener getSetOptionsAction() {
        if (this.setOptionsAction == null) {
            this.setOptionsAction = new SetOptionsAction();
        }
        return this.setOptionsAction;
    }


    private ActionListener createDefaultAction() {
        return this.createAction(null);
    }


    private ActionListener createAction(ActionListener TideBrowseAction) {
        Condition batchUpdateCondition = new BatchUpdateCondition();

        if (TideBrowseAction == null) {
            return (
                new CompoundAction(
                    this.getUpdateOptionsAction(),
                    new ConditionalAction(
                        batchUpdateCondition,
                        null,
                        this.getSetOptionsAction()
                    )
                )
            );
        } else {
            return (
                new CompoundAction(
                    this.getUpdateOptionsAction(),
                    new ConditionalAction(
                        batchUpdateCondition,
                        null,
                        new CompoundAction(
                            this.getSetOptionsAction(), TideBrowseAction
                        )
                    )
                )
            );
        }
    }



    public GeneralOptions getOptions() { return options; }


    /**
     * The method that sets the option object's state to reflect the values
     * specified by the current state of the TideBrowseOptionPane.
    **/
    private void setOptions() {
        // General Options
        options.setShowStatusBar( cbxStatusBar.getModel().isSelected() );
        options.setAutomaticParse( cbxAutomaticParse.getModel().isSelected() );
        options.setSort( cbxSort.getModel().isSelected() );
        options.setAutocomplete( cbxAutocomplete.getModel().isSelected() );

        displayOpt.setShowIconKeywords( cbxShowIconKeywords.getModel().isSelected() );
        displayOpt.setShowLineNum( cbxShowLineNum.getModel().isSelected() );
    }


    /**
     * Adds a component to the TideBrowse option pane.
     * <ul>
     *   <li>Components are added in a vertical fashion, one per row</li>
     *   <li>Components fill the horizontal space</li>
     * </ul>
     * Overrides org.gjt.sp.jedit.AbstractOptionPane#addComponent(java.awt.Component)
    **/
    public void addComponent(Component comp) {
        GridBagConstraints cons = new GridBagConstraints();
        cons.gridy = y++;
        cons.gridheight = 1;
        cons.gridwidth = cons.REMAINDER;
        cons.fill = GridBagConstraints.HORIZONTAL;
        cons.anchor = GridBagConstraints.CENTER;
        cons.weightx = 1.0f;

        gridBag.setConstraints(comp,cons);
        add(comp);
    }


    /**
     * Creates the border of the option panels
    **/
    private Border createOptionBorder(String title) {
        Border border = BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createBevelBorder(BevelBorder.LOWERED),
                title, TitledBorder.CENTER, TitledBorder.TOP
            ),
            BorderFactory.createEmptyBorder(0, 3, 1, 1)
        );

        return border;
    }


    /**
     * Implements the option pane logic and interactions between components
    **/
    private class UpdateOptionsAction implements ActionListener {
        public void actionPerformed(ActionEvent evt)
        {
            Object actionSource = evt.getSource();

        }
    }


    /**
     * Action to encapsulate a call to TideBrowseOption.setOptions
    **/
    private class SetOptionsAction implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            TideBrowseOptionPane.this.setOptions();
        }
    }


    private class CompoundAction implements ActionListener {
        private ActionListener[] listeners;


        public CompoundAction(ActionListener listener1,
                              ActionListener listener2
        ) {
            this.listeners = new ActionListener[] {
                listener1, listener2
            };
        }


        public CompoundAction(ActionListener[] listeners) {
            this.listeners = listeners;
        }


        public void actionPerformed(ActionEvent evt) {
            for (int i = 0; i < this.listeners.length; i++) {
                if (this.listeners[i] != null) {
                    this.listeners[i].actionPerformed(evt);
                }
            }
        }
    }


    /**
     * Condition interface
    **/
    interface Condition {
        boolean test();
    }


    private class BatchUpdateCondition implements Condition {
        public boolean test() {
            return TideBrowseOptionPane.this.batchUpdate;
        }
    }


    private class ConditionalAction implements ActionListener {
        private Condition condition;
        private ActionListener trueAction;
        private ActionListener falseAction;


        public ConditionalAction(Condition condition,
                                 ActionListener trueAction,
                                 ActionListener falseAction
        ) {
            this.condition = condition;
            this.trueAction = trueAction;
            this.falseAction = falseAction;
        }


        public void actionPerformed(ActionEvent evt) {
            if (this.condition.test()) {
                if (this.trueAction != null) {
                    this.trueAction.actionPerformed(evt);
                }
            } else {
                if (this.falseAction != null) {
                    this.falseAction.actionPerformed(evt);
                }
            }
        }
    }


    /**
     * This class is used to for panels that require a gridBag layout for
     * placement into (for example) an OptionPane.
     */
    static class OptionPanel extends JPanel
    {
        /**
         * The layout manager.
         */
        protected GridBagLayout gridBag;

        /**
         * The number of components already added to the layout manager.
         */
        protected int y;

        /**
         * Creates a new option pane.
         * @param name The internal name
         */
        public OptionPanel() {
            setLayout(gridBag = new GridBagLayout());
        }


        /**
         * Adds a labeled component to the option pane.
         * @param label The label
         * @param comp The component
         */
        protected void addComponent(String label, Component comp) {
            GridBagConstraints cons = new GridBagConstraints();
            cons.gridy = y++;
            cons.gridheight = 1;
            cons.gridwidth = 3;
            cons.fill = GridBagConstraints.BOTH;
            cons.weightx = 1.0f;

            cons.gridx = 0;
            JLabel l = new JLabel(label,SwingConstants.RIGHT);
            gridBag.setConstraints(l,cons);
            add(l);

            cons.gridx = 3;
            cons.gridwidth = 1;
            gridBag.setConstraints(comp,cons);
            add(comp);
        }


        /**
         * Adds a component to the option pane.
         * @param comp The component
         */
        protected void addComponent(Component comp) {
            GridBagConstraints cons = new GridBagConstraints();
            cons.gridy = y++;
            cons.gridheight = 1;
            cons.gridwidth = cons.REMAINDER;
            cons.fill = GridBagConstraints.HORIZONTAL;
            cons.anchor = GridBagConstraints.WEST;
            cons.weightx = 1.0f;

            gridBag.setConstraints(comp,cons);
            add(comp);
        }
    }
}
