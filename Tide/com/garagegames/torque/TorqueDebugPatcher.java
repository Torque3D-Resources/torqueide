// copyright (c) 2002, Paul Dana.
// Distributed under the GNU GPL: http://www.fsf.org/copyleft/gpl.html

// TorqueDebugPatcher

// Determines if User's torque based game supports -dbgEnable and the other
// -dbg commandline options which allow a game to enable telnet based
// debugging automatically upon startup. If the user's Torque based game
// does not support these -dbgXXXX options then this class allows the user
// to automatically patch their script files so they DO support these features

package com.garagegames.torque;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;
import org.gjt.sp.util.Log;

public class TorqueDebugPatcher
{
   File gamePath;
   File mainCS;
   boolean checkedForDebugOptions;
   boolean debugOptionsFound;

   // these many patches...
   int numPatches = 4;
   String[] patch;
   String[] after;

   String origMainCS;       // our idea of the "unpatched" main.cs file
   String patchedMainCS;    // our idea of the "patched" main.cs file

   // construct
   public TorqueDebugPatcher(File gamePath)
   {
      this.gamePath = gamePath;

      // these files contain the lines of code to insert for patch
      patch = new String[numPatches];
      patch[0] = "TorqueDebugPatchA.txt";
      patch[1] = "TorqueDebugPatchB.txt";
      patch[2] = "TorqueDebugPatchC.txt";
      patch[3] = "TorqueDebugPatchD.txt";

      // we insert the patches after we match this string to a line of code...
      after = new String[numPatches];
      after[0] = "// Process command line arguments";
      after[1] = "//--------------------";
      //after[2] = "Start up the window console";
      //beffy
      after[2] = "Play back a journal and issue an int3 at the end";
      after[3] = "echo(\"Engine initialized...\");";

      // we have a copy of the "patched" and "unpatched" files incase the user
      // wishes to patch the code themselves and wants an example of
      // what is involved in a patch
      origMainCS = "TorqueDebugMainCS.txt";
      patchedMainCS = "TorqueDebugMainCSWithDebug.txt";
   }

