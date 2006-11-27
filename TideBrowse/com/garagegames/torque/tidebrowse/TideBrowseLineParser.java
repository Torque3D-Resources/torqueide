/*
 *  TideBrowseLineParser.java - a TideBrowseParser for Torque Script Code via LineSource
 *
 *  Copyright (c) 1999 George Latkiewicz, 2000-2001 Andre Kaplan, 2002-2003 Stefan "Beffy" Moises
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

import com.garagegames.torque.tidebrowse.model.ModelAttribute;
import com.garagegames.torque.tidebrowse.model.ModelElement;
import com.garagegames.torque.tidebrowse.model.ModelFolderMember;
import com.garagegames.torque.tidebrowse.model.ModelMessage;
import com.garagegames.torque.tidebrowse.model.ModelType;

import java.util.Stack;

import org.gjt.sp.util.Log;

/**
 *  The class that implemnts a TideBrowseParser for Torque Script Code via a
 *  TideBrowseParser.LineSource.
 *
 *@author     George Latkiewicz, "hacked" by Stefan "Beffy" Moises to parse
 *      Torque related files...
 *@created    16. Dezember 2003
 *@version    $Id: TideBrowseLineParser.java,v 1.6 2003/12/15 20:58:20 cvsuser
 *      Exp $
 */
public class TideBrowseLineParser implements TideBrowseParser {
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
   private TideBrowseParser.LineSource source;

   private String fileName;
   private String expectedTopLevelName;

   Stack nodeStack = new Stack();
   private ModelTree.Node root;

   // Counters
   int tokenCount = 0;
   // only count tokens returned by TokenSource
   int curTokenLine = -1;
   int curTokenPos = -1;


   /**
    *  This method creates a new instance of TideBrowse GUI and Parsing engine.
    */
   public TideBrowseLineParser() {
      this.results = new TideBrowseParser.Results();
   }


   /**
    *  Gets the source attribute of the TideBrowseLineParser object
    *
    *@return    The source value
    */
   public TideBrowseParser.LineSource getSource() {
      return this.source;
   }


   /**
    *  Sets the source attribute of the TideBrowseLineParser object
    *
    *@param  source  The new source value
    */
   public void setSource(TideBrowseParser.LineSource source) {
      this.source = source;
   }


   /**
    *  Returns the String that represents the name associated with the current
    *  TideBrowseParser.LineSource (e.g. the fileName associated with the
    *  current buffer), or null if the TideBrowseParser.LineSource is not
    *  currently associated with a source.
    *
    *@return    The sourceName value
    */
   public String getSourceName() {
      if(this.source != null) {
         return this.source.getName();
      }
      else {
         return null;
      }
   }


   /**
    *  Gets the sourcePath attribute of the TideBrowseLineParser object
    *
    *@return    The sourcePath value
    */
   public String getSourcePath() {
      if(this.source != null) {
         return this.source.getPath();
      }
      else {
         return null;
      }
   }


   /**
    *  Sets the rootNode attribute of the TideBrowseLineParser object
    *
    *@param  root  The new rootNode value
    */
   public final void setRootNode(ModelTree.Node root) {
      this.root = root;
   }


   /**
    *  Gets the ready attribute of the TideBrowseLineParser object
    *
    *@return    The ready value
    */
   public boolean isReady() {
      boolean rVal = false;

      if((this.source != null)
             && (this.root != null)
            ) {
         rVal = true;
      }

      return rVal;
   }


