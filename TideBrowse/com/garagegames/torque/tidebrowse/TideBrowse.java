/*
 *  TideBrowse.java - TideBrowse GUI and Engine
 *
 *  Copyright (c) 1999, 2000 George Latkiewicz
 *  Copyright (c) 1999, 2000, 2001, 2002 Andre Kaplan
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.garagegames.torque.tidebrowse;

import com.garagegames.torque.tidebrowse.options.DisplayOptions;
import com.garagegames.torque.tidebrowse.options.GeneralOptions;
import com.garagegames.torque.tidebrowse.options.MutableDisplayOptions;
import com.garagegames.torque.tidebrowse.options.MutableFilterOptions;

import com.garagegames.torque.tidebrowse.sidekick.*;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Comparator;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.Position;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EBComponent;
import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.EditPane;
import org.gjt.sp.jedit.EditPlugin;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.help.*;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.msg.BufferUpdate;
import org.gjt.sp.jedit.msg.EditPaneUpdate;

import org.gjt.sp.jedit.search.*;
import org.gjt.sp.jedit.textarea.Selection;
import org.gjt.sp.util.Log;
import projectviewer.*;
import projectviewer.config.*;
import projectviewer.event.*;
import projectviewer.vpt.*;
/**
 *  The class that defines the main gui panel for the TideBrowse plugin.
 *
 *@author     beffy
 *@created    15. Dezember 2003
 *@version    $Id: TideBrowse.java,v 1.8 2003/12/16 13:40:34 cvsuser Exp $
 */
public class TideBrowse extends JPanel implements EBComponent {
   /*
    *  public instance attributes
    */
   /**
    *  Description of the Field
    */
   public TideBrowseParser.Results results;

   /*
    *  private instance attributes
    */
   private View view;
   private PropertyAccessor props;
   private TideBrowseParser parser;

   private ModelTree umlTree;
   private ModelTree.Model treeModel;
   private ModelTree.Node root;

   // status and action bar
   private TideBrowseStatusPane statusPanel;
   private TideBrowseActionPane actionPanel;

   private TideBrowseOptionDialog optionDialog;
   private GeneralOptions options;
   private MutableFilterOptions filterOpt;
   private MutableDisplayOptions displayOpt;

   private ActionListener resizeAction = null;
   private ActionListener parseAction = null;
   private ActionListener showOptionsAction = null;
   private ActionListener findInProjectAction = null;
   private ActionListener showHelpAction = null;

   private ActionListener statusBarOptionAction = null;
   private ActionListener sortOptionAction = null;
   private ActionListener filterOptionAction = null;
   private ActionListener autocompleteOptionAction = null;
   private ActionListener displayOptionAction = null;

   private ModelTreeHandler umlTreeHandler = null;
   private TreeExpansionListener umlTreeExpansionHandler = null;
   private FocusListener textAreaFocusHandler = null;

   private ComponentListener componentShownHandler = null;

   private JScrollPane scpTreePane;

   private Runnable parseRunnable =
      new Runnable() {
         public void run() {
            TideBrowse.this.results = TideBrowse.this.parser.parse();
            TideBrowse.this.showResults(TideBrowse.this.results);
         }
      };


   /**
    *  Main TideBrowse constructor
    *
    *@param  view    Description of the Parameter
    *@param  resize  Description of the Parameter
    */
   public TideBrowse(View view, ActionListener resize) {
      super();
      this.init(view, resize);

      String fileName = this.parser.getSourceName();

      // set root node to file name
      if(!(fileName.toLowerCase().endsWith(".cs") || fileName.toLowerCase().endsWith(".gui") || fileName.toLowerCase().endsWith(".mis"))) {
         fileName += " (NON-Torque file)";
         this.actionPanel.setErrorText("Un-parsed");
         this.actionPanel.validate();
      }

      this.initUI();
      this.initTree(fileName);

      // Configure Parser
      this.parser.setRootNode(this.root);

      this.parse();
   }


   /**
    *  This method creates a new instance of TideBrowse GUI and Parsing engine.
    *
    *@param  view    Description of the Parameter
    *@param  resize  Description of the Parameter
    */
   private void init(View view, ActionListener resize) {
      this.view = view;
      this.props = new JEditPropertyAccessor();

      Buffer buffer = view.getBuffer();
      TideBrowseParser.LineSource lineSource = new JEditLineSource(buffer);

      if(buffer.getName().toLowerCase().endsWith(".cs") || buffer.getName().toLowerCase().endsWith(".gui") || buffer.getName().toLowerCase().endsWith(".mis")) {
         this.parser = new TideBrowseLineParser();
      }
      else {
         this.parser = new TideBrowseNullParser();
      }

      this.parser.setSource(lineSource);

      this.umlTree = new ModelTree();
      this.umlTreeHandler = new ModelTreeHandler();

      this.resizeAction = resize;

      this.actionPanel = new TideBrowseActionPane(this);
      this.statusPanel = new TideBrowseStatusPane();

      this.setLayout(new BorderLayout());

      // Connect to the optionPane's options
      this.optionDialog =
            new TideBrowseOptionDialog(this, "TideBrowse - Configure Options");
      this.options = this.optionDialog.getOptions();
      this.filterOpt = this.options.getFilterOptions();
      this.displayOpt = this.options.getDisplayOptions();
   }


