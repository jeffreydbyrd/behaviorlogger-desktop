package com.threebird.recorder.persistence;

public class Preferences
{
  public static void saveSessionDirectory( String directory )
  {}

  public static String getSessionDirectory()
  {
    return null;
  }

  public static void saveDuration( int duration )
  {}

  public static Integer getDuration()
  {
    return 0;
  }

  public static void saveColorOnEnd( boolean colorOnEnd )
  {}

  public static boolean getColorOnEnd()
  {
    return true;
  }

  public static void savePauseOnEnd( boolean pauseOnEnd )
  {}

  public static boolean getPauseOnEnd()
  {
    return false;
  }

  public static void saveSoundOnEnd( boolean soundOnEnd )
  {}

  public static boolean getSoundOnEnd()
  {
    return false;
  }
}