   // patch game
   public void patchGame(Class resourceClass, ZipFile jarFile)
   throws Exception
   {
      // if it already has these options then it dont need patching
      if (hasDebugOptions())
         return;

      // we are going to write to a temporary file
      File newMainCS = new File(gamePath.getParent(),"TorqueDebugPatcherNewMainCS.txt");
      PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(newMainCS)));

      // we need to read from the main.cs file
      BufferedReader br = new BufferedReader(new FileReader(mainCS.getAbsolutePath()));

      // start with the first patch
      int thisPatch = 0;

      // read lines from original file and write lines to the new flie
      for (;;)
      {
         // readline from input
         String line = br.readLine();
         if (line == null)
            break;

         // write this line to output
         ps.println(line);

         // if we are done patching...
         if (thisPatch >= numPatches)
            continue;

         // search for the 'after' text in this line
         if (line.indexOf(after[thisPatch]) >= 0)
         {
            // insert the patch here!
            insertPatch(ps,resourceClass, patch[thisPatch], jarFile);

            // onto next patch!
            thisPatch++;
         }
      }

      ps.close();
      br.close();

      // this is important...if we dont explicitly free the memory now then
      // we might still have the file open for reading when we are trying
      // to rename it...which will throw an exception.
      br = null;

      // if we did not do enough patches
      // then we failed to patch it correctly
      if (thisPatch != numPatches)
         throw new TorqueDebugException("Patch Failed: unable to apply all patches");

      // if we have one already
      File savedMainCS = new File(gamePath.getParent(),"main_prePatch.cs");
      if (savedMainCS.exists())
      {
         // if we cant delete this then rename to random name
         if (!savedMainCS.canWrite())
         {
            File tmpFile;
            for (int i=0;;i++)
            {
               tmpFile = new File(gamePath.getParent(),"main_prePatch"+i+".cs");
               if (!tmpFile.exists())
                  break;
            }
            savedMainCS.renameTo(tmpFile);
         }
         else
         {
            savedMainCS.delete();
         }
      }

      // now we gotta rename the original and copy in ours!
      if(!mainCS.renameTo(savedMainCS))
      {
         try
         {
            renameFile(mainCS, savedMainCS);
         }
         catch(Exception ex)
         {
            String renameMainError = "Patch Failed: error renaming main.cs to main_prePatch.cs\n";
            renameMainError += "Please rename the file 'main.cs' to 'main_prePatch.cs' manually!";
            renameMainError += "Message is:\n" + ex.getMessage();
            throw new TorqueDebugException(renameMainError);
         }
      }

      // attempt to rename tempfile to main.cs
      if (!newMainCS.renameTo(new File(gamePath.getParent(),"main.cs")))
      {
         try
         {
            renameFile(newMainCS, new File(gamePath.getParent(),"main.cs"));
         }
         catch(Exception ex)
         {
            String renameMainError = "Patch Failed: error renaming tempfile to main.cs\n";
            renameMainError += "Please rename the file 'TorqueDebugPatcherNewMainCS.txt' to 'main.cs' manually!";
            renameMainError += "Message is:\n" + ex.getMessage();
            throw new TorqueDebugException(renameMainError);
         }
      }
   }

   /*
   * Java has a File.renameTo() bug so if renameTo() doesnt work on the client's JVM
   * we deletet the old file and write its contents to a new one
   */
   public void renameFile(File old, File nu) throws Exception
   {
      try
      {
         byte[] buf = new byte[1024];
         java.io.InputStream in = new FileInputStream(old);
         java.io.OutputStream out = new FileOutputStream(nu);
         int len;
         while ((len = in.read(buf)) >= 0)
         {
            out.write(buf, 0, len);
         }
         in.close();
         out.close();

         // Delete the old file.
         old.delete();
      }
      catch (IOException ioe)
      {
         throw new IOException("Couldn't rename file " + old.getName() + " to " + nu.getName());
      }
   }


   // return true if the game already has the needed -dbgXXXX options
   public boolean hasDebugOptions()
   throws Exception
   {
      if (checkedForDebugOptions)
         return debugOptionsFound;

      // if the game does not even exist...this is a prob
      if (gamePath == null)
         throw new TorqueDebugException("TorqueDebugPatcher: gamePath == null");
      if (!gamePath.exists())
         throw new TorqueDebugException(
            "TorqueDebugPatcher: gamePath does not exist: " +
            gamePath.getAbsolutePath());

      // all the features are in the ~/main.cs file
      mainCS = new File(gamePath.getParent(),"main.cs");
      if (!mainCS.exists())
         throw new TorqueDebugException(
            "TorqueDebugPatcher: main.cs does not exist: " +
            mainCS.getAbsolutePath());

      // ok we have checked
      checkedForDebugOptions = true;

      // if we find the $GameDebugEnabled variable then we can assume
      // that all the other features are there as well...
      if (foundInFile(mainCS,"$GameDebugEnable"))
      {
         debugOptionsFound = true;
         return true;
      }

      // no debug options found
      return false;
   }

   // copy our idea of the original file to the game path as the given name
   public void copyOriginal(String name)
   {
      File f = new File(gamePath.getParent(),name);

      // now copy the original file we got
   }

   // copy our idea of a patched original file to the game path as the given name
   public void copyPatched(String name)
   {
      File f = new File(gamePath.getParent(),name);

      // now copy the original file we got
   }

   // get buffered reader for this file which exists as a resource
   // in the same folder as the class files. jarFile and parentFolder are
   // provided as a failsafe incase for some reason we cannot
   // get to the file as a resource. In this case the file be searched for
   // explicitly in the parent folder of the given .jar file
   private BufferedReader getBufferedReader(String parent, String name, ZipFile jarFile)
   throws Exception
   {
      return getBufferedReader(parent + "/" + name, jarFile);
   }
   private BufferedReader getBufferedReader(String name, ZipFile jarFile)
   throws Exception
   {
      Log.log(Log.DEBUG, this, "Trying to read file:" + name);
      BufferedReader br = null;
      InputStream istream = getClass().getResourceAsStream(name);
      if (istream == null)
      {
         // if we fail to get it as a resource...read from .jar directly
         String entryName;
         if (name.startsWith("/"))
            entryName = name.substring(1);
         else
            entryName = name;
         ZipEntry entry = null;
         if (jarFile != null)
            entry = jarFile.getEntry(entryName);
         if (entry == null)
         {
            // as a completely silly fallback just read from current folder
            br = new BufferedReader(new FileReader("."+name));
         }
         else
         {
            // otherwise use the entry
            br = new BufferedReader(new InputStreamReader(jarFile.getInputStream(entry)));
         }
      }
      else
      {
         br = new BufferedReader(new InputStreamReader(istream));
      }

      return br;
   }


   // insert this patchfile at this point, use jarFile as failsafe if patch file cant be found
   public void insertPatch(PrintStream ps, Class resourceClass, String patchFile, ZipFile jarFile)
   throws Exception
   {
      // open the patch file and copy all its lines to the print stream
      String name = "/com/garagegames/torque/" + patchFile;
      Log.log(Log.DEBUG, this, "Trying to patch file:" + name);
      BufferedReader br = getBufferedReader(name,jarFile);

      if (br == null)
         throw new TorqueDebugException("Cannot open patchfile: "+name);

      // readlines from patch file...copy to stream
      for(;;)
      {
         String line = br.readLine();
         if (line == null)
            break;
         ps.println(line);
      }

      br.close();
   }

   // return true if given string found in given file
   public boolean foundInFile(File f, String s)
   throws Exception
   {
      BufferedReader br = new BufferedReader(new FileReader(f.getAbsolutePath()));

      // read lines until end of file
      for (;;)
      {
         String line = br.readLine();
         if (line == null)
            break;

         // search for the substring in this line
         if (line.indexOf(s) >= 0)
            return true;
      }

      // if we get to end then we did not find it in the entire file
      return false;
   }
}
