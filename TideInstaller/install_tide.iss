[Setup]
AppName=TorqueIDE (TIDE)
AppVerName=TorqueIDE 1.3
AppPublisher=beffy
AppPublisherURL=http://torqueide.sourceforge.net
AppSupportURL=http://torqueide.sourceforge.net/docs/faq.html
;AppUpdatesURL=https://sourceforge.net/project/showfiles.php?group_id=97831&package_id=104712
AppUpdatesURL=https://sourceforge.net/project/showfiles.php?group_id=97831
DefaultDirName={pf}\Tide
DefaultGroupName=Tide
;AllowNoIcons=yes
LicenseFile=D:\work_projects\_svn_kunden\torqueide\installer\gpl.txt
InfoBeforeFile=D:\work_projects\_svn_kunden\torqueide\installer\infobefore.txt
Compression=lzma
SolidCompression=yes
OutputBaseFilename=Tide1.3

[Languages]
Name: en; MessagesFile: compiler:Default.isl
Name: de; MessagesFile: compiler:Languages\German.isl

[Messages]
en.BeveledLabel=English
de.BeveledLabel=Deutsch

[Components]
Name: main; Description: TorqueIDE Files; Types: full compact custom; Flags: fixed
Name: jdk; Description: Java Runtime Environment 6 (JRE); Types: full compact custom
Name: jedit; Description: JEdit; Types: full compact custom

[Tasks]

[Files]
Source: jre-6u13-windows-i586-p.exe; DestDir: {app}; Flags: ignoreversion deleteafterinstall; Components: jdk
Source: jedit4.3pre16install.exe; DestDir: {app}; Flags: ignoreversion deleteafterinstall; Components: jedit
; TODO: copy config.properties in the CODE section AFTER jEdit was installed!
Source: user\projectviewer\config.properties; DestDir: {app}\.jedit\projectviewer\; Flags: overwritereadonly ignoreversion touch replacesameversion
Source: files\*.*; DestDir: {app}\jedit\modes\; Flags: ignoreversion touch
Source: jedit\*.*; DestDir: {app}\jedit; Flags: recursesubdirs ignoreversion overwritereadonly touch; Components: main
;Source: user\projectviewer\config.properties; DestDir: {code:GetUserHome}\.jedit\projectviewer\; Flags: overwritereadonly ignoreversion replacesameversion
;Source: files\*.*; DestDir: {code:GetDataDir}\modes\; Flags: ignoreversion touch
;Source: jedit\*.*; DestDir: {code:GetDataDir}; Flags: recursesubdirs ignoreversion overwritereadonly touch; Components: main

[INI]
Filename: {app}\tide.url; Section: InternetShortcut; Key: URL; String: http://torqueide.sourceforge.net
Filename: {app}\tide_faq.url; Section: InternetShortcut; Key: URL; String: http://torqueide.sourceforge.net/docs/faq.html

[Icons]
Name: {group}\Tide Online; Filename: {app}\tide.url
Name: {group}\Tide FAQ; Filename: {app}\tide_faq.url
Name: {group}\{cm:UninstallProgram,Tide}; Filename: {uninstallexe}

[Run]
Filename: {app}\jre-6u13-windows-i586-p; Description: Installing JRE; Components: jdk; StatusMsg: Installing JRE ...
Filename: {app}\jedit4.3pre16install.exe; Description: Installing JEdit.; StatusMsg: Installing JEdit ...; Components: jedit

[UninstallDelete]
Type: files; Name: {app}\tide.url
Type: files; Name: {app}\tide_faq.url

;  MsgBox(AppData + ' Result:'  + Result, mbInformation, MB_OK);
[Code]
var
	AppData:String;
	UserName:String;
	Len:Integer;
	NamePos:Integer;
	EndPos:Integer;

function GetUserHome(Param: String): String;
begin
  AppData:=ExtractFileDir(ExpandConstant('{userappdata}'));
  UserName:=GetUserNameString();
  Len:=Length(UserName);
  NamePos:=Pos(UserName, AppData);
  EndPos:=NamePos+Len-1;
  Result:=Copy(AppData,0,EndPos);