   /**
    *  Description of the Method
    */
   private void initUI() {
      this.removeAll();

      if(this.scpTreePane == null) {
         this.scpTreePane = new JScrollPane();
      }
      this.scpTreePane.setViewportView(this.umlTree);

      this.add(this.actionPanel, BorderLayout.NORTH);
      this.add(this.scpTreePane, BorderLayout.CENTER);
      this.add(this.statusPanel, BorderLayout.SOUTH);

      if(this.options.getShowStatusBar()) {
         this.statusPanel.setVisible(true);
      }
      else {
         this.statusPanel.setVisible(false);
      }
   }


   /**
    *  The initialization process for the tree, called as the last step of the
    *  constructor.
    *
    *@param  fileName  Description of the Parameter
    */
   private void initTree(String fileName) {
      // Setup Tree
      this.root = new ModelTree.Node(fileName);

      // get tree model and tree
      this.treeModel = new ModelTree.Model(this.root);
   }


   /**
    *  Adds a feature to the Notify attribute of the TideBrowse object
    */
   public void addNotify() {
      super.addNotify();
      EditBus.addToBus(this);
      this.addHandlers();
   }


   /**
    *  Description of the Method
    */
   public void removeNotify() {
      super.removeNotify();
      EditBus.removeFromBus(this);
      this.removeHandlers();
   }


   /**
    *  Description of the Method
    */
   public void dispose() {
      this.optionDialog.dispose();
   }


   /**
    *  Gets the displayOptions attribute of the TideBrowse object
    *
    *@return    The displayOptions value
    */
   public DisplayOptions getDisplayOptions() {
      return this.displayOpt;
   }


   /**
    *  Gets the generalOptions attribute of the TideBrowse object
    *
    *@return    The generalOptions value
    */
   public GeneralOptions getGeneralOptions() {
      return this.options;
   }


   /**
    *  Gets the propertyAccessor attribute of the TideBrowse object
    *
    *@return    The propertyAccessor value
    */
   public PropertyAccessor getPropertyAccessor() {
      return this.props;
   }


   /**
    *  Sets the docked attribute of the TideBrowse object
    *
    *@param  docked  The new docked value
    */
   public void setDocked(boolean docked) {
      if(docked && (this.componentShownHandler == null)) {
         this.componentShownHandler = new ComponentShownHandler();
         this.addComponentListener(this.componentShownHandler);
      }
      else if(!docked && (this.componentShownHandler != null)) {
         this.removeComponentListener(this.componentShownHandler);
         this.componentShownHandler = null;
      }
   }


   /**
    *  Description of the Method
    */
   private void parse() {
      this.parse(this.view, this.view.getBuffer());
   }


   /**
    *  Description of the Method
    *
    *@param  view    Description of the Parameter
    *@param  buffer  Description of the Parameter
    */
   private void parse(View view, Buffer buffer) {
      if(buffer.getName().toLowerCase().endsWith(".cs") || buffer.getName().toLowerCase().endsWith(".gui") || buffer.getName().toLowerCase().endsWith(".mis")) {
         if(buffer.isLoaded()) {
            TideBrowseParser.LineSource lineSource = new JEditLineSource(buffer);
            this.parser.setSource(lineSource);
            SwingUtilities.invokeLater(TideBrowse.this.parseRunnable);
         }
         else {
            this.results = new TideBrowseParser.Results();
            this.showResults(this.results);
            EditBus.addToBus(new BufferParseAction(view, buffer));
         }
      }
      else {
         this.results = new TideBrowseParser.Results();
         this.showResults(this.results);
      }
   }



   /**
    *  Search for current selection in the last/recent project. Requires
    *  ProjectViewer plugin! (-> dependency in TideBrowse.props and
    *  build.xml!!!) Added by Stefan "Beffy" Moises 07/19/2002 Fixed for current
    *  ProjectViewer 2.0.2 / JEdit 4.1, 12/12/2003
    *
    *@param  view    Description of the Parameter
    *@param  buffer  Description of the Parameter
    *@return         Description of the Return Value
    */
   public static boolean doSearch(View view, Buffer buffer) {
      // current selection in buffer...
      String currSelectionStr = view.getTextArea().getSelectedText();
      // if there is no selection, return
      if(currSelectionStr == null || currSelectionStr.equals("") || currSelectionStr.length() == 0) {
         Log.log(Log.DEBUG, view, "TideBrowse - Nothing selected!");
         return false;
      }
      Log.log(Log.DEBUG, view, "TideBrowse - Searching for: " + currSelectionStr);

      //return doFunctionSearch(view, buffer);
      return submitSearch(view, currSelectionStr, false);
   }


