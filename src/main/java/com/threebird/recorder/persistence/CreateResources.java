package com.threebird.recorder.persistence;

import java.io.File;
import java.io.IOException;

import com.google.common.io.Files;
import com.threebird.recorder.models.preferences.PreferencesManager;
import com.threebird.recorder.utils.resources.ResourceUtils;

/**
 * Checks to see if there is an existing Resources folder for this version. If there is not then it copies the Resources
 * from the old version and migrates the data to whatever this version requires. If there is no older version, then it
 * does nothing.
 */
public class CreateResources
{
  public static void apply()
  {
    File oldResources = ResourceUtils.resources1_0();
    File newResources = ResourceUtils.resources1_1();

    // If 1.1 resource folder exists, then we should be good to go.
    if (newResources.exists()) {
      return;
    }

    // If 1.0 resource folder doesn't exist, then there's nothing we can do.
    if (!oldResources.exists()) {
      return;
    }

    // OK so 1.0 exists and 1.1 does not. Copy all files over to 1.1
    try {
      newResources.mkdirs();

      for (File oldFile : oldResources.listFiles()) {
        String newFileName = newResources.getAbsolutePath() + "/" + oldFile.getName();
        File newFile = new File( newFileName );
        Files.copy( oldFile, newFile );
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Rename 1.0 DB file to 1.1 DB file
    if (ResourceUtils.getDb1_0().exists()) {
      File dbFile = new File( newResources.getAbsoluteFile() + "/" + ResourceUtils.getDb1_0().getName() );
      dbFile.renameTo( ResourceUtils.getDb() );
    }

    // Update the Preferences time since we used to store it in seconds.
    PreferencesManager.saveDuration( PreferencesManager.getDuration() * 1000 );
  }
}
