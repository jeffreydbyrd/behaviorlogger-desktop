package com.threebird.recorder.persistence;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.StandardCharsets;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.io.Files;
import com.google.gson.Gson;

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
}