   /**
    *  The method that performs the actual parsing. This is the method which
    *  builds the tree model that reflects the heirarchical structure of the
    *  Java code in the current LineSource. This method is now heavily changed /
    *  stripped down to handle Torque script files instead of Java source
    *  code... Stefan "Beffy" Moises, 07/19/2002
    *
    *@return    Description of the Return Value
    */
   public TideBrowseParser.Results parse() {
      results.reset();
      // reset result counters to 0

      TideBrowseParser.LineSource ls = this.source;
      ls.reset();
      // reset the LineSource to the beginning of the buffer

      if(!isReady()) {
         return results;
      }

      // Set initial counts
      tokenCount = 0;
      // only count tokens returned by TokenSource
      curTokenLine = -1;
      curTokenPos = -1;

      ModelElement currentElement = null;
      ModelMessage messageElement = null;
      ModelType eType;

      // additional "root" elements which are used as folders
      // for grouping the different elements
      ModelElement datablockRoot = null;
      ModelElement functionsRoot = null;
      ModelElement newobjectRoot = null;

      nodeStack.push(root);
      ModelTree.Node parentNode = root;

      // additional nodes
      ModelTree.Node datablockParentNode = root;
      ModelTree.Node functionsParentNode = root;
      ModelTree.Node newobjectParentNode = root;

      ModelTree.Node newobjectParentNodeSave = null;
      boolean inObjectBlock = false;
      int openBrackets = 0;
      int closedBrackets = 0;

      int methodBraceCount = 0;
      int lastTokenLine = -1;

      int packageMemberStartPos = -1;
      int packageMemberLine = -1;

      // Parsing Attributes
      int curElementStartPos = -1;

      String msgStr = null;
      boolean badFlag = false;
      boolean resetFlag = false;

      String lastToken = null;
      String token = null;
      char tokenStartChar;

      boolean exceptionThrown = false;

      // Get fileName and TokenSource
      fileName = ls.getName();
      TokenSource ts = new TokenSource(ls);

      // only parse TGE related files
      if(!(fileName.toUpperCase().endsWith(".CS") || fileName.toUpperCase().endsWith(".GUI") || fileName.toUpperCase().endsWith(".MIS"))) {
         expectedTopLevelName = fileName;
         fileName += " (NON-Torque file?)";
      }
      else {
         expectedTopLevelName = fileName.substring(0, fileName.indexOf('.'));
      }
      root.setName(fileName);

      try {
         root.setPosition(ls.createPosition(0));
         if(root.getChildCount() > 0) {
            root.removeAllChildren();
         }

         // add sub-root elements as "group-folders":
         // functions
         functionsRoot = new ModelFolderMember("Functions", ModelType.FUNCTION, 0,
               0);
         nodeStack.push(functionsParentNode = insertAsNode(
               functionsRoot, 0, root));

         // datablocks
         datablockRoot = new ModelFolderMember("Datablocks", ModelType.DATABLOCK, 0,
               0);
         nodeStack.push(datablockParentNode = insertAsNode(
               datablockRoot, 0, root));

         // objects
         newobjectRoot = new ModelFolderMember("Objects", ModelType.OBJECT, 0,
               0);
         nodeStack.push(newobjectParentNode = insertAsNode(
               newobjectRoot, 0, root));

         // parsing attributes
         int mod = 0;
         String className = null;

         String memberType = null;
         // method return type or attribute type
         String memberName = null;

         String parmType = "";
         String parmName = "";
         String currType = "";

         do {
            tokenCount++;
            lastTokenLine = curTokenLine;
            token = ts.getNextToken();
            curTokenLine = ts.getCurrentLineNum();
            curTokenPos = ts.getCurrentPos();
            if(token == null) {
               break;
            }
            // may be necessary if we end in a comment
            //Log.log(Log.DEBUG, this, "curTokenLine: " + curTokenLine + " - curTokenPos: " + curTokenPos + " - Token: " + token);

            tokenStartChar = token.charAt(0);

            // add functions
            if(token.equals("function")) {
               currType = "function";
               curElementStartPos = ls.getStartOffset() + curTokenPos;
               mod = 0;
               String nextTokenName = ts.getNextToken();
               String nextTokenName2 = ts.getCurrentLine();

               nextTokenName2 = nextTokenName2.substring(nextTokenName2.indexOf("function ") + 9, nextTokenName2.indexOf("("));

               mod = MutableModifier.setFunction(mod);
               // use the "FUNCTION" type (used for choosing the icon, etc.)
               eType = ModelType.FUNCTION;
               className = nextTokenName2;
               results.incFunctionCount();
               // generate new element and insert it into the tree
               currentElement = new ModelFolderMember(nextTokenName2, eType, mod,
                     curTokenLine);
               nodeStack.push(parentNode = insertAsNode(
                     currentElement, curElementStartPos, functionsParentNode));
            }
            // add datablocks
            // fix: we also could have something like "datablock = %data;", so we have to look if its a real datablock with "()"!
            else if(token.equals("datablock") && ts.getCurrentLine().indexOf(")") != -1) {
               curElementStartPos = ls.getStartOffset() + curTokenPos;
               currType = "datablock";
               mod = 0;
               String nextTokenName = ts.getNextToken();
               String nextTokenName2 = ts.getCurrentLine();
               nextTokenName2 = nextTokenName2.substring(nextTokenName2.indexOf("datablock ") + 10, nextTokenName2.indexOf(")") + 1);

               mod = MutableModifier.setDatablock(mod);
               // use the "DATABLOCK" type (used for choosing the icon, etc.)
               eType = ModelType.DATABLOCK;
               className = nextTokenName2;
               results.incInterfaceCount();
               // generate new element and insert it into the tree
               currentElement = new ModelFolderMember(nextTokenName2, eType, mod,
                     curTokenLine);
               nodeStack.push(parentNode = insertAsNode(
                     currentElement, curElementStartPos, datablockParentNode));
            }
            // add objects in a flat structure
            else if(token.equals("new")) {
               if(ts.getCurrentLine().indexOf(";") != -1 || ts.getCurrentLine().indexOf("=") != -1) {
                  continue;
               }
               // we don't want stuff like "$spline = new Spline();" or "%obj = new WheeledVehicle()"
               currType = "object";
               curElementStartPos = ls.getStartOffset() + curTokenPos;
               mod = 0;
               String nextTokenName = ts.getNextToken();
               String nextTokenName2 = ts.getCurrentLine();
               nextTokenName2 = nextTokenName2.substring(nextTokenName2.indexOf("new ") + 4, nextTokenName2.indexOf(")") + 1);

               mod = MutableModifier.setTGEObject(mod);
               // use the "OBJECT" type (used for choosing the icon, etc.)
               eType = ModelType.OBJECT;
               className = nextTokenName2;
               results.incObjAttrCount();
               // generate new "object" element and insert it into the tree
               currentElement = new ModelFolderMember(nextTokenName2, eType, mod,
                     curTokenLine);

               nodeStack.push(parentNode = insertAsNode(
                     currentElement, curElementStartPos, newobjectParentNode));
            }

            /*
             *  / this is a test to get the "objects" into a tree structure, but it doesnt really work for complex
             *  / trees... so for now the above code just adds the "objects" in a flat structure...
             *  / START TEST
             *  else if (token.equals("new") ) {
             *  if(ts.getCurrentLine().indexOf(";") != -1 || ts.getCurrentLine().indexOf("=") != -1)
             *  continue; // we don't want stuff like "$spline = new Spline();" or "%obj = new WheeledVehicle()"
             *  currType = "object";
             *  curElementStartPos = ls.getStartOffset() + curTokenPos;
             *  mod = 0;
             *  String nextTokenName = ts.getNextToken();
             *  String nextTokenName2 = ts.getCurrentLine();
             *  nextTokenName2 = nextTokenName2.substring(nextTokenName2.indexOf("new ")+4, nextTokenName2.indexOf(")")+1);
             *  / there is no Attribute modifier and .setAttribute(), so we use
             *  / "final" - only needed for displaying the keywords in the GUI
             *  / which is selectable in the "Options" dialog...
             *  mod = MutableModifier.setTGEObject(mod);
             *  / use the UML "OBJECT" type (used for choosing the icon, etc.)
             *  eType = ModelType.OBJECT;
             *  className = nextTokenName2;
             *  results.incObjAttrCount();
             *  / generate new UML element and insert it into the tree
             *  currentElement = new ModelFolderMember(nextTokenName2, eType, mod,
             *  curTokenLine);
             *  /Log.log(Log.ERROR, this, "openBrackets: " + openBrackets + " - closedBrackets: " + closedBrackets + " - Token: " + token);
             *  if(openBrackets > closedBrackets && inObjectBlock) {
             *  / another nested object!
             *  openBrackets--;
             *  inObjectBlock = true;
             *  newobjectParentNode = parentNode;
             *  nodeStack.push( parentNode = insertAsNode(
             *  currentElement, curElementStartPos, parentNode) );
             *  }
             *  else{
             *  inObjectBlock = true;
             *  nodeStack.push( parentNode = insertAsNode(
             *  currentElement, curElementStartPos, newobjectParentNode) );
             *  }
             *  }
             *  else if (token.equals("{") && currType.equals("object")) {
             *  openBrackets++;
             *  /inObjectBlock = true;
             *  }
             *  else if (token.equals("}") && currType.equals("object")) {
             *  closedBrackets++;
             *  if(inObjectBlock) {
             *  /inObjectBlock = false;
             *  }
             *  }
             *  / END TEST
             */
         }while (token != null);
      }
      catch(TokenSource.Exception e) {

         exceptionThrown = true;

         curTokenLine = ts.getCurrentLineNum();
         curTokenPos = ts.getCurrentPos();
         curElementStartPos = ls.getStartOffset() + curTokenPos;

         // Unterminated multi-line comment, String or char expression
         msgStr = e.getMessage();
         // Create error node and increment count
         results.incErrorCount();

         try {
            parentNode = (ModelTree.Node) nodeStack.peek();
            currentElement = parentNode.getElement();
         }
         catch(Exception ex) {
            parentNode = null;
            currentElement = null;
         }
         messageElement = new ModelMessage(msgStr, ModelType.ERROR,
               currentElement, curTokenLine);
         insertAsNode(messageElement, curElementStartPos, parentNode);

      }
      catch(java.util.EmptyStackException e) {

         exceptionThrown = true;
      }
      // end try-catch

      // Report Final Errors

      curTokenLine = ts.getCurrentLineNum();
      curTokenPos = ts.getCurrentPos();
      curElementStartPos = ls.getStartOffset() + curTokenPos;

      if(results.getFunctionCount() == 0
             && results.getInterfaceCount() == 0 && results.getObjAttrCount() == 0 && results.getMethodCount() == 0) {

         msgStr = "No functions, datablocks or objects found!";

         // Show only this error
         root.removeAllChildren();

         // Create error node and increment count
         results.setErrorCount(1);

         messageElement = new ModelMessage(msgStr, ModelType.ERROR,
               null, curTokenLine);
         insertAsNode(messageElement, curElementStartPos, root);

      }
      // if no package members found

      return results;
   }


   /**
    *  Description of the Method
    *
    *@param  e           Description of the Parameter
    *@param  pos         Description of the Parameter
    *@param  parentNode  Description of the Parameter
    *@return             Description of the Return Value
    */
   private final ModelTree.Node insertAsNode(ModelElement e, int pos,
         ModelTree.Node parentNode) {
      // Insert Node
      ModelTree.Node node = new ModelTree.Node(e);
      node.setPosition(this.source.createPosition(pos));

      // line below is faster than: treeModel.insertNodeInto(node, parentNode, index);
      parentNode.insert(node, parentNode.getChildCount());

      return node;
   }


   /**
    *  Description of the Method
    *
    *@param  level     Description of the Parameter
    *@param  msgStr    Description of the Parameter
    *@param  lineText  Description of the Parameter
    */
   private final void detailLog(int level, String msgStr, String lineText) {
      Log.log(level, this, "(" + tokenCount + ") "
             + curTokenLine + "-" + curTokenPos + ":\n\t"
             + msgStr + "\n\t" + lineText);
   }

}


