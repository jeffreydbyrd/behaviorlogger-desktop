package com.threebird.recorder.utils.resources;

import java.io.File;

public class ResourceUtils
{
  private static ResourceService service;

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

  public static File getDb()
  {
    return service().getDb();
  }

  public static File getPrefs()
  {
    return service().getPrefs();
  }

  public static File getResources()
  {
    return service().getResources();
  }

  public static File getSessionDetails()
  {
    return service().getSessionDetails();
  }

}