   /**
    *  Description of the Method
    *
    *@param  view    Description of the Parameter
    *@param  buffer  Description of the Parameter
    */
   public static void createProjectAutocomplete(View view, Buffer buffer) {
      TideSideKickParser.createProjectAutocomplete();
   }


   /**
    *  Description of the Method
    *
    *@param  view       Description of the Parameter
    *@param  searchStr  Description of the Parameter
    *@param  regExp     Description of the Parameter
    *@return            Description of the Return Value
    */
   public static boolean submitSearch(View view, String searchStr, boolean regExp) {
      // get the ProjectManager instance
      ProjectManager pm = ProjectManager.getInstance();
      // get the ProjectViewer instance
      //ProjectViewer viewer = ProjectViewer.getInstance(view);
      ProjectViewer viewer = ProjectViewer.getViewer(view);
      // get current project
      VPTProject currProject = ProjectViewer.getActiveProject(view);//PVActions.getCurrentProject(view);

      // now get the current projects' root dir...
      String rootDir = currProject.getRootPath();

      Log.log(Log.DEBUG, view, "Current Project: " + currProject);
      Log.log(Log.DEBUG, view, "Current Project Root: " + rootDir);

      // instantiate SearchAndReplace...
      SearchAndReplace.setIgnoreCase(true);

      // use the global ProjectManager file types:
      String importExts = ProjectViewerConfig.getInstance().getImportGlobs();//.getImportExts();
      String commaSep = importExts.replace(' ', ',');
      String fileSpec = "*.{" + commaSep + "}";
      if(regExp)
    	  fileSpec = "*.{cs,gui}";
      Log.log(Log.DEBUG, view, "File Spec: " + fileSpec);
      SearchAndReplace.setSearchFileSet(new DirectoryListSet(rootDir, fileSpec, true));

      //snr.setSearchFileSet(new DirectoryListSet(rootDir, "*.{cs,gui,mis,dml,cc,h,cpp,c,asm}", true));

      SearchAndReplace.setSearchString(searchStr);
      SearchAndReplace.setBeanShellReplace(false);
      SearchAndReplace.setRegexp(regExp);
      // do the search...
      boolean searchSuccess = SearchAndReplace.hyperSearch(view, false);
      if(searchSuccess) {
         try {
            final HyperSearchResults results = (HyperSearchResults)
                  view.getDockableWindowManager()
                  .getDockable(HyperSearchResults.NAME);
            int resultCount = results.getTreeModel().getChildCount(results.getTreeModel().getRoot());
            Log.log(Log.DEBUG, view, "resultCount: " + resultCount);
         }
         catch(Exception ex) {
            Log.log(Log.ERROR, view, "Error while evaluating results: " + ex.getLocalizedMessage());
         }
      }
      return searchSuccess;
   }


   /**
    *  Description of the Method
    *
    *@param  view    Description of the Parameter
    *@param  buffer  Description of the Parameter
    *@return         Description of the Return Value
    */
   public static boolean doFunctionSearch(View view, Buffer buffer) {
      String currSelectionStr = view.getTextArea().getSelectedText();
      // if there is no selection, return
      if(currSelectionStr == null || currSelectionStr.equals("") || currSelectionStr.length() == 0) {
         Log.log(Log.DEBUG, view, "TideBrowse - Nothing selected!");
         return false;
      }
      // build regExp string to search for: function ... SELECTION ... (...)
      //currSelectionStr = "function" + currSelectionStr + "\\s*?" + "[[:punct:]].*[[:punct:]]";
      //currSelectionStr = "function" + "[\\s*?][a-z|0-9|:]*?" + currSelectionStr + "\\s*?" + "[[:punct:]].*[[:punct:]]";
      currSelectionStr = "function" + "[\\s*?][a-z|0-9|:]*?" + currSelectionStr + "\\s*?" + "\\(.*\\)";
      // e.g.:  [a-z|0-9]*[\s*|::]createPlayer\s*[[:punct:]].*[[:punct:]]
      Log.log(Log.DEBUG, view, "TideBrowse - Searching for: " + currSelectionStr);
      return submitSearch(view, currSelectionStr, true);
   }


   /**
    *  Reparse the current buffer
    */
   private void reparse() {
      this.reparse(this.view, this.view.getBuffer());
   }


