package com.threebird.recorder.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonUtils
{
  private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

  /**
   * Attempts to parse the JSON contained within 'file' into an instance of T.
   * If 'file' doesn't exist, it creates an empty instance of T
   * 
   * @param file
   *          - the file that contains the JSON data used to populate a model
   * @param clazz
   *          - the type of class Gson should try to create
   * @return a new instance of type T matching the contents of 'file'
   */
  static < T > T createModel( File file, Class< T > clazz )
  {
    try {
      T model;
      if (file.exists()) {
        BufferedReader reader = Files.newReader( file, StandardCharsets.UTF_8 );
        model = new Gson().fromJson( reader, clazz );
        reader.close();
      } else {
        model = clazz.newInstance();
      }
      return model;
    } catch (Exception e) {
      throw new RuntimeException( e );
    }
  }

  public static ExecutorService es = Executors.newSingleThreadExecutor();

  /**
   * Save the model to a JSON file, creating the file if it doesn't already
   * exist.
   */
  static void save( File file, Object model )
  {
    es.execute( ( ) -> {
      try {
        if (!file.exists()) {
          file.createNewFile();
        }

        String json = gson.toJson( model );

        BufferedWriter writer = Files.newWriter( file, StandardCharsets.UTF_8 );
        writer.write( json );
        writer.flush();
        writer.close();
      } catch (Exception e) {
        throw new RuntimeException( e );
      }
    } );
  }

}