end;


var
  DataDirPage: TInputDirWizardPage;
  DataDir: String;

procedure InitializeWizard;
begin
  { Create the pages }
	  DataDirPage := CreateInputDirPage(wpInfoAfter,
		'Select jEdit Directory', 'Where is jEdit installed?',
		'Select the folder in which jEdit is installed.',
		False, '');
	  DataDirPage.Add('');
	  DataDirPage.Values[0] := ExpandConstant('{pf}\jEdit\');

end;

function GetDataDir(Param: String): String;
begin
  { Return the selected DataDir }
  Result := DataDirPage.Values[0];
end;

var
  FilesFound: Integer;
  FindRec: TFindRec;
  Copied: Boolean;
function CopyFiles(Source, Destination, FileSpec: String): Integer;
begin
  FilesFound := 0;
  if FindFirst(FileSpec, FindRec) then begin
    try
      repeat
        // Don't count directories
        if FindRec.Attributes and FILE_ATTRIBUTE_DIRECTORY = 0 then
          begin
			  FilesFound := FilesFound + 1;
			  Copied := FileCopy(Source + FindRec.Name, Destination + FindRec.Name, false);
			  { MsgBox('Copying ' + Source + FindRec.Name + ' to ' + Destination + FindRec.Name + ' directory.', mbInformation, MB_OK); }
		  end;
      until not FindNext(FindRec);
    finally
      FindClose(FindRec);
    end;
  end;
  Result:=FilesFound;
end;


var
	FilesCopied: Integer;
	Destination: String;
	FileSpec: String;
	Source: String;
	UserHomeDir: String;
procedure CurPageChanged(CurPageID: Integer);
begin
  case CurPageID of
    wpFinished:
		begin

		    FileSpec := ExpandConstant('{app}\jedit\*');
		    Source := ExpandConstant('{app}\jedit\');
		    Destination := DataDirPage.Values[0] + '\';

			UserHomeDir := GetUserHome(UserHomeDir);
			MsgBox('Finishing Installation:' #13#13 'Setup will copy some TIDE files to ' + DataDirPage.Values[0] + ' and ' + UserHomeDir + '\.jedit\ now.', mbInformation, MB_OK);
			FilesCopied := CopyFiles(Source, Destination, FileSpec);

		    FileSpec := ExpandConstant('{app}\jedit\jars\*');
		    Source := ExpandConstant('{app}\jedit\jars\');
		    Destination := DataDirPage.Values[0] + '\jars\';
			FilesCopied := FilesCopied + CopyFiles(Source, Destination, FileSpec);

		    FileSpec := ExpandConstant('{app}\jedit\modes\*');
		    Source := ExpandConstant('{app}\jedit\modes\');
		    Destination := DataDirPage.Values[0] + '\modes\';
			FilesCopied := FilesCopied + CopyFiles(Source, Destination, FileSpec);

		    FileSpec := ExpandConstant('{app}\.jedit\projectviewer\*');
		    Source := ExpandConstant('{app}\.jedit\projectviewer\');
		    Destination := UserHomeDir + '\.jedit\projectviewer\';
			{ MsgBox('UserHomeDir Destination: ' + Destination, mbInformation, MB_OK); }
			FilesCopied := FilesCopied + CopyFiles(Source, Destination, FileSpec);

			MsgBox(IntToStr(FilesCopied) + ' TIDE files copied.', mbInformation, MB_OK);
		end;
  end;
end;



function NextButtonClick(CurPageID: Integer): Boolean;
var
  ResultCode: Integer;
begin
  case CurPageID of
    wpSelectDir:
  end;

  Result := True;
end;

procedure AfterMyProgInstall(S: String);
begin
  MsgBox('AfterMyProgInstall:' #13#13 'Setup just installed ' + S + ' as ' + CurrentFileName + '.', mbInformation, MB_OK);
end;