   /**
    *  Reparse the given buffer in the given view
    *
    *@param  view    Description of the Parameter
    *@param  buffer  Description of the Parameter
    */
   private void reparse(View view, Buffer buffer) {
      if(buffer.getName().toLowerCase().endsWith(".cs") || buffer.getName().toLowerCase().endsWith(".gui") || buffer.getName().toLowerCase().endsWith(".mis")) {
         if(this.parser instanceof TideBrowseNullParser) {
            TideBrowseParser.LineSource lineSource = new JEditLineSource(buffer);
            this.parser = new TideBrowseLineParser();
            this.parser.setSource(lineSource);

            this.initUI();
            this.initTree(this.parser.getSourceName());

            this.parser.setRootNode(this.root);
         }

         if(buffer.isLoaded()) {
            TideBrowseParser.LineSource lineSource = new JEditLineSource(buffer);
            this.parser.setSource(lineSource);
            SwingUtilities.invokeLater(TideBrowse.this.parseRunnable);
         }
         else {
            this.results = new TideBrowseParser.Results();
            this.showResults(this.results);
            EditBus.addToBus(new BufferParseAction(view, buffer));
         }
      }
   }


   /**
    *  Reparse the current buffer if the automatic reparse option is enabled
    */
   public void automaticReparse() {
      this.automaticReparse(this.view, this.view.getBuffer());
   }


   /**
    *  Reparse the given buffer in the given view if the automatic reparse
    *  option is enabled
    *
    *@param  view    Description of the Parameter
    *@param  buffer  Description of the Parameter
    */
   private void automaticReparse(View view, Buffer buffer) {
      boolean automaticParse = this.options.getAutomaticParse();
      if(automaticParse && this.isShowing()) {
         this.reparse(view, buffer);
      }
   }


   /**
    *  Shows the option dialog
    */
   private void showOptions() {
      this.optionDialog.init();

      // Determine size and position of the OptionDialog
      Dimension screen = getToolkit().getScreenSize();
      Dimension optSize = this.optionDialog.getSize();
      // JDK 1.2: replace getLocation().x with getX()
      int treeX = this.getLocation().x;
      int optY = (screen.height - optSize.height) / 2;

      if(treeX + this.getSize().width + 12 + optSize.width > screen.width) {
         // i.e. won't fit at right so...
         if(treeX - optSize.width - 12 < 0) {
            // i.e. won't fit at left either so overlap at right end
            this.optionDialog.setLocation(screen.width - optSize.width - 2, optY);
         }
         else {
            // left of
            this.optionDialog.setLocation(treeX - optSize.width - 10, optY);
         }
      }
      else {
         // right of
         this.optionDialog.setLocation(treeX + getSize().width + 10, optY);
      }

      this.optionDialog.setVisible(true);
      this.optionDialog.paintAll(this.optionDialog.getGraphics());

      // Repaint the TideBrowse Dialog, required to eliminate
      // any contamination from the OptionDialog (really!)
      this.paintAll(this.getGraphics());
   }


   /**
    *  Shows TideBrowse help in the jEdit help viewer
    */
   private void showHelp() {
      java.net.URL helpUrl = TideBrowse.class.getResource("/TideBrowse.html");
      if(helpUrl == null) {
         Log.log(Log.NOTICE, this,
               "Help URL is null, cannot display help");
      }
      else {
         new org.gjt.sp.jedit.help.HelpViewer(helpUrl.toString());
      }
   }


   /**
    *  EBComponent implementation Handles
    *  <UL>
    *    <LI> EditPaneUpdate.BUFFER_CHANGED: Calls for automatic reparse
    *    <LI> EditPaneUpdate.CREATED: Adds to the EditPane textArea a TideBrowse
    *    focus listener to do automatic reparse on focus
    *    <LI> EditPaneUpdate.DESTROYED: Removes from the focus listener
    *  </UL>
    *
    *
    *@param  message  Description of the Parameter
    */
   public void handleMessage(EBMessage message) {
      if(message instanceof EditPaneUpdate) {
         EditPaneUpdate epu = (EditPaneUpdate) message;
         //View v = ((EditPane) epu.getSource()).getView();
         View v = jEdit.getActiveView();

         Log.log(Log.DEBUG, this, "***** EditPaneUpdate " + v);
         if(v == this.view) {
            if(epu.getWhat() == EditPaneUpdate.CREATED) {
               epu.getEditPane().getTextArea().addFocusListener(
                     this.getTextAreaFocusHandler()
                     );
            }
            else if(epu.getWhat() == EditPaneUpdate.DESTROYED) {
               epu.getEditPane().getTextArea().removeFocusListener(
                     this.getTextAreaFocusHandler()
                     );
            }
            else if(epu.getWhat() == EditPaneUpdate.BUFFER_CHANGED) {
               this.automaticReparse();
            }
         }
      }
   }


