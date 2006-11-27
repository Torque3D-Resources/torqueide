package com.garagegames.torque.tidebrowse.sidekick;
import java.util.List;

import javax.swing.*;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.textarea.Selection;
import org.gjt.sp.util.Log;
import sidekick.SideKickCompletion;

/**
 *@author     Stefan "beffy" Moises
 *@created    27. November 2006
 */
public class TideSideKickCompletion extends SideKickCompletion {
   private String lastWord;


   /**
    *  Constructor for the TideSideKickCompletion object
    *
    *@param  word      Description of the Parameter
    *@param  lastWord  Description of the Parameter
    */
   public TideSideKickCompletion(String word, String lastWord) {
      super(jEdit.getActiveView(), word);
      this.lastWord = lastWord;
   }


   /**
    *  Adds a feature to the Item attribute of the TideSideKickCompletion object
    *
    *@param  item  The feature to be added to the Item attribute
    *@param  word  The feature to be added to the Item attribute
    */
   public void addItem(Object item, String word) {
      boolean caseSensitive = false;
      //Log.log(Log.MESSAGE, TideSideKickCompletion.class, "TRY ADDING WORD: " + word + " ITEM: " + item.toString());
      //if (item.toString().regionMatches(caseSensitive, 0, word, 0, word.length())) {
      if(!items.contains(item)) {
         //Log.log(Log.MESSAGE, TideSideKickCompletion.class, "WORD ADDED: " + word);
         items.add(item);
      }
      //}
   }


   /**
    *  Gets the renderer attribute of the TideSideKickCompletion object
    *
    *@return    The renderer value
    */
   public ListCellRenderer getRenderer() {
      return new DefaultListCellRenderer();
   }


   /**
    *  Gets the itemsCount attribute of the TideSideKickCompletion object
    *
    *@return    The itemsCount value
    */
   public int getItemsCount() {
      return items.size();
   }


   /**
    *  Adds a feature to the OutlineableList attribute of the
    *  TideSideKickCompletion object
    *
    *@param  items  The feature to be added to the OutlineableList attribute
    *@param  word   The feature to be added to the OutlineableList attribute
    */
   public void addOutlineableList(List items, String word) {
      for(int i = 0; i < items.size(); i++) {
         addItem(items.get(i), word);
      }
   }


   /**
    *  Description of the Method
    *
    *@param  index  Description of the Parameter
    */
   public void insert(int index) {
      Object object = items.get(index);
      String insertText = "";
      int caret = textArea.getCaretPosition();
      if(text.length() != 0) {
         Selection selection = textArea.getSelectionAtOffset(caret);
         if(selection == null) {
            selection = new Selection.Range(caret - text.length(), caret);
         }
         else {
            int start = selection.getStart();
            int end = selection.getEnd();
            selection = new Selection.Range(start - text.length(), end);
         }
         textArea.setSelection(selection);
      }
      insertText = object.toString();
      /*
       *  if ("function".equals(lastWord)) {
       *  insertText += "()";
       *  caret--; //to go between the parenthesis
       *  }
       *  else
       *  {
       *  insertText += " ";
       *  caret--;
       *  }
       */
      caret += insertText.length();
      textArea.setSelectedText(insertText);
   }


   /**
    *  Gets the tokenLength attribute of the TideSideKickCompletion object
    *
    *@return    The tokenLength value
    */
   public int getTokenLength() {
      return text.length();
   }


   /**
    *  Description of the Method
    *
    *@param  selectedIndex  Description of the Parameter
    *@param  keyChar        Description of the Parameter
    *@return                Description of the Return Value
    */
   public boolean handleKeystroke(int selectedIndex, char keyChar) {
      if(keyChar == '\n' || keyChar == ' ' || keyChar == '\t') {
         insert(selectedIndex);
         if(keyChar == ' ') {
            //inserting the space after the insertion
            textArea.userInput(' ');
         }
         else if(keyChar == '\t') {
            //removing the end of the word
            textArea.deleteWord();
         }
         return false;
      }
      else {
         textArea.userInput(keyChar);
         return true;
      }
   }
}

