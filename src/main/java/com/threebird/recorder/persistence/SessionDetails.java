package com.threebird.recorder.persistence;

import java.io.File;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public class SessionDetails
{
  private static class Model
  {
    String observer;
    String therapist;
    String condition;
    int sessionNumber;
  }

  private static File file = new File( "./resources/session-details.json" );

  /*
   * Lazily load the Model. If 'file' doesn't exist, run with an empty default
   * Model. If it does exist, populate model with content
   */
  private static Supplier< Model > model = Suppliers.memoize( ( ) -> GsonUtils.createModel( file, Model.class ) );

  private static void save()
  {
    GsonUtils.save( file, model.get() );
  }

  public static void saveObserver( String observer )
  {
    model.get().observer = observer;
    save();
  }

  public static String getObserver()
  {
    return model.get().observer;
  }

  public static void saveTherapist( String therapist )
  {
    model.get().therapist = therapist;
    save();
  }

  public static String getTherapist()
  {
    return model.get().therapist;
  }

  public static void saveCondition( String condition )
  {
    model.get().condition = condition;
    save();
  }

  public static String getCondition()
  {
    return model.get().condition;
  }

  public static void saveSessionNumber( int sessionNumber )
  {
    model.get().sessionNumber = sessionNumber;
    save();
  }

  public static int getSessionNumber()
  {
    return model.get().sessionNumber;
  }

}