   /**
    *  Gets the preferredSize attribute of the TideBrowse object
    *
    *@return    The preferredSize value
    */
   public Dimension getPreferredSize() {
      actionPanel.validate();
      Dimension dPanel = this.getSize();
      Dimension dNewPanel = new Dimension(0, 0);
      Dimension dViewCur = scpTreePane.getViewport().getSize();
      Dimension dViewPref = scpTreePane.getViewport().getPreferredSize();
      Dimension dTreePref = umlTree.getPreferredSize();

      dNewPanel.height += actionPanel.getPreferredSize().height;

      // Set vertical scrollbar to visible, if it should be
      if(dViewCur.height < dViewPref.height) {
         scpTreePane.getVerticalScrollBar().setVisible(true);
      }

      // Set width to preferred width based on view and scroll bar
      if(scpTreePane.getVerticalScrollBar().isVisible()) {
         // i.e. 15 = scpTreePane.getVerticalScrollBar().getSize().width;
         dNewPanel.width = dViewPref.width + 12 + 16;
      }
      else {
         dNewPanel.width = dViewPref.width + 12;
      }
      dNewPanel.height += dViewPref.height;

      // Adjust width for status panel, if visible
      if(statusPanel.isVisible()) {
         statusPanel.validate();
         dNewPanel.width = Math.max(dNewPanel.width, statusPanel.getPreferredSize().width + 8);
         dNewPanel.height += statusPanel.getPreferredSize().height;
      }

      // Adjust width for top panel
      dNewPanel.width = Math.max(dNewPanel.width, actionPanel.getPreferredSize().width + 8);
      dNewPanel.height = Math.max(dNewPanel.height, dPanel.height);
      return dNewPanel;
   }


   /**
    *  Sets the preferredSize attribute of the TideBrowse object
    */
   public void setPreferredSize() {
      setSize(this.getPreferredSize());
      paintAll(getGraphics());
   }


   /**
    *  Gets the statusVisible attribute of the TideBrowse object
    *
    *@return    The statusVisible value
    */
   public boolean isStatusVisible() {
      return this.statusPanel.isVisible();
   }


   /**
    *  Sets the statusVisible attribute of the TideBrowse object
    *
    *@param  visible  The new statusVisible value
    */
   public void setStatusVisible(boolean visible) {
      this.statusPanel.setVisible(visible);
   }


   /**
    *  This method should be called after every parse. It updates the text of
    *  the status bar's labels to the results counts, displays the error
    *  indicator (if appropriate) and then calls umlTree.display() to reload the
    *  tree.
    *
    *@param  results  Description of the Parameter
    */
   public void showResults(TideBrowseParser.Results results) {
      umlTree.display(treeModel, options, results);

      if(results != null) {
         // required until File parser is implemented?
         // Update Status Bar
         this.statusPanel.showResults(results);

         // Update Parse Error Indicator
         if(results.getErrorCount() > 0) {
            actionPanel.setErrorText(results.getErrorCount() + " error(s)");
            actionPanel.setErrorVisible(true);
            actionPanel.validate();

            Dimension dFrame = getSize();
            Dimension dActionPanel = actionPanel.getPreferredSize();
            if(dFrame.width < dActionPanel.width + 8) {
               setPreferredSize();
            }
         }
         else {
            actionPanel.setErrorVisible(false);
         }
      }
   }


   /**
    *  Adds the listeners to provide the sort and automatic reparse features
    */
   private void addHandlers() {
      this.umlTree.addTreeSelectionListener(this.umlTreeHandler);
      this.umlTree.addMouseListener(this.umlTreeHandler);

      this.umlTree.addTreeExpansionListener(
            this.getModelTreeExpansionHandler()
            );

      EditPane[] editPanes = this.view.getEditPanes();
      for(int i = 0; i < editPanes.length; i++) {
         editPanes[i].getTextArea().addFocusListener(
               this.getTextAreaFocusHandler()
               );
      }
   }


   /**
    *  Removes the listeners that provided the sort and automatic reparse
    *  features
    */
   private void removeHandlers() {
      this.umlTree.removeTreeSelectionListener(this.umlTreeHandler);
      this.umlTree.removeMouseListener(this.umlTreeHandler);

      this.umlTree.removeTreeExpansionListener(
            this.getModelTreeExpansionHandler()
            );

      EditPane[] editPanes = this.view.getEditPanes();
      for(int i = 0; i < editPanes.length; i++) {
         editPanes[i].getTextArea().removeFocusListener(
               this.getTextAreaFocusHandler()
               );
      }
   }


   /**
    *  Gets the resizeAction attribute of the TideBrowse object
    *
    *@return    The resizeAction value
    */
   public ActionListener getResizeAction() {
      return this.resizeAction;
   }


   /**
    *  Gets the parseAction attribute of the TideBrowse object
    *
    *@return    The parseAction value
    */
   public ActionListener getParseAction() {
      if(this.parseAction == null) {
         this.parseAction =
            new ActionListener() {
               public void actionPerformed(ActionEvent evt) {
                  TideBrowse.this.reparse();
               }
            };
      }

      return this.parseAction;
   }


