package com.behaviorlogger.persistence.recordings;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.DateTime;

import com.behaviorlogger.models.MappableChar;
import com.behaviorlogger.models.behaviors.BehaviorEvent;
import com.behaviorlogger.models.behaviors.ContinuousBehavior;
import com.behaviorlogger.models.behaviors.DiscreteBehavior;
import com.behaviorlogger.persistence.recordings.Recordings.SaveDetails;
import com.behaviorlogger.utils.BehaviorLoggerUtil;

public class WriteRecordingXls
{
  public static void write( SaveDetails details ) throws IOException
  {
    if (!details.f.exists()) {
      details.f.getParentFile().mkdirs();
      details.f.createNewFile();
    }

    Workbook wb = new HSSFWorkbook();
    Sheet s = wb.createSheet( "Summary" );

    Row r;
    int rownum = 0;

    // __Session Summary__
    r = s.createRow( rownum++ );
    r.createCell( 0 ).setCellValue( "Session Summary" );

    // Client
    r = s.createRow( rownum++ );
    r.createCell( 0 ).setCellValue( "Client" );
    r.createCell( 1 ).setCellValue( details.schema.client );

    // Project
    r = s.createRow( rownum++ );
    r.createCell( 0 ).setCellValue( "Project" );
    r.createCell( 1 ).setCellValue( details.schema.project );

    // Observer
    r = s.createRow( rownum++ );
    r.createCell( 0 ).setCellValue( "Observer" );
    r.createCell( 1 ).setCellValue( details.observer );

    // Therapist
    r = s.createRow( rownum++ );
    r.createCell( 0 ).setCellValue( "Therapist" );
    r.createCell( 1 ).setCellValue( details.therapist );

    // Condition
    r = s.createRow( rownum++ );
    r.createCell( 0 ).setCellValue( "Condition" );
    r.createCell( 1 ).setCellValue( details.condition );

    r = s.createRow( rownum++ );
    r.createCell( 0 ).setCellValue( "Location" );
    r.createCell( 1 ).setCellValue( details.location );

    // Session Number
    r = s.createRow( rownum++ );
    r.createCell( 0 ).setCellValue( "Session Number" );
    r.createCell( 1 ).setCellValue( details.sessionNumber );

    // Start-Time
    r = s.createRow( rownum++ );
    r.createCell( 0 ).setCellValue( "Start Time" );
    r.createCell( 1 ).setCellValue( new DateTime( details.startTime ).toString( "yyyy-MM-dd HH:mm:ss" ) );

    // End-Time
    r = s.createRow( rownum++ );
    r.createCell( 0 ).setCellValue( "Stop Time" );
    r.createCell( 1 ).setCellValue( new DateTime( details.stopTime ).toString( "yyyy-MM-dd HH:mm:ss" ) );

    // Session Time
    r = s.createRow( rownum++ );
    r.createCell( 0 ).setCellValue( "Session Time" );
    r.createCell( 1 ).setCellValue( BehaviorLoggerUtil.millisToTimestamp( details.totalTimeMillis ) );

    rownum++; // Skip a row

    // __Behavior Summary__
    r = s.createRow( rownum++ );
    r.createCell( 0 ).setCellValue( "Behavior Summary" );

    // Discrete Behaviors:
    // Headers:
    r = s.createRow( rownum++ );
    r.createCell( 0 ).setCellValue( "Key" );
    r.createCell( 1 ).setCellValue( "Behavior" );
    r.createCell( 2 ).setCellValue( "Count" );
    r.createCell( 3 ).setCellValue( "Responses / Min" );

    // Data:
    Map< MappableChar, List< DiscreteBehavior > > discretes =
        details.behaviors.stream().filter( b -> !b.isContinuous() )
                         .map( b -> (DiscreteBehavior) b )
                         .collect( Collectors.groupingBy( db -> db.key ) );

    r = s.createRow( rownum++ );
    for (Entry< MappableChar, List< DiscreteBehavior > > e : discretes.entrySet()) {
      MappableChar ch = e.getKey();
      List< DiscreteBehavior > dbs = e.getValue();

      r.createCell( 0 ).setCellValue( ch.c + "" );
      r.createCell( 1 ).setCellValue( dbs.get( 0 ).name );
      r.createCell( 2 ).setCellValue( dbs.size() );

      double mins = BehaviorLoggerUtil.millisToMinutes( details.totalTimeMillis );
      r.createCell( 3 ).setCellValue( ((double) dbs.size()) / mins );

      r = s.createRow( rownum++ );
    }

    // Continuous Behaviors
    // Headers:
    rownum++; // skip a row
    r = s.createRow( rownum++ );
    r.createCell( 0 ).setCellValue( "Key" );
    r.createCell( 1 ).setCellValue( "Behavior" );
    r.createCell( 2 ).setCellValue( "Total Time" );
    r.createCell( 3 ).setCellValue( "% Session Time" );

    // Data:
    Map< MappableChar, List< ContinuousBehavior > > continuous =
        details.behaviors.stream().filter( b -> b.isContinuous() )
                         .map( b -> (ContinuousBehavior) b )
                         .collect( Collectors.groupingBy( db -> db.key ) );

    r = s.createRow( rownum++ );
    for (Entry< MappableChar, List< ContinuousBehavior > > e : continuous.entrySet()) {
      MappableChar ch = e.getKey();
      List< ContinuousBehavior > cbs = e.getValue();

      r.createCell( 0 ).setCellValue( ch.c + "" );
      r.createCell( 1 ).setCellValue( cbs.get( 0 ).name );

      double sum = 0.0;
      for (ContinuousBehavior cb : cbs) {
        sum += cb.getDuration();
      }
      r.createCell( 2 ).setCellValue( sum / 1000.0 + "s" );

      double percent = sum / details.totalTimeMillis;
      r.createCell( 3 ).setCellValue( percent );

      r = s.createRow( rownum++ );
    }

    // __Behavior Log and Breakdown__
    s = wb.createSheet( "Breakdown" );
    rownum = 0;

    // Headers:
    r = s.createRow( rownum++ );
    r.createCell( 0 ).setCellValue( "Key" );
    r.createCell( 1 ).setCellValue( "Timestamp" );
    r.createCell( 2 ).setCellValue( "Behavior" );

    for (BehaviorEvent b : details.behaviors) {
      r = s.createRow( rownum++ );
      r.createCell( 0 ).setCellValue( b.key.c + "" );
      r.createCell( 1 ).setCellValue( b.timeDisplay() );
      r.createCell( 2 ).setCellValue( b.name );
    }

    // __Notes__
    s = wb.createSheet( "Session Notes" );
    rownum = 0;

    r = s.createRow( rownum );
    r.createCell( 0 ).setCellValue( details.notes );

    FileOutputStream out = new FileOutputStream( details.f );
    wb.write( out );
    wb.close();
    out.close();
  }
}
