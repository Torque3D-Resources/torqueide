/*
 *  EvaluationToolTip.java - evaluate variables under cursor and show tooltip
 *  Copyright (c) 2002 Paul Dana
 *
 *  :tabSize=4:indentSize=4:noTabs=false:maxLineLen=0:
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

package com.garagegames.torque.tidedebug;

import java.awt.*;
import java.awt.event.MouseEvent;

import org.gjt.sp.jedit.*;
import org.gjt.sp.util.Log;
import org.gjt.sp.jedit.textarea.*;

import com.garagegames.torque.tide.*;
import com.garagegames.torque.*;

/**
 *  Evaluates variables under cursor and shows tool tip text
 *
 *@author     beffy
 *@created    26. August 2004
 */
public class EvaluationToolTip extends TextAreaExtension {

   private Tide tide;
   private JEditTextArea textArea;

   /**
    *  Description of the Field
    */
   protected static int lastNum = 0;


   /**
    *  Constructor for the EvaluationToolTip object
    *
    *@param  tide      Description of the Parameter
    *@param  textArea  Description of the Parameter
    */
   public EvaluationToolTip(Tide tide, JEditTextArea textArea) {
      this.tide = tide;
      this.textArea = textArea;
   }


   // is the given char a legal variable char?
   /**
    *  Gets the legal attribute of the EvaluationToolTip object
    *
    *@param  c  Description of the Parameter
    *@return    The legal value
    */
   public boolean isLegal(char c) {
      if (c >= 'A' && c <= 'Z') {
         return true;
      }
      if (c >= 'a' && c <= 'z') {
         return true;
      }
      if (c >= '0' && c <= '9') {
         return true;
      }
      if (c == '_' || c == '%' || c == '$') {
         return true;
      }
      if (c == ':') {
         return true;
      }

      return false;
   }


   // get symbol at given char offset
   /**
    *  Gets the symbol attribute of the EvaluationToolTip object
    *
    *@param  offset  Description of the Parameter
    *@return         The symbol value
    */
   public String getSymbol(int offset) {
      // first get the whole line this offset is on
      int lineOffset = textArea.getLineOfOffset(offset);

      // get the offset of first char in line
      int lineStartOffset = textArea.getLineStartOffset(lineOffset);

      // this is the offset into this line of char we came down on
      int charOffset = offset - lineStartOffset;

      // get the line itself
      String line = textArea.getLineText(lineOffset);

      // if offset is past end of line then do nothing
      if (charOffset < 0 || charOffset >= line.length()) {
         return null;
      }

      // if we are not in middle of legal symbol string then nothing
      if (!isLegal(line.charAt(charOffset))) {
         return null;
      }

      // search string backward and forward from this location
      // until we hit an illegal or separator character
      int startOffset = charOffset;
      while (startOffset >= 0 && isLegal(line.charAt(startOffset))) {
         startOffset--;
      }

      int endOffset = charOffset + 1;
      while (endOffset < line.length() && isLegal(line.charAt(endOffset))) {
         endOffset++;
      }

      if (startOffset < 0) {
         startOffset = 0;
      }

      if (endOffset > line.length()) {
         endOffset = line.length() - 1;
      }

      if (!isLegal(line.charAt(startOffset))) {
         startOffset++;
      }

      if (startOffset > endOffset) {
         return null;
      }

      // only evaluate local and global vars
      if (line.charAt(startOffset) != '$' && line.charAt(startOffset) != '%') {
         return null;
      }

      // extract the symbol from the line and return it
      String symbol = line.substring(startOffset, endOffset);
      return symbol;
   }


   // called when mouse pauses over text area
   /**
    *  Gets the toolTipText attribute of the EvaluationToolTip object
    *
    *@param  x  Description of the Parameter
    *@param  y  Description of the Parameter
    *@return    The toolTipText value
    */
   public String getToolTipText(int x, int y) {
      // if we are not connected or are not paused then do nothing...
      if (tide.getState() != Tide.PAUSED) {
         return null;
      }

      // get the character offset of the given x,y location
      // if not in the buffer then quit out
      int offset = textArea.xyToOffset(x, y);
      if (offset < 0) {
         return null;
      }

      // get the symbol at this location
      String symbol = getSymbol(offset);
      if (symbol == null) {
         return null;
      }

      // evaluate this symbol and get result immediately
      String value = tide.evaluateNow(symbol);
      return symbol + "=" + value;
   }
}

