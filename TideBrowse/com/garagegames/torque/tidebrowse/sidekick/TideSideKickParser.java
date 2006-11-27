package com.garagegames.torque.tidebrowse.sidekick;

import com.garagegames.torque.tidebrowse.*;
import com.garagegames.torque.tidebrowse.options.GeneralOptions;

import errorlist.DefaultErrorSource;
import errorlist.ErrorSource;

import java.io.*;

import java.util.*;

import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.syntax.*;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.util.Log;

import projectviewer.*;

import projectviewer.event.*;

import projectviewer.vpt.*;

import sidekick.*;

/**
 *@author     beffy
 *@created    27. November 2006
 */
public final class TideSideKickParser
       extends SideKickParser {

   private final static String completionFileName = "autocomplete.txt";
   private final static ArrayList completionsList = new ArrayList();
   private TideBrowseOptionPane optionPane;
   private GeneralOptions options;

   /**
    *  the php parser.
    */
   private TideBrowseLineParser parser;

   /**
    *  the error source.
    */
   private final DefaultErrorSource phpErrorSource = null;

   /**
    *  the project manager.
    */
   private final ProjectManager projectManager;


   /**
    *  Instantiate the TideSideKickParser.
    */
   public TideSideKickParser() {
      super("cs");

      // access the option pane instance (singleton) to get
      // the selected options so that we can see if autocomplete is enabled...
      optionPane = TideBrowseOptionPane.getInstance();

      if(optionPane != null) {
         options = optionPane.getOptions();
         options.load(new JEditPropertyAccessor());
      }

      // get the ProjectManager instance
      projectManager = ProjectManager.getInstance();

      // build autocomplete array
      buildCompletions();

      // register event listeners for current project

      /*
       *  VPTProject project = PVActions.getCurrentProject(jEdit.getActiveView());
       *  project.addProjectListener(new ProjectListener() {
       *  public void fileAdded(ProjectEvent evt){
       *  Log.log(Log.DEBUG, TideSideKickParser.class, "+++++++ FILE ADDED, RELOADING AUTOCOMPLETIONS!");
       *  createProjectAutocomplete();
       *  }
       *  public void filesAdded(ProjectEvent evt){
       *  Log.log(Log.DEBUG, TideSideKickParser.class, "+++++++ FILES ADDED, RELOADING AUTOCOMPLETIONS!");
       *  createProjectAutocomplete();
       *  }
       *  public void fileRemoved(ProjectEvent evt){
       *  Log.log(Log.DEBUG, TideSideKickParser.class, "+++++++ FILE REMOVED, RELOADING AUTOCOMPLETIONS!");
       *  createProjectAutocomplete();
       *  }
       *  public void filesRemoved(ProjectEvent evt){
       *  Log.log(Log.DEBUG, TideSideKickParser.class, "+++++++ FILES REMOVED, RELOADING AUTOCOMPLETIONS!");
       *  createProjectAutocomplete();
       *  }
       *  public void propertiesChanged(ProjectEvent evt){
       *  Log.log(Log.DEBUG, TideSideKickParser.class, "+++++++ PROPERTIES CHANGED, RELOADING AUTOCOMPLETIONS!");
       *  createProjectAutocomplete();
       *  }
       *  });
       */
      ProjectViewer.addProjectViewerListener(
         new ProjectViewerListener() {
            public void projectLoaded(ProjectViewerEvent evt) {
               Log.log(Log.DEBUG, TideSideKickParser.class,
                     "+++++++ PROJECT CHANGED, RELOADING AUTOCOMPLETIONS!");

               // reload completions
               buildCompletions();

               // register event listeners for current project
               // TODO: REMOVE THEM WITH EVERY CHANGE!!!

               /*
                *  VPTProject project = PVActions.getCurrentProject(jEdit.getActiveView());
                *  project.addProjectListener(new ProjectListener() {
                *  public void fileAdded(ProjectEvent evt){
                *  Log.log(Log.DEBUG, TideSideKickParser.class, "+++++++ FILE ADDED, RELOADING AUTOCOMPLETIONS!");
                *  createProjectAutocomplete();
                *  }
                *  public void filesAdded(ProjectEvent evt){
                *  Log.log(Log.DEBUG, TideSideKickParser.class, "+++++++ FILES ADDED, RELOADING AUTOCOMPLETIONS!");
                *  createProjectAutocomplete();
                *  }
                *  public void fileRemoved(ProjectEvent evt){
                *  Log.log(Log.DEBUG, TideSideKickParser.class, "+++++++ FILE REMOVED, RELOADING AUTOCOMPLETIONS!");
                *  createProjectAutocomplete();
                *  }
                *  public void filesRemoved(ProjectEvent evt){
                *  Log.log(Log.DEBUG, TideSideKickParser.class, "+++++++ FILES REMOVED, RELOADING AUTOCOMPLETIONS!");
                *  createProjectAutocomplete();
                *  }
                *  public void propertiesChanged(ProjectEvent evt){
                *  Log.log(Log.DEBUG, TideSideKickParser.class, "+++++++ PROPERTIES CHANGED, RELOADING AUTOCOMPLETIONS!");
                *  createProjectAutocomplete();
                *  }
                *  });
                */
            }


            public void projectRemoved(ProjectViewerEvent evt) {
            }


            public void projectAdded(ProjectViewerEvent evt) {
            }
         }, jEdit.getActiveView());
   }


   /**
    *  Static function to re-generate the project autocompletion
    */
   public static void createProjectAutocomplete() {
      removeProjectCompletions();
      buildCompletions();
   }


   /**
    *  Static function to re-generate file-based autocompletions
    */
   private static void buildCompletions() {

      // get completions
      String jeditFolder = System.getProperty("jedit.home");
      completionsList.clear();

      File file = new File(jeditFolder, completionFileName);
      completionsList.addAll(buildAutoCompletionsFromFile(file));
      completionsList.addAll(getProjectFilesCompletions());
   }


   /**
    *  Deletes project specific autocompletion file
    */
   private static void removeProjectCompletions() {

      View actView = jEdit.getActiveView();
      VPTProject project = PVActions.getCurrentProject(actView);
      String projectRoot = project.getRootPath();
      File projectCompleteFile = new File(projectRoot,
            "project_autocomplete.txt");

      if(projectCompleteFile.exists()) {
         Log.log(Log.DEBUG, TideSideKickParser.class,
               "*** Removing file: " + projectCompleteFile.getPath());
         projectCompleteFile.delete();
      }
   }


   /**
    *  Parses the given file and builds an array with autocomplete words
    *
    *@param  file  The file object to parse
    *@return       An <code>ArrayList</code> with completions
    */
   private static ArrayList buildAutoCompletionsFromFile(File file) {

      ArrayList aList = new ArrayList();
      BufferedReader in = null;

      try {

         if(file != null) {
            in = new BufferedReader(new FileReader(file));

            for(String line; (line = in.readLine()) != null; ) {

               if(!line.startsWith("#") && line.indexOf("|") != -1) {

                  String funcName = line.substring(0, line.indexOf("|"));
                  aList.add(funcName);
               }
            }
         }
      }
      catch(IOException e) {
         e.printStackTrace();
      }
      finally {

         if(in != null) {

            try {
               in.close();
            }
            catch(IOException e) {
               e.printStackTrace();
            }
         }
      }

      /*
       *  aList.add("getTransform");
       *  aList.add("OpenALInitDriver");
       */
      // sort the list
      Collections.sort(aList);

      return aList;
   }


   /**
    *  Parses the given text and returns a tree model. This method is called by
    *  the sidekick plugin
    *
    *@param  buffer       The buffer to parse.
    *@param  errorSource  An error source to add errors to.
    *@return              A new instance of the <code>SideKickParsedData</code>
    *      class.
    */
   public SideKickParsedData parse(Buffer buffer,
         DefaultErrorSource errorSource) {

      SideKickParsedData data = new SideKickParsedData(buffer.getName());

      return data;
   }


   /**
    *  Description of the Method
    *
    *@param  path    Description of the Parameter
    *@param  reader  Description of the Parameter
    */
   public void parse(String path, Reader reader) {
   }


   /**
    *  Description of the Method
    */
   public void stop() {
   }


   /**
    *  Description of the Method
    *
    *@return    Description of the Return Value
    */
   public boolean supportsCompletion() {

      return true;
   }


   /**
    *  Description of the Method
    *
    *@return    boolean
    */
   public boolean canCompleteAnywhere() {

      return true;
   }


   /**
    *  get possible completions for a given (sub)string
    *
    *@param  part  substring to compare against
    *@return       An <code>ArrayList</code> with possible completions
    */
   public ArrayList getCompletionBySubstr(String part) {

      ArrayList retlist = new ArrayList();
      Object[] objects = completionsList.toArray();

      for(int i = 0; i < objects.length; i++) {

         String str = (String) objects[i];

         if(str.startsWith(part)) {
            retlist.add(str);
         }
      }

      return retlist;
   }


   /**
    *  main completion function
    *
    *@param  editPane  Description of the Parameter
    *@param  caret     Description of the Parameter
    *@return           <code>SideKickCompletion</code>
    */
   public SideKickCompletion complete(EditPane editPane, int caret) {

      if(!options.getAutocomplete()) {

         return null;
      }

      //Log.log(Log.DEBUG, this, "Requesting sidekick complete");
      Buffer buffer = editPane.getBuffer();
      JEditTextArea textArea = editPane.getTextArea();
      int caretLine = textArea.getCaretLine();
      int caretInLine = caret - buffer.getLineStartOffset(caretLine);

      if(caretInLine == 0) {

         return null;
      }

      String line = buffer.getLineText(caretLine);
      int wordStart = TextUtilities.findWordStart(line, caretInLine - 1, "");
      String currentWord = line.substring(wordStart, caretInLine);
      TideSideKickCompletion tideSideKickCompletion = null;
      String lastWord2 = getPreviousWord(caret, buffer);

      /*
       *  if (lastWord2.equalsIgnoreCase("datablock") && currentWord.equals(" "))
       *  {
       *  /Log.log(Log.DEBUG, TideSideKickParser.class, "currentWord : '"+currentWord+"'");
       *  tideSideKickCompletion = new TideSideKickCompletion("", currentWord + " ");
       *  tideSideKickCompletion.addItem("PlayerData", "datablock");
       *  tideSideKickCompletion.addItem("AudioProfile", "datablock");
       *  return tideSideKickCompletion;
       *  }
       *  / hm, doesnt really make sense for functions, does it?
       *  else if (lastWord2.equalsIgnoreCase("function") && currentWord.equals(" ")) {
       *  Log.log(Log.DEBUG, TideSideKickParser.class, "currentWord : '"+currentWord+"'");
       *  tideSideKickCompletion = new TideSideKickCompletion("", currentWord + " ");
       *  tideSideKickCompletion.addItem("getTransform", "function");
       *  tideSideKickCompletion.addItem("myfunc2", "function");
       *  return tideSideKickCompletion;
       *  }
       *  else
       */
      if(!currentWord.equals(" ")) {

         // get completions from txt file
         ArrayList possibleCompletions = getCompletionBySubstr(lastWord2);

         // add buffer keywords
         //Completion[] bufferCompletions = getBufferCompletions(jEdit.getBuffers(), currentWord, caret);
         Completion[] bufferCompletions = getBufferCompletions(jEdit.getBuffers(),
               currentWord);

         if(bufferCompletions.length > 0) {

            for(int i = 0; i < bufferCompletions.length; i++) {

               if(!bufferCompletions[i].toString().equals(lastWord2)) {
                  possibleCompletions.add(bufferCompletions[i].toString());
               }
            }
         }

         // sort entries
         Collections.sort(possibleCompletions);

         if(possibleCompletions.size() > 0) {
            tideSideKickCompletion = new TideSideKickCompletion(
                  currentWord, lastWord2);

            for(int i = 0; i < possibleCompletions.size(); i++) {
               tideSideKickCompletion.addItem(possibleCompletions.get(i),
                     currentWord);
            }

            return tideSideKickCompletion;
         }
      }

      return null;
   }


   /**
    *  Returns the previous word in a buffer.
    *
    *@param  caret   the caret position
    *@param  buffer  the buffer
    *@return         the previous word or ""
    */
   private static String getPreviousWord(int caret, Buffer buffer) {

      int i;

      for(i = caret - 1; i > 0; i--) {

         char c = buffer.getText(i, 1).charAt(0);

         if(!Character.isWhitespace(c)) {

            break;
         }
      }

      int j;

      for(j = i - 1; j > 0; j--) {

         char c = buffer.getText(j, 1).charAt(0);

         //if (Character.isWhitespace(c)) break;
         if(Character.isWhitespace(c) || c == '.' || c == ':') {

            break;
         }
      }

      return buffer.getText(j + 1, i - j);
   }


   /**
    *  Static function to check for or create file-based autocompletions (if
    *  file is not available yet)
    *
    *@return    <code>ArrayList</code> with project specific completions
    */
   private static ArrayList getProjectFilesCompletions() {

      ArrayList retList = new ArrayList();
      View actView = jEdit.getActiveView();
      VPTProject project = PVActions.getCurrentProject(actView);
      String projectRoot = project.getRootPath();
      BufferedWriter out = null;

      try {

         File projectCompleteFile = new File(projectRoot,
               "project_autocomplete.txt");

         if(projectCompleteFile.exists()) {
            Log.log(Log.DEBUG, TideSideKickParser.class,
                  "***READING FILE: *** " +
                  projectCompleteFile.getPath());

            return buildAutoCompletionsFromFile(projectCompleteFile);
         }
         else {

            Collection projectFiles = project.getFiles();
            Iterator fileIt = projectFiles.iterator();
            out = new BufferedWriter(new FileWriter(projectCompleteFile.getPath()));

            while(fileIt.hasNext()) {

               VPTNode nextNode = (VPTNode) fileIt.next();

               if(nextNode != null && nextNode.isFile()) {

                  VPTFile vptFile = (VPTFile) nextNode;
                  File nextFile = vptFile.getFile();

                  if(nextFile != null) {

                     // parse file
                     String filePath = nextFile.getPath();

                     if(!filePath.toUpperCase().endsWith("CS") &&
                           !filePath.toUpperCase().endsWith("GUI")) {

                        continue;
                     }

                     //Log.log(Log.DEBUG, this, "nextFile.getPath(): " + filePath);
                     //Log.log(Log.DEBUG, this, "nextFile.getParent(): " + nextFile.getParent());
                     BufferedReader in = null;

                     try {
                        in = new BufferedReader(new FileReader(
                              nextFile));

                        for(String line;
                              (line = in.readLine()) != null; ) {

                           if(!line.trim().startsWith("//")) {

                              String nextToken = "";

                              // remove non-alphanum chars from line first and replace them with spaces
                              char[] nonalphas = new char[line.length()];
                              int nonalphacount = 0;

                              for(int l = 0; l < line.length(); l++) {

                                 // leave comments intact for now
                                 boolean isComment = false;

                                 if(line.charAt(l) == '/' &&
                                       line.charAt(l + 1) == '/') {
                                    isComment = true;
                                 }

                                 if(!Character.isLetterOrDigit(line.charAt(
                                       l)) &&
                                       !isComment) {
                                    nonalphas[nonalphacount] = line.charAt(
                                          l);
                                    nonalphacount++;
                                 }
                              }

                              for(int c = 0;
                                    c < nonalphas.length;
                                    c++) {
                                 line = line.replace(nonalphas[c],
                                       ' ');
                              }

                              //Log.log(Log.DEBUG, this, "***LINE*** " + line);
                              //synchronized(retList)
                              //{
                              StringTokenizer st =
                                    new StringTokenizer(line);

                              while(st.hasMoreTokens()) {
                                 nextToken = st.nextToken();

                                 // comment before this token?
                                 if(line.substring(0,
                                       line.indexOf(
                                       nextToken))
                                       .indexOf("//") != -1) {

                                    break;
                                 }

                                 // check if the token contains only digits
                                 boolean containsOnlyDigits = true;

                                 for(int s = 0;
                                       s < nextToken.length();
                                       s++) {

                                    if(Character.isLetter(nextToken.charAt(
                                          s))) {
                                       containsOnlyDigits = false;
                                    }
                                 }

                                 //Log.log(Log.DEBUG, this, "***NEXT TOKEN*** " + nextToken);
                                 if(!retList.contains(nextToken) &&
                                       nextToken.length() > 2 &&
                                       !containsOnlyDigits) {
                                    retList.add(nextToken);

                                    //Log.log(Log.DEBUG, this, "***ADDING NEXT TOKEN*** " + nextToken);
                                 }
                              }

                              //}
                           }
                        }
                     }
                     catch(IOException e) {
                        e.printStackTrace();
                     }
                     finally {

                        if(in != null) {

                           try {
                              in.close();
                           }
                           catch(IOException e) {
                              e.printStackTrace();
                           }
                        }
                     }

                     /*
                      *  Buffer tempBuf = jEdit.openTemporary(actView, nextFile.getParent(), filePath, true);
                      *  if(tempBuf != null)
                      *  {
                      *  /Log.log(Log.DEBUG, this, "buffer.toString(): " + tempBuf.toString());
                      *  try
                      *  {
                      *  KeywordMap tempMap = tempBuf.getKeywordMapAtOffset(0);
                      *  if(tempMap != null)
                      *  {
                      *  String[] keywords = tempMap.getKeywords();
                      *  for(int i=0; i<keywords.length;i++)
                      *  retList.add(keywords[i]);
                      *  }
                      *  }
                      *  catch(Exception ex)
                      *  {
                      *  Log.log(Log.ERROR, this, "Error getting buffer keywords: " + ex.getMessage());
                      *  ex.printStackTrace();
                      *  }
                      *  }
                      */
                  }
               }
            }

            // sort the list
            Collections.sort(retList);

            // write the list to file for later use
            Iterator retListIter = retList.iterator();

            while(retListIter.hasNext()) {
               out.write(retListIter.next() + "|\n");
            }
         }
      }
      catch(Exception ex) {
         Log.log(Log.ERROR, TideSideKickParser.class,
               "Error getting project autocompletes: " +
               ex.getMessage());
         ex.printStackTrace();
      }
      finally {

         if(out != null) {

            try {
               out.close();
            }
            catch(IOException e) {
               e.printStackTrace();
            }
         }
      }

      return retList;
   }


   /**
    *  Gets the nonAlphaNumericWordChars attribute of the TideSideKickParser
    *  class
    *
    *@param  buffer      Description of the Parameter
    *@param  keywordMap  Description of the Parameter
    *@return             The nonAlphaNumericWordChars value
    */
   private static String getNonAlphaNumericWordChars(Buffer buffer,
         KeywordMap keywordMap) {

      // figure out what constitutes a word character and what
      // doesn't
      String noWordSep = buffer.getStringProperty("noWordSep");

      if(noWordSep == null) {
         noWordSep = "";
      }

      if(keywordMap != null) {

         String keywordNoWordSep = keywordMap.getNonAlphaNumericChars();

         if(keywordNoWordSep != null) {
            noWordSep = noWordSep + keywordNoWordSep;
         }
      }

      return noWordSep;
   }


   //private static Completion[] getBufferCompletions(Buffer buffer, String word,
   //int caret)

   /**
    *  Gets the bufferCompletions attribute of the TideSideKickParser class
    *
    *@param  currBuffers  Description of the Parameter
    *@param  word         Description of the Parameter
    *@return              The bufferCompletions value
    */
   private static Completion[] getBufferCompletions(Buffer[] currBuffers,
         String word) {

      // build a list of unique words in all visible buffers
      Set completions = new TreeSet(new MiscUtilities.StringCompare());
      Set buffers = new HashSet();

      // only complete current buffer's keyword map

      /*
       *  KeywordMap keywordMap = buffer.getKeywordMapAtOffset(caret);
       *  String noWordSep = getNonAlphaNumericWordChars(
       *  buffer,keywordMap);
       */
      for(int i = 0; i < currBuffers.length; i++) {

         Buffer b = currBuffers[i];

         //Log.log(Log.DEBUG, TideSideKickParser.class, "BUFFER PATH: " + b.getPath());
         if(buffers.contains(b)) {

            continue;
         }

         buffers.add(b);

         //KeywordMap keywordMap = b.getKeywordMapAtOffset(caret);
         KeywordMap keywordMap = b.getKeywordMapAtOffset(0);
         String noWordSep = getNonAlphaNumericWordChars(b, keywordMap);

         // only complete current buffer's keyword map
         KeywordMap _keywordMap;

         //if(b == buffer)
         _keywordMap = keywordMap;

         /*
          *  else
          *  _keywordMap = null;
          *  int offset = (b == buffer ? caret : 0);
          */
         int offset = 0;
         getCompletions(b, word, keywordMap, noWordSep, offset, completions);
      }

      Completion[] completionArray = (Completion[]) completions.toArray(
            new Completion[completions.size()]);

      return completionArray;
   }


   /**
    *  Gets the completions attribute of the TideSideKickParser class
    *
    *@param  buffer       Description of the Parameter
    *@param  word         Description of the Parameter
    *@param  keywordMap   Description of the Parameter
    *@param  noWordSep    Description of the Parameter
    *@param  caret        Description of the Parameter
    *@param  completions  Description of the Parameter
    */
   private static void getCompletions(Buffer buffer, String word,
         KeywordMap keywordMap, String noWordSep,
         int caret, Set completions) {

      int wordLen = word.length();

      if(keywordMap != null) {

         String[] keywords = keywordMap.getKeywords();

         for(int i = 0; i < keywords.length; i++) {

            String _keyword = keywords[i];

            if(_keyword.regionMatches(keywordMap.getIgnoreCase(), 0, word,
                  0, wordLen)) {

               Completion keyword = new Completion(_keyword, true);

               if(!completions.contains(keyword)) {
                  completions.add(keyword);
               }
            }
         }
      }

      for(int i = 0; i < buffer.getLineCount(); i++) {

         String line = buffer.getLineText(i);
         int start = buffer.getLineStartOffset(i);

         // check for match at start of line
         //if(line.startsWith(word) && caret != start + word.length())
         if(!line.trim().startsWith("//") && line.startsWith(word) &&
               caret != start + word.length()) {

            String _word = completeWord(line, 0, noWordSep);
            Completion comp = new Completion(_word, false);

            // remove duplicates
            if(!completions.contains(comp)) {
               completions.add(comp);
            }
         }

         // check for match inside line
         int len = line.length() - word.length();

         for(int j = 0; j < len; j++) {

            char c = line.charAt(j);

            if(!Character.isLetterOrDigit(c) &&
                  noWordSep.indexOf(c) == -1) {

               if(line.regionMatches(j + 1, word, 0, wordLen) &&
                     caret != start + j + word.length() + 1) {

                  String _word = completeWord(line, j + 1, noWordSep);

                  // check for comment chars before word
                  if(line.substring(0, line.indexOf(_word)).indexOf("//") == -1) {

                     Completion comp = new Completion(_word, false);

                     // remove duplicates
                     if(!completions.contains(comp)) {
                        completions.add(comp);
                     }
                  }
               }
            }
         }
      }
   }


   /**
    *  Description of the Method
    *
    *@param  line       Description of the Parameter
    *@param  offset     Description of the Parameter
    *@param  noWordSep  Description of the Parameter
    *@return            Description of the Return Value
    */
   private static String completeWord(String line, int offset,
         String noWordSep) {

      // '+ 1' so that findWordEnd() doesn't pick up the space at the start
      int wordEnd = TextUtilities.findWordEnd(line, offset + 1, noWordSep);

      return line.substring(offset, wordEnd);
   }


   // *************************************************************************
   // OPEN BUFFERS COMPLETION
   // *************************************************************************

   /**
    *  Description of the Class
    *
    *@author        beffy
    *@created       27. November 2006
    *@deprecated    27. November 2006
    */
   static class Completion {

      boolean keyword;
      String text;


      /**
       *  Constructor for the Completion object
       *
       *@param  text     Description of the Parameter
       *@param  keyword  Description of the Parameter
       */
      Completion(String text, boolean keyword) {
         this.text = text;
         this.keyword = keyword;
      }


      /**
       *  Description of the Method
       *
       *@return    Description of the Return Value
       */
      public String toString() {

         return text;
      }


      /**
       *  Description of the Method
       *
       *@return    Description of the Return Value
       */
      public int hashCode() {

         return text.hashCode();
      }


      /**
       *  Description of the Method
       *
       *@param  obj  Description of the Parameter
       *@return      Description of the Return Value
       */
      public boolean equals(Object obj) {

         if(obj instanceof Completion) {

            return ((Completion) obj).text.equals(text);
         }
         else {

            return false;
         }
      }
   }
}

