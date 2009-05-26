/*
 * ModelTree.java
 * Copyright (c) 1999 George Latkiewicz	(georgel@arvotek.net)
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


import java.util.Comparator;
import java.util.Enumeration;
import java.util.Vector;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.gjt.sp.util.Log;

import com.garagegames.torque.tidebrowse.options.DisplayOptions;
import com.garagegames.torque.tidebrowse.options.FilterOptions;
import com.garagegames.torque.tidebrowse.options.GeneralOptions;
import com.garagegames.torque.tidebrowse.model.ModelElement;
import com.garagegames.torque.tidebrowse.model.ModelType;


public class ModelTree extends JTree
{
    /**
     * Constructor for a ModelTree, automatically sets the new ModelTree's
     * associated UMLModel to null.
     */
    public ModelTree() {
        super.setModel(null);

        putClientProperty("JTree.lineStyle", "Angled");
        setVisibleRowCount(15);

    }


    /**
     * This is the method that is called whenever the results of a new parse
     * need to be displayed, or when filter options have changed on the
     * currently parsed and displayed ModelTree.
     */
    public void display(Model tm, GeneralOptions options, TideBrowseParser.Results results) {
        DisplayOptions displayOpt = options.getDisplayOptions();
        FilterOptions  filterOpt  = options.getFilterOptions();

        setCellRenderer(new CellRenderer( displayOpt ) );

        tm.setFilterOptions(filterOpt);
        super.setModel(tm);
        tm.reload();

        expandRow(0);

        TreePath tp = results.getTopLevelPath();
        if (tp != null) {
            expandPath(tp);
        }
    }


    public void updateVisibleToggled(GeneralOptions options) {
        Model tm = (Model) getModel();
        FilterOptions filterOpt = options.getFilterOptions();
        TreePath aPath;
        Node aNode;
        Object[] cChildrenObject;

        aPath = getPathForRow(0);
        Enumeration e = getDescendantToggledPaths(aPath);

        while ( e.hasMoreElements() ) {
            aPath = (TreePath) e.nextElement();
            aNode = (Node) aPath.getLastPathComponent();

            cChildrenObject = aNode.getVisibleChildrenObject(filterOpt);

            if (cChildrenObject != null) {
                tm.nodesChanged(aNode, (int[]) cChildrenObject[0]);
            }
        }
    }

    public void autocompleteToggled(GeneralOptions options) {
       // nothing to do
        Log.log(Log.DEBUG, this, "** autocompleteToggled");
    }

    public void sortToggled(GeneralOptions options) {
        Model tm = (Model) getModel();
        TreePath aPath = getPathForRow(0);
        Enumeration e  = getDescendantToggledPaths(aPath);

        Comparator compare;
        if (options.getSort()) {
            compare = ComparatorFactory.createModelTreeDefaultComparator();
        } else {
            compare = ComparatorFactory.createModelTreeLineComparator();
        }

        collapsePath(aPath);

        Log.log(Log.DEBUG, this, "** sortToggled called for root path: " + aPath);

        while (e.hasMoreElements()) {
            Log.log(Log.DEBUG, this, "++ sortToggled called for root path: " + aPath);
            aPath = (TreePath) e.nextElement();

            ModelTree.Node node = (ModelTree.Node) aPath.getLastPathComponent();
            ModelTreeSorter.sort(node, compare);

            expandPath(aPath);
        }
    }


    public static class Model extends DefaultTreeModel
    {
        private FilterOptions filterOpt = null;

        // Overrides:
        //    getChild() & getChildCount()


        public Model(TreeNode root) {
            super(root);
        }


        public void setFilterOptions(FilterOptions filterOpt) {
            this.filterOpt = filterOpt;
        }


        public FilterOptions getFilterOptions() { return filterOpt; }


        // Overridden methods to provide intended behaviour

        public Object getChild(Object parent, int index) {
            if (filterOpt != null) {
                    return ((Node) parent).getChildAt(index, filterOpt);
            }
            return ((Node) parent).getChildAt(index);
        }


        public int getChildCount(Object parent) {
            if (filterOpt != null) {
                    return ( (Node) parent).getChildCount(filterOpt);
            }
            return ((Node) parent).getChildCount();
        }


        public boolean isLeaf(Object node) {
            if (getChildCount(node) > 0) {
                return false;
            } else {
                return true;
            }
        }
    }


    public static class Node extends DefaultMutableTreeNode
    {
        private Object pos = null;


        public Node(ModelElement userObject) {
            super(userObject);
        }


        public Node(String userObject) {
            super(userObject);
        }


        public final void setPosition(Object pos) {
            this.pos = pos;
        }


        public final Object getPosition() {
            return pos;
        }


        /**
         * Returns the TreePath from the specified ancestor Node to this
         * node.
         */
        public TreePath getPathFrom(Node ancestor) {
            Enumeration e = pathFromAncestorEnumeration(ancestor);
            Vector pathList = new Vector();
            int depth = 0;
            Node curNode;

            while (e.hasMoreElements()) {
                depth++;
                curNode = (Node) e.nextElement();
                pathList.addElement(curNode); //  patch for jdk1.1
                // pathList.add(curNode); // jdk1.2 only ???
            }

            // for JDK 1.1
            Node[] pathArray = new Node[pathList.size()];
            pathList.copyInto(pathArray);
            return new TreePath(pathArray);

            // for JDI 1.2
            //return new TreePath( pathList.toArray() );
        } // getPathFrom(Node): TreePath


        public final boolean isVisible(FilterOptions filterOpt) {
            if (userObject instanceof ModelElement) {
                return ( (ModelElement) getUserObject() ).isVisible(filterOpt);
            } else {
                return true;
            }
        }


        public final ModelElement getElement() {
            if (userObject instanceof ModelElement) {
                return (ModelElement) userObject;
            } else {
                return null;
            }
        }


        public static final ModelElement getElement(Object o) {
            if (!(o instanceof ModelTree.Node)) {
                return null;
            }

            ModelTree.Node n = (ModelTree.Node)o;
            return n.getElement();
        }


        public ModelType getElementType() {
            if (userObject instanceof ModelElement) {
                return ((ModelElement) userObject).getElementType();
            } else {
                return null;
            }
        }


        public void setName(String name) {
            if (userObject instanceof ModelElement) {
                ((ModelElement) userObject).setName(name);
            } else {
                userObject = name;
            }
        }


        /**
         * Returns visible index of the current node, or -1 if it is not
         * visible based on the specified filter options.
         */
        public final int getVisibleIndex(FilterOptions filterOpt) {
            Node parent = (Node) this.getParent();
            if (parent == null) { return -1; }

            Vector children = parent.children;
            int visibleIndex = -1;

            for(int i = 0; i < children.size(); i++) {
                Node curNode = (Node) children.elementAt(i);
                Object nodeObject = curNode.userObject;
                if (((ModelElement) nodeObject).isVisible(filterOpt)) {
                    visibleIndex++;
                    if (curNode == this) { return visibleIndex; }
                }

                if (curNode == this) { return -1; }
            }

            throw new ArrayIndexOutOfBoundsException("index unmatched");
        }


        /**
         * Creates and returns a forward-order enumeration of this node's
         * visible children.
         *
         * @return	an Enumeration of this node's visible children
         */
        public Object[] getVisibleChildrenObject(FilterOptions filterOpt) {
            int count = getChildCount(filterOpt);
            if (count > 0) {
                Object[] visibleChildNodes = new Object[count];
                int[]    visibleChildIndxs = new int[count];
                int visibleIndex = -1;

                for(int i = 0; i < children.size(); i++) {
                    Node curNode = (Node) children.elementAt(i);
                    Object nodeObject = curNode.userObject;
                    if (((ModelElement) nodeObject).isVisible(filterOpt)) {
                        visibleIndex++;
                        visibleChildIndxs[visibleIndex] = visibleIndex;
                        visibleChildNodes[visibleIndex] = curNode;
                    }
                }
                return new Object[] { visibleChildIndxs, visibleChildNodes };
            } else {
                return null;
            }

        }


        /**
         * Returns the child of this node with the specified visible index
         * based on the specified filter options.
         */
        public final Node getChildAt(int index, FilterOptions filterOpt) {
            if (children == null) {
                throw new ArrayIndexOutOfBoundsException("node has no children");
            }

            int realIndex    = -1;
            int visibleIndex = -1;
            Enumeration myenum = children.elements();
            while (myenum.hasMoreElements()) {
                Object nodeObject = ((Node) myenum.nextElement()).userObject;

                if (((ModelElement) nodeObject).isVisible(filterOpt)) {
                    visibleIndex++;
                }

                realIndex++;
                if (visibleIndex == index) {
                    return (Node)children.elementAt(realIndex);
                }
            }

            throw new ArrayIndexOutOfBoundsException("index unmatched");
        }


        /**
         * Returns a count of the number of visible children of this node
         * based on the specified filter options.
         */
        public final int getChildCount(FilterOptions filterOpt) {
            if (children == null) {
                return 0;
            }

            int count = 0;
            Enumeration myenum = children.elements();
            while (myenum.hasMoreElements()) {
                Object nodeObject = ((Node) myenum.nextElement()).userObject;

                if (((ModelElement) nodeObject).isVisible(filterOpt)) {
                    count++;
                }
            }
            return count;
        }

    }


    public static class CellRenderer extends DefaultTreeCellRenderer
    {
        private static Font standardFont = new Font("Helvetica", Font.PLAIN, 12);
        private static Font italicFont   = new Font("Helvetica", Font.ITALIC, 12);

        private boolean isUnderlined;
        private DisplayOptions options;
        private DisplayOptions inverseOptions;


        public CellRenderer(DisplayOptions options) {
            super();
            this.options = options;
        }


        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean sel,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus)
        {
            Component r = super.getTreeCellRendererComponent(tree, value, sel,
                            expanded, leaf,
                            row, hasFocus);

            isUnderlined = false;

            if (r instanceof JLabel) {
                JLabel lab = (JLabel) r;

                lab.setToolTipText(null);
                tree.setToolTipText(null);

                ModelTree.Node node = (ModelTree.Node) value;

                Object uObj = node.getUserObject();

                if (uObj != null && uObj instanceof ModelElement) {

                    ModelElement e = (ModelElement) uObj;
                    ModelType type = e.getElementType();

                    if (type != null) {

                        // label
                        lab.setText(e.toString(options));

                        // tips
                        inverseOptions = options.getInverseOptions();

                        lab.setToolTipText(e.toString(inverseOptions) + " ");
                        tree.setToolTipText(e.toString(inverseOptions) + " ");

                        // icon
                        Icon icon = (Icon) type.getIcon();
                        if (icon != null) {
                            lab.setIcon(icon) ;
                        }
                    }

                } else {

                    // for strings (e.g. root)
                    lab.setFont(standardFont);
                }
            }

            return r;
        }


        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if ( this.isUnderlined) {
                int x = getIcon().getIconWidth() + Math.max(0, getIconTextGap() - 1);
                g.setColor(Color.black);
                g.drawLine(x, getHeight() - 2, getWidth() - 2, getHeight() - 2);
            }
        }
    }
}

