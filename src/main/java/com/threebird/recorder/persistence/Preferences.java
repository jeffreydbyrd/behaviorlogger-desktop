package com.threebird.recorder.persistence;

import java.io.File;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

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

  private static File file = new File( "./resources/prefs.json" );
  private static Supplier< Model > model = Suppliers.memoize( ( ) -> GsonUtils.createModel( file, Model.class ) );

  private static void save()
  {
    GsonUtils.save( file, model.get() );
  }

  public static void saveSessionDirectory( String directory )
  {
    model.get().directory = directory;
    save();
  }

  public static String getSessionDirectory()
  {
    return model.get().directory;
  }

  public static void saveDuration( int duration )
  {
    model.get().duration = duration;
    save();
  }

  public static Integer getDuration()
  {
    return model.get().duration;
  }

  public static void saveColorOnEnd( boolean colorOnEnd )
  {
    model.get().colorOnEnd = colorOnEnd;
    save();
  }

  public static boolean getColorOnEnd()
  {
    return model.get().colorOnEnd;
  }

  public static void savePauseOnEnd( boolean pauseOnEnd )
  {
    model.get().pauseOnEnd = pauseOnEnd;
    save();
  }

  public static boolean getPauseOnEnd()
  {
    return model.get().pauseOnEnd;
  }

  public static void saveSoundOnEnd( boolean soundOnEnd )
  {
    model.get().soundOnEnd = soundOnEnd;
    save();
  }

  public static boolean getSoundOnEnd()
  {
    return model.get().soundOnEnd;
  }
}
