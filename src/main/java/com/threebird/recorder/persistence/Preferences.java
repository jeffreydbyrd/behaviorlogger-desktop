package com.threebird.recorder.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Preferences
{

  private static class Model
  {
    String directory = System.getProperty( "user.home" );;
    int duration = 600;
    boolean colorOnEnd = true;
    boolean pauseOnEnd = false;
    boolean soundOnEnd = false;
  }

  private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
  private static Model model;
  private static File file = new File( "./resources/prefs.json" );

  /*
   * Setup the model: If json file doesn't exist, create it and we'll run with
   * the default model. If json file does exists, populate model with contents
   */
  static {
    try {
      if (file.exists()) {
        BufferedReader reader = Files.newReader( file, StandardCharsets.UTF_8 );
        model = gson.fromJson( reader, Model.class );
        reader.close();
      } else {
        model = new Model();
      }
    } catch (Exception e) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Save the model to a JSON file, creating the file if it doesn't already
   * exist.
   */
  private static void save()
  {
    if (!file.exists()) {
      try {
        file.createNewFile();
      } catch (IOException e) {
        throw new RuntimeException( e );
      }
    }

    String json = gson.toJson( model );

    try (BufferedWriter writer = Files.newWriter( file, StandardCharsets.UTF_8 )) {
      writer.write( json );
    } catch (Exception e) {
      throw new RuntimeException( e );
    }
  }

  public static void saveSessionDirectory( String directory )
  {
    model.directory = directory;
    save();
  }

  public static String getSessionDirectory()
  {
    return model.directory;
  }

  public static void saveDuration( int duration )
  {
    model.duration = duration;
    save();
  }

  public static Integer getDuration()
  {
    return model.duration;
  }

  public static void saveColorOnEnd( boolean colorOnEnd )
  {
    model.colorOnEnd = colorOnEnd;
    save();
  }

  public static boolean getColorOnEnd()
  {
    return model.colorOnEnd;
  }

  public static void savePauseOnEnd( boolean pauseOnEnd )
  {
    model.pauseOnEnd = pauseOnEnd;
    save();
  }

  public static boolean getPauseOnEnd()
  {
    return model.pauseOnEnd;
  }

  public static void saveSoundOnEnd( boolean soundOnEnd )
  {
    model.soundOnEnd = soundOnEnd;
    save();
  }

  public static boolean getSoundOnEnd()
  {
    return model.soundOnEnd;
  }
}
