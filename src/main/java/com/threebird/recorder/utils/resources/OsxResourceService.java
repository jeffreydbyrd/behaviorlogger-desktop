package com.threebird.recorder.utils.resources;

import java.io.File;
import java.io.IOException;

class OsxResourceService implements ResourceService
{
  File resources = getResources();

  @Override public boolean createDB()
  {
    try {
      resources.mkdirs();

      String filePath = resources.getAbsolutePath() + "/recorder.db";
      return new File( filePath ).createNewFile();
    } catch (IOException e) {
      throw new RuntimeException( e );
    }
  }

  @Override public File getPrefs()
  {
    resources.mkdirs();
    return new File( resources.getAbsolutePath() + "/prefs.json" );
  }

  @Override public String getDbPath()
  {
    return resources.getAbsolutePath() + "/recorder.db";
  }

  @Override public File getResources()
  {
    String home = System.getProperty( "user.home" );
    String resourcesPath = String.format( "%s/Library/Application Support/3bird/event-recorder/resources/", home );
    return new File( resourcesPath );
  }
}
