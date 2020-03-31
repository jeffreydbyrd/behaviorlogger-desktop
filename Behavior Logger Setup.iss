; -- Behavior Logger.iss --
; How to use this
; 1. Build the JavaFX installer first through eclipse
; 2. Run that installer
; 3. Go into the directory that it creates (/user/AppData/Behavior Logger/)
; 4. Copy everything except the "1.1" folder and the "unint000" file
; 5. Paste into a new directory like "bl-1.1.x-setup"
; 6. Put this script in that directory also
; 7. This script basically installs all those copied files into the {app} directory and *uninstalls* BL 1.0 (if it exists)

[Setup]
AppName=Behavior Logger
AppVersion=1.1.2
AppId=2b29f03d-be34-4ce7-b834-337ed62c89c0
DefaultDirName={pf}\Behavior Logger
DefaultGroupName=Behavior Logger
UninstallDisplayIcon={app}\Behavior Logger.exe
Compression=lzma2
SolidCompression=yes
OutputDir=C:\Users\jeffr\Downloads

[Files]
Source: "Behavior Logger.exe"; DestDir: "{app}"; BeforeInstall: MyBeforeInstall
Source: "*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs

[Icons]
Name: "{group}\Behavior Logger"; Filename: "{app}\Behavior Logger.exe"; WorkingDir: "{app}"

[Code]
/////////////////////////////////////////////////////////////////////
function GetUninstallString(): String;
var
  sUnInstPath: String;
  sUnInstallString: String;
begin
  sUnInstPath := ExpandConstant('Software\Microsoft\Windows\CurrentVersion\Uninstall\{{fxApplication}}_is1');
  sUnInstallString := '';
  if not RegQueryStringValue(HKLM, sUnInstPath, 'UninstallString', sUnInstallString) then
    RegQueryStringValue(HKCU, sUnInstPath, 'UninstallString', sUnInstallString);
  Result := sUnInstallString;
end;


/////////////////////////////////////////////////////////////////////
function IsUpgrade(): Boolean;
begin
  Result := (GetUninstallString() <> '');
end;


/////////////////////////////////////////////////////////////////////
function UnInstallOldVersion(): Integer;
var
  sUnInstallString: String;
  iResultCode: Integer;
begin
// Return Values:
// 1 - uninstall string is empty
// 2 - error executing the UnInstallString
// 3 - successfully executed the UnInstallString

  // default return value
  Result := 0;

  // get the uninstall string of the old app
  sUnInstallString := GetUninstallString();
  if sUnInstallString <> '' then begin
    sUnInstallString := RemoveQuotes(sUnInstallString);
    if Exec(sUnInstallString, '/NORESTART','', SW_HIDE, ewWaitUntilTerminated, iResultCode) then
      Result := 3
    else
      Result := 2;
  end else
    Result := 1;
end;

/////////////////////////////////////////////////////////////////////
procedure MyBeforeInstall();
begin
  if (IsUpgrade()) then
  begin
    // Ask the user to uninstall old version   
    MsgBox('Setup needs to uninstall an older version of Behavior Logger. Your data and schemas will NOT be affected.',
            mbInformation, 
            MB_OK);
    UnInstallOldVersion();
  end;
end;
