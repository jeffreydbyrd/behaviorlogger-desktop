package com.threebird.recorder.persistence;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.io.Files;
import com.threebird.recorder.models.behaviors.Behavior;
import com.threebird.recorder.models.schemas.SchemasManager;
import com.threebird.recorder.models.sessions.SessionManager;
import com.threebird.recorder.utils.ioa.IoaUtils;
import com.threebird.recorder.utils.ioa.KeyToTime;

public class Recordings
{
  static class SaveDetails
  {
    public List< Behavior > behaviors;
    public File f;
    public String client;
    public String project;
    public String observer;
    public String therapist;
    public String condition;
    public Integer sessionNumber;
    public Integer totalTimeMillis;
  }

  public static enum Writer
  {
    CSV(Recordings::writeCsv),
    XLS(Recordings::writeXls);

    private final BlockingQueue< SaveDetails > q = new ArrayBlockingQueue< SaveDetails >( 1 );
    private final ExecutorService es = Executors.newSingleThreadExecutor();
    private final Thread taskManager;

    private Writer( Function< SaveDetails, Long > func )
    {
      this.taskManager = new Thread( ( ) -> {
        while (true) {
          try {
            SaveDetails sd = q.take();
            Future< Long > future = es.submit( ( ) -> func.apply( sd ) );
            future.get();
          } catch (Exception e) {
            throw new RuntimeException( e );
          }
        }
      } );

      this.taskManager.setDaemon( true );
    }

    public void schedule( SaveDetails details )
    {
      Preconditions.checkState( !es.isShutdown() );

      if (!this.taskManager.isAlive()) {
        this.taskManager.start();
      }

      q.clear();
      try {
        q.put( details );
      } catch (InterruptedException e) {
        throw new RuntimeException( e );
      }
    }

    public void shutdown()
    {
      es.shutdown();
    }
  }

  private static SaveDetails createSaveDetails( File f, List< Behavior > behaviors, int totalTime )
  {
    SaveDetails sd = new SaveDetails();

    sd.f = f;
    sd.behaviors = behaviors;
    sd.client = SchemasManager.getSelected().client;
    sd.project = SchemasManager.getSelected().project;
    sd.observer = SessionManager.getObserver();
    sd.therapist = SessionManager.getTherapist();
    sd.condition = SessionManager.getCondition();
    sd.sessionNumber = SessionManager.getSessionNumber();
    sd.totalTimeMillis = totalTime;
    return sd;
  }

  public static void saveCsv( File f, List< Behavior > behaviors, int count )
  {
    Writer.CSV.schedule( createSaveDetails( f, behaviors, count ) );
  }

  public static void saveXls( File f, List< Behavior > behaviors, int count )
  {
    Writer.XLS.schedule( createSaveDetails( f, behaviors, count ) );
  }

  /**
   * @return an array where each index represents a second, and each element is
   *         a bar-separated list of keys pressed within each second
   */
  private static String[] mapTimeToKeys( List< Behavior > behaviors, int totalTimeSeconds )
  {
    @SuppressWarnings("unchecked") List< Character >[] res1 = new List[totalTimeSeconds + 1];
    KeyToTime keysToTime = IoaUtils.mapKeysToTime( behaviors, 1000 );

    for (Entry< Character, Multiset< Integer >> e : keysToTime.entrySet()) {
      Character ch = e.getKey();
      Multiset< Integer > millis = e.getValue();
      for (Integer i : millis) {
        int s = i.intValue();
        if (res1[s] == null) {
          res1[s] = Lists.< Character > newArrayList();
        }
        res1[s].add( ch );
      }
    }

    String[] res2 = new String[totalTimeSeconds + 1];
    for (int s = 0; s < totalTimeSeconds + 1; s++) {
      if (res1[s] != null) {
        res2[s] = String.join( "|", Iterables.transform( res1[s], c -> c.toString() ) );
      } else {
        res2[s] = "";
      }
    }
    return res2;
  }

  private static Long writeCsv( SaveDetails details )
  {
    try {
      if (!details.f.exists()) {
        details.f.createNewFile();
      }

      String[] timeToKeys = mapTimeToKeys( details.behaviors, details.totalTimeMillis / 1000 );

      String[] headers = { "Time", "Keys" };
      BufferedWriter out = Files.newWriter( details.f, Charsets.UTF_8 );
      CSVPrinter printer = CSVFormat.DEFAULT.withHeader( headers ).print( out );

      for (int s = 0; s < timeToKeys.length; s++) {
        printer.printRecord( s, timeToKeys[s] );
      }

      printer.flush();
      printer.close();

      return details.f.length();
    } catch (IOException e) {
      throw new RuntimeException( e );
    }
  }

  private static Long writeXls( SaveDetails details )
  {
    try {
      WriteRecordingXls.write( details );
      return details.f.length();
    } catch (IOException e) {
      throw new RuntimeException( e );
    }
  }
}
