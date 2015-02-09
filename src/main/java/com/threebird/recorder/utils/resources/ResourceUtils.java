package com.threebird.recorder.utils.resources;

import java.io.File;

public class ResourceUtils
{
  public static ResourceService service;

  private static ResourceService service()
  {
    if (service == null) {
      String osName = System.getProperty( "os.name" );
      if (osName.equals( "Mac OS X" )) {
        service = new OsxResourceService();
      } else {
        throw new IllegalStateException( "Unsupported Operating System: " + osName );
      }
    }

    return service;
  }

  public static boolean createDB()
  {
    return service().createDB();
  }

  public static File getPrefs()
  {
    return service().getPrefs();
  }

  public static File getResources()
  {
    return service().getResources();
  }

  public static String getDbPath()
  {
    return service().getDbPath();
  }

}
