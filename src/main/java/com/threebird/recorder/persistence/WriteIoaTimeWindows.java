package com.threebird.recorder.persistence;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class WriteIoaTimeWindows
{
  public static void write( Map< Character, Double > results, File f ) throws IOException
  {
    if (!f.exists()) {
      f.createNewFile();
    }

    FileOutputStream out = new FileOutputStream( f );

    Workbook wb = new HSSFWorkbook();
    Sheet s = wb.createSheet();

    Row row;
    int r = 0;

    Double sum = results.values().stream().reduce( 0.0, ( d1, acc ) -> d1 + acc );
    Double overall = sum / results.size();

    // __Key Summary__
    row = s.createRow( r++ );
    row.createCell( 0 ).setCellValue( "Overall" );
    row.createCell( 1 ).setCellValue( overall.doubleValue() );

    for (Entry< Character, Double > entry : results.entrySet()) {
      Character c = entry.getKey();
      Double avg = entry.getValue();

      row = s.createRow( r++ );
      row.createCell( 0 ).setCellValue( c.toString() );
      row.createCell( 1 ).setCellValue( avg );
    }

    wb.write( out );
    out.flush();
    wb.close();
    out.close();
  }
}
