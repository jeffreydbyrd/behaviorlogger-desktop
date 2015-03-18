package com.threebird.recorder.persistence;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.threebird.recorder.utils.ioa.IntervalCalculations;

public class WriteIoaIntervals
{
  public static void write( Map< Character, IntervalCalculations > intervals, File f ) throws IOException
  {
    if (!f.exists()) {
      f.createNewFile();
    }

    FileOutputStream out = new FileOutputStream( f );

    Workbook wb = new HSSFWorkbook();
    Sheet s = wb.createSheet();

    Row row;
    int r = 0;

    Double total = intervals.values().stream().collect( Collectors.averagingDouble( calcs -> calcs.avg ) );

    // __Behavior Summary__
    row = s.createRow( r++ );
    Cell totalCell = row.createCell( 0 );
    totalCell.setCellValue( "Overall" );
    row.createCell( 1 ).setCellValue( total.doubleValue() );

    for (Entry< Character, IntervalCalculations > entry : intervals.entrySet()) {
      Character c = entry.getKey();
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
    for (Entry< Character, IntervalCalculations > e : intervals.entrySet()) {
      Character ch = e.getKey();
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

    wb.write( out );
    out.flush();
    wb.close();
    out.close();
  }
}