   /**
    *  Gets the showOptionsAction attribute of the TideBrowse object
    *
    *@return    The showOptionsAction value
    */
   public ActionListener getShowOptionsAction() {
      if(this.showOptionsAction == null) {
         this.showOptionsAction =
            new ActionListener() {
               public void actionPerformed(ActionEvent evt) {
                  TideBrowse.this.showOptions();
               }
            };
      }

      return this.showOptionsAction;
   }


   /**
    *  Gets the findInProjectAction attribute of the TideBrowse object
    *
    *@return    The findInProjectAction value
    */
   public ActionListener getFindInProjectAction() {
      if(this.findInProjectAction == null) {
         this.findInProjectAction =
            new ActionListener() {
               public void actionPerformed(ActionEvent evt) {
                  TideBrowse.this.doSearch(view, jEdit.getFirstBuffer());
               }
            };
      }

      return this.findInProjectAction;
   }


   /**
    *  Gets the showHelpAction attribute of the TideBrowse object
    *
    *@return    The showHelpAction value
    */
   public ActionListener getShowHelpAction() {
      if(this.showHelpAction == null) {
         this.showHelpAction =
            new ActionListener() {
               public void actionPerformed(ActionEvent evt) {
                  TideBrowse.this.showHelp();
               }
            };
      }

      return this.showHelpAction;
   }


   /**
    *  Gets the statusBarOptionAction attribute of the TideBrowse object
    *
    *@return    The statusBarOptionAction value
    */
   public ActionListener getStatusBarOptionAction() {
      if(this.statusBarOptionAction == null) {
         this.statusBarOptionAction = new StatusBarOptionAction();
      }

      return this.statusBarOptionAction;
   }


   /**
    *  Gets the sortOptionAction attribute of the TideBrowse object
    *
    *@return    The sortOptionAction value
    */
   public ActionListener getSortOptionAction() {
      if(this.sortOptionAction == null) {
         this.sortOptionAction = new SortOptionAction();
      }

      return this.sortOptionAction;
   }


   /**
    *  Gets the autocompleteOptionAction attribute of the TideBrowse object
    *
    *@return    The autocompleteOptionAction value
    */
   public ActionListener getAutocompleteOptionAction() {
      if(this.autocompleteOptionAction == null) {
         this.autocompleteOptionAction = new AutocompleteOptionAction();
      }

      return this.autocompleteOptionAction;
   }


   /**
    *  Gets the filterOptionAction attribute of the TideBrowse object
    *
    *@return    The filterOptionAction value
    */
   public ActionListener getFilterOptionAction() {
      if(this.filterOptionAction == null) {
         this.filterOptionAction = new FilterOptionAction();
      }

      return this.filterOptionAction;
   }


   /**
    *  Gets the displayOptionAction attribute of the TideBrowse object
    *
    *@return    The displayOptionAction value
    */
   public ActionListener getDisplayOptionAction() {
      if(this.displayOptionAction == null) {
         this.displayOptionAction = new DisplayOptionAction();
      }

      return this.displayOptionAction;
   }


   /**
    *  Gets the uMLTreeExpansionHandler attribute of the TideBrowse object
    *
    *@return    The uMLTreeExpansionHandler value
    */
   private TreeExpansionListener getModelTreeExpansionHandler() {
      if(this.umlTreeExpansionHandler == null) {
         this.umlTreeExpansionHandler = new ModelTreeExpansionHandler();
      }

      return this.umlTreeExpansionHandler;
   }


   /**
    *  Gets the textAreaFocusHandler attribute of the TideBrowse object
    *
    *@return    The textAreaFocusHandler value
    */
   private FocusListener getTextAreaFocusHandler() {
      if(this.textAreaFocusHandler == null) {
         this.textAreaFocusHandler = new TextAreaFocusHandler();
      }

      return this.textAreaFocusHandler;
   }


   /**
    *  Description of the Class
    *
    *@author     beffy
    *@created    15. Dezember 2003
    */
   private class StatusBarOptionAction implements ActionListener {
      /**
       *  Description of the Method
       *
       *@param  e  Description of the Parameter
       */
      public void actionPerformed(ActionEvent e) {
         TideBrowse.this.setStatusVisible(
               TideBrowse.this.options.getShowStatusBar()
               );
      }
   }


   /**
    *  Description of the Class
    *
    *@author     beffy
    *@created    15. Dezember 2003
    */
   private class SortOptionAction implements ActionListener {
      /**
       *  Description of the Method
       *
       *@param  e  Description of the Parameter
       */
      public void actionPerformed(ActionEvent e) {
         if(TideBrowse.this.root.getChildCount() > 0) {
            TideBrowse.this.umlTree.sortToggled(TideBrowse.this.options);
         }
      }
   }


