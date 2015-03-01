package com.threebird.recorder.utils.resources;

import java.io.File;

class OsxResourceService implements ResourceService
{
  File resources = getResources();

  @Override public File getResources()
  {
    String home = System.getProperty( "user.home" );
    String resourcesPath = String.format( "%s/Library/Application Support/3bird/event-recorder/resources/", home );
    return new File( resourcesPath );
  }

  @Override public File getDb()
  {
    String path = resources.getAbsolutePath() + "/recorder.db";
    return new File( path );
  }

  @Override public File getPrefs()
  {
    resources.mkdirs();
    return new File( resources.getAbsolutePath() + "/prefs.json" );
  }

  @Override public File getSessionDetails()
  {
    String path = resources.getAbsolutePath() + "/session-details.json";
    return new File( path );
  }

  @Override public File getIoaDetails()
  {
    String path = resources.getAbsolutePath() + "/ioa-details.json";
    return new File( path );
  }
}
