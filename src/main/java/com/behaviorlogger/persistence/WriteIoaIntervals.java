package com.behaviorlogger.persistence;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.behaviorlogger.utils.ioa.IntervalCalculations;
import com.google.common.io.Files;

public class WriteIoaIntervals
{
  public static void write( Map< String, IntervalCalculations > intervals,
                            boolean appendToFile,
                            File f ) throws Exception
  {
    if (!f.exists()) {
      f.createNewFile();
    }

    Workbook wb;
    if (appendToFile) {
      File tmp = File.createTempFile( f.getName(), "" );
      Files.copy( f, tmp );
      wb = WorkbookFactory.create( tmp );
    } else {
      wb = new HSSFWorkbook();
    }

    Sheet s = wb.createSheet();

    Row row;
    int r = 0;

    // __Summary__
    for (Entry< String, IntervalCalculations > entry : intervals.entrySet()) {
      String c = entry.getKey();
      IntervalCalculations calcs = entry.getValue();

      row = s.createRow( r++ );
      row.createCell( 0 ).setCellValue( c.toString() );
      row.createCell( 1 ).setCellValue( calcs.avg );
    }

    // __Interval Breakdown__
    r++; // Skip a row

    Row headers = s.createRow( r++ );
    headers.createCell( 0 ).setCellValue( "" );

    int max =
        intervals.values().stream()
                 .map( calc -> calc.result.length )
                 .collect( Collectors.maxBy( ( l1, l2 ) -> l1 - l2 ) )
                 .orElse( 0 );

    Row[] rows = new Row[max];

    int start = r;
    for (int i = 0; r < start + rows.length; r++, i++) {
      rows[i] = s.createRow( r );
      rows[i].createCell( 0 ).setCellValue( i );
    }

    int col = 1;
    for (Entry< String, IntervalCalculations > e : intervals.entrySet()) {
      String ch = e.getKey();
      IntervalCalculations calcs = e.getValue();
      headers.createCell( col ).setCellValue( ch.toString() );
      for (int i = 0; i < rows.length; i++) {
        if (i < calcs.result.length) {
          rows[i].createCell( col ).setCellValue( calcs.result[i] );
        } else {
          break;
        }
      }
      col++;
    }

    FileOutputStream out = new FileOutputStream( f );
    wb.write( out );
    out.flush();
    wb.close();
    out.close();
  }
}