   /**
    *  Description of the Class
    *
    *@author     beffy
    *@created    27. November 2006
    */
   private class AutocompleteOptionAction implements ActionListener {
      /**
       *  Description of the Method
       *
       *@param  e  Description of the Parameter
       */
      public void actionPerformed(ActionEvent e) {
         if(TideBrowse.this.root.getChildCount() > 0) {
            TideBrowse.this.umlTree.autocompleteToggled(TideBrowse.this.options);
         }
      }
   }


   /**
    *  Description of the Class
    *
    *@author     beffy
    *@created    15. Dezember 2003
    */
   private class FilterOptionAction implements ActionListener {
      /**
       *  Description of the Method
       *
       *@param  e  Description of the Parameter
       */
      public void actionPerformed(ActionEvent e) {
         if(TideBrowse.this.root.getChildCount() > 0) {
            // there are nodes below the root, therefore need to reload
            TideBrowse.this.umlTree.display(
                  TideBrowse.this.treeModel,
                  TideBrowse.this.options,
                  TideBrowse.this.results
                  );
         }
      }
   }


   /**
    *  Description of the Class
    *
    *@author     beffy
    *@created    15. Dezember 2003
    */
   private class DisplayOptionAction implements ActionListener {
      /**
       *  Description of the Method
       *
       *@param  e  Description of the Parameter
       */
      public void actionPerformed(ActionEvent e) {
         if(TideBrowse.this.root.getChildCount(TideBrowse.this.filterOpt) > 0) {
            TideBrowse.this.umlTree.updateVisibleToggled(options);
         }
      }
   }


   /**
    *  Description of the Class
    *
    *@author     beffy
    *@created    15. Dezember 2003
    */
   private class ModelTreeExpansionHandler implements TreeExpansionListener {
      /**
       *  Description of the Method
       *
       *@param  evt  Description of the Parameter
       */
      public void treeCollapsed(TreeExpansionEvent evt) {
         Log.log(Log.DEBUG, this, "Tree Collapsed");
      }


      /**
       *  Description of the Method
       *
       *@param  evt  Description of the Parameter
       */
      public void treeExpanded(TreeExpansionEvent evt) {
         Log.log(Log.DEBUG, this, "Tree Expanded");

         Comparator compare;
         if(TideBrowse.this.options.getSort()) {
            compare = ComparatorFactory.createModelTreeDefaultComparator();
         }
         else {
            compare = ComparatorFactory.createModelTreeLineComparator();
         }

         TreePath path = evt.getPath();
         ModelTree.Node node = (ModelTree.Node) path.getLastPathComponent();
         ModelTreeSorter.sort(node, compare);

         DefaultTreeModel model = (DefaultTreeModel) TideBrowse.this.umlTree.getModel();
         model.nodeStructureChanged(node);
      }
   }


   /**
    *  This class handles communication between jEdit and TideBrowse tree for
    *  mouse events and tree slection events
    *
    *@author     beffy
    *@created    15. Dezember 2003
    */
   private class ModelTreeHandler implements MouseListener, TreeSelectionListener {
      /**
       *  Description of the Method
       *
       *@param  e  Description of the Parameter
       */
      public void mouseClicked(MouseEvent e) {
         int selRow = TideBrowse.this.umlTree.getRowForLocation(e.getX(), e.getY());

         // If user clicked outside of the text, returns
         if(selRow == -1) {
            return;
         }

         // Only required to supplement valueChanged(TreeSelectionEvent evt)
         // when mouse clicked on same node.
         if((selRow == TideBrowse.this.umlTree.getMaxSelectionRow())
                && (e.getClickCount() == 1)
               ) {
            // Check if view buffer matches tree buffer
            if(!this.verifyBuffer()) {
               return;
            }

            ModelTree.Node selectedNode = (ModelTree.Node)
                  TideBrowse.this.umlTree.getLastSelectedPathComponent();

            this.setPosition(selectedNode);
         }
      }


      /**
       *  Description of the Method
       *
       *@param  e  Description of the Parameter
       */
      public void mouseEntered(MouseEvent e) { }


      /**
       *  Description of the Method
       *
       *@param  e  Description of the Parameter
       */
      public void mouseExited(MouseEvent e) { }


      /**
       *  Description of the Method
       *
       *@param  e  Description of the Parameter
       */
      public void mousePressed(MouseEvent e) { }


      /**
       *  Description of the Method
       *
       *@param  e  Description of the Parameter
       */
      public void mouseReleased(MouseEvent e) { }


      /**
       *  Description of the Method
       *
       *@param  evt  Description of the Parameter
       */
      public void valueChanged(TreeSelectionEvent evt) {
         ModelTree.Node selectedNode;

         if(TideBrowse.this.umlTree.isSelectionEmpty()) {
            return;
         }

         // Check if view buffer matches tree buffer
         if(!this.verifyBuffer()) {
            return;
         }

         selectedNode = (ModelTree.Node)
               TideBrowse.this.umlTree.getSelectionPath().getLastPathComponent();

         this.setPosition(selectedNode);
      }


