package com.threebird.recorder.utils.resources;

import java.io.File;

public class WinResourceService implements ResourceService
{
  private File resources = getResources();

  @Override public File getResources()
  {
    String path = System.getenv( "localappdata" ) + "\\3bird\\event-recorder\\resources\\";
    return new File( path );
  }

  @Override public File getDb()
  {
    String path = resources.getAbsolutePath() + "\\recorder.db";
    return new File( path );
  }

  @Override public File getPrefs()
  {
    resources.mkdirs();
    return new File( resources.getAbsolutePath() + "\\prefs.json" );
  }

  @Override public File getSessionDetails()
  {
    String path = resources.getAbsolutePath() + "\\session-details.json";
    return new File( path );
  }
}
