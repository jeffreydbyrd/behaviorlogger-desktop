package com.threebird.recorder.utils.resources;

import java.io.File;

public class ResourceUtils
{
  private static File resources;

  private static File resources()
  {
    if (resources != null) {
      return resources;
    }

    String osName = System.getProperty( "os.name" );
    if (osName.equals( "Mac OS X" )) {
      String home = System.getProperty( "user.home" );
      String resourcesPath = String.format( "%s/Library/Application Support/3bird/event-recorder/resources/", home );
      resources = new File( resourcesPath );
    } else if (osName.toUpperCase().contains( "WIN" )) {
      String path = System.getenv( "localappdata" ) + "/3bird/event-recorder/resources/";
      resources = new File( path );
    } else {
      throw new IllegalStateException( "Unsupported Operating System: " + osName );
    }

    resources.mkdirs();
    return resources;
  }

  public static File getDb()
  {
    resources().mkdirs();
    String path = resources().getAbsolutePath() + "/recorder.db";
    return new File( path );
  }

  public static File getPrefs()
  {
    resources().mkdirs();
    return new File( resources.getAbsolutePath() + "/prefs.json" );
  }

  public static File getSessionDetails()
  {
    resources().mkdirs();
    String path = resources().getAbsolutePath() + "/session-details.json";
    return new File( path );
  }

  public static File getIoaDetails()
  {
    String path = resources().getAbsolutePath() + "/ioa-details.json";
    return new File( path );
  }

  public static File getManual()
  {
    String currentDir = System.getProperty( "user.dir" );
    String path = currentDir + "/resources/manual/manual.html";
    return new File( path );
  }

  public static File getPositionDetails()
  {
    String path = resources().getAbsolutePath() + "/positions.json";
    return new File( path );
  }
}