      /**
       *  Changes the position of the cursor in the buffer to match the tree
       *  node. The line containing the match is selected and appears near the
       *  top of the textarea
       *
       *@param  node  The new position value
       */
      private void setPosition(ModelTree.Node node) {
         Position pos = (Position) node.getPosition();
         if(pos == null) {
            return;
         }

         TideBrowseParser.LineSource lineSource = TideBrowse.this.parser.getSource();
         Buffer buffer = (Buffer) lineSource.getObject();

         int lineIndex = buffer.getLineOfOffset(pos.getOffset());

         TideBrowse.this.view.getTextArea().setSelection(new Selection.Range(
               buffer.getLineStartOffset(lineIndex),
               buffer.getLineEndOffset(lineIndex) - 1
               ));
         TideBrowse.this.view.getTextArea().moveCaretPosition(
               buffer.getLineStartOffset(lineIndex)
               );
         //search test
         //doSearch(TideBrowse.this.view, buffer);
      }


      /**
       *  Checks if the view buffer matches tree buffer
       *
       *@return    Description of the Return Value
       */
      private boolean verifyBuffer() {
         if(TideBrowse.this.parser instanceof TideBrowseNullParser) {
            return false;
         }

         TideBrowseParser.LineSource lineSource = TideBrowse.this.parser.getSource();

         Buffer buffer = TideBrowse.this.view.getBuffer();
         if(buffer == lineSource.getObject()) {
            return true;
         }

         String bufferWant = lineSource.getPath();
         String bufferHave = buffer.getPath();
         GUIUtilities.error(
               TideBrowse.this.view,
               "TideBrowse.msg.wrongBuffer",
               new Object[]{bufferWant, bufferHave}
               );
         return false;
      }
   }


   /**
    *  TideBrowse acts on the last focused TextArea
    *
    *@author     beffy
    *@created    15. Dezember 2003
    */
   private class TextAreaFocusHandler extends FocusAdapter {
      /**
       *  Description of the Method
       *
       *@param  evt  Description of the Parameter
       */
      public void focusGained(FocusEvent evt) {
         // walk up component hierarchy, looking for an EditPane
         Component comp = (Component) evt.getSource();
         while(!(comp instanceof EditPane)) {
            if(comp == null) {
               Log.log(Log.ERROR, this, "Could not find EditPane");
               return;
            }
            comp = comp.getParent();
         }

         EditPane editPane = (EditPane) comp;
         // No need to reparse if the buffer was already parsed
         if(editPane.getBuffer().getPath().equals(
               TideBrowse.this.parser.getSourcePath())
               ) {
            return;
         }
         if(editPane.getBuffer().isLoaded()) {
            TideBrowse.this.automaticReparse(
                  editPane.getView(), editPane.getBuffer()
                  );
         }
      }
   }


   /**
    *  Event handler to automatically reparse the current buffer when the docked
    *  TideBrowse is shown again
    *
    *@author     beffy
    *@created    15. Dezember 2003
    */
   private class ComponentShownHandler implements ComponentListener {
      /**
       *  Description of the Method
       *
       *@param  ce  Description of the Parameter
       */
      public void componentHidden(ComponentEvent ce) { }


      /**
       *  Description of the Method
       *
       *@param  ce  Description of the Parameter
       */
      public void componentMoved(ComponentEvent ce) { }


      /**
       *  Description of the Method
       *
       *@param  ce  Description of the Parameter
       */
      public void componentResized(ComponentEvent ce) { }


      /**
       *  Description of the Method
       *
       *@param  ce  Description of the Parameter
       */
      public void componentShown(ComponentEvent ce) {
         TideBrowse.this.automaticReparse();
      }
   }


   /**
    *  Description of the Class
    *
    *@author     beffy
    *@created    15. Dezember 2003
    */
   private class BufferParseAction implements EBComponent {
      private View view;
      private Buffer buffer;


      /**
       *  Constructor for the BufferParseAction object
       *
       *@param  view    Description of the Parameter
       *@param  buffer  Description of the Parameter
       */
      public BufferParseAction(View view, Buffer buffer) {
         this.view = view;
         this.buffer = buffer;
      }


      /**
       *  Description of the Method
       *
       *@param  message  Description of the Parameter
       */
      public void handleMessage(EBMessage message) {
         if(message instanceof BufferUpdate) {
            BufferUpdate bu = (BufferUpdate) message;
            if((bu.getWhat() == BufferUpdate.LOADED)
                   && (this.buffer == bu.getBuffer())
                  ) {
               TideBrowseParser.LineSource lineSource = new JEditLineSource(this.buffer);
               TideBrowse.this.parser.setSource(lineSource);
               SwingUtilities.invokeLater(TideBrowse.this.parseRunnable);
               EditBus.removeFromBus(this);
            }
         }
      }
   }
}

