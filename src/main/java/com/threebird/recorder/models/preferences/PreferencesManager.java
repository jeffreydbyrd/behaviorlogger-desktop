package com.threebird.recorder.models.preferences;

import java.io.File;

import javafx.beans.property.SimpleStringProperty;

import com.google.common.base.Preconditions;
import com.threebird.recorder.persistence.Preferences;

public class PreferencesManager
{
  private static SimpleStringProperty sessionDirectoryProperty;

  public static SimpleStringProperty sessionDirectoryProperty()
  {
    if (sessionDirectoryProperty == null) {
      String dir = Preferences.getSessionDirectory();
      if (dir == null || !new File( dir ).exists()) {
        dir = System.getProperty( "user.home" );
      }
      sessionDirectoryProperty = new SimpleStringProperty( dir );
    }
    return sessionDirectoryProperty;
  }

  public static void saveSessionDirectory( String dir )
  {
    Preconditions.checkNotNull( dir );
    sessionDirectoryProperty().set( dir );
  }

  public static String getSessionDirectory()
  {
    return sessionDirectoryProperty().get();
  }
}
