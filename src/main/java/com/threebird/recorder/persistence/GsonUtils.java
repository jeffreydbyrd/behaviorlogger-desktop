package com.threebird.recorder.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.joda.time.DateTime;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.threebird.recorder.models.MappableChar;

public class GsonUtils
{
  private static class DateTimeSerializer implements JsonSerializer< DateTime >
  {
    public JsonElement serialize( DateTime src, Type typeOfSrc, JsonSerializationContext context )
    {
      return new JsonPrimitive( src.toString() );
    }
  }

  private static class DateTimeDeserializer implements JsonDeserializer< DateTime >
  {
    public DateTime deserialize( JsonElement json, Type typeOfT, JsonDeserializationContext context )
        throws JsonParseException
    {
      return new DateTime( json.getAsJsonPrimitive().getAsString() );
    }
  }

  private static Gson gson =
      new GsonBuilder().registerTypeAdapter( DateTime.class, new DateTimeSerializer() )
                       .registerTypeAdapter( DateTime.class, new DateTimeDeserializer() )
                       .registerTypeAdapter( MappableChar.class, MappableChar.gsonDeserializer )
                       .registerTypeAdapter( MappableChar.class, MappableChar.gsonSerializer )
                       .create();

  public static ExecutorService es = Executors.newSingleThreadExecutor();

  /**
   * Save the model to a JSON file, creating the file if it doesn't already exist.
   * 
   * @throws Exception
   */
  public static void save( File file, Object model ) throws Exception
  {
    es.submit( () -> {
      if (!file.exists()) {
        file.getParentFile().mkdirs();
        file.createNewFile();
      }

      String json = gson.toJson( model );

      BufferedWriter writer = Files.newWriter( file, StandardCharsets.UTF_8 );
      writer.write( json );
      writer.flush();
      writer.close();
      return null;
    } ).get();
  }

  @SuppressWarnings("unchecked") public static < T > T get( File file, T bean ) throws IOException
  {
    if (!file.exists()) {
      return bean;
    }

    BufferedReader reader = Files.newReader( file, Charsets.UTF_8 );
    T t = (T) gson.fromJson( reader, bean.getClass() );
    reader.close();

    return t;
  }
}
