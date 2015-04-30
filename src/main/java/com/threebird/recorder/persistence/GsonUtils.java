package com.threebird.recorder.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonUtils
{
  private static Gson gson =
      new GsonBuilder().setPrettyPrinting()
                       // .registerTypeAdapter( MappableChar.class,
                       // MappableChar.gsonSerializer )
                       // .registerTypeAdapter( MappableChar.class,
                       // MappableChar.gsonDeserializer )
                       .create();

  public static ExecutorService es = Executors.newSingleThreadExecutor();

  /**
   * Save the model to a JSON file, creating the file if it doesn't already
   * exist.
   */
  public static void save( File file, Object model )
  {
    es.execute( ( ) -> {
      try {
        if (!file.exists()) {
          file.getParentFile().mkdirs();
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

  @SuppressWarnings("unchecked") public static < T > T get( File file, T bean )
  {
    try {
      if (!file.exists()) {
        return bean;
      }

      BufferedReader reader = Files.newReader( file, Charsets.UTF_8 );
      T t = (T) gson.fromJson( reader, bean.getClass() );
      reader.close();
      
      return t;
    } catch (Exception e) {
      throw new RuntimeException( e );
    }
  }
}
