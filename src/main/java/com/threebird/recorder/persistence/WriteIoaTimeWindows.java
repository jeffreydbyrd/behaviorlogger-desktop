package com.threebird.recorder.persistence;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.google.common.io.Files;
import com.threebird.recorder.utils.ioa.TimeWindowCalculations;

public class WriteIoaTimeWindows
{
  public static void write( Map< Character, TimeWindowCalculations > ioaDiscrete,
                            Map< Character, Double > ioaContinuous,
                            String file1,
                            String file2,
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

    // __Discrete Key Summary__
    row = s.createRow( r++ );
    row.createCell( 0 ).setCellValue( "Discrete IOA Calculations" );

    row = s.createRow( r++ );
    row.createCell( 0 ).setCellValue( "Key" );
    row.createCell( 1 ).setCellValue( file1 );
    row.createCell( 2 ).setCellValue( file1 );

    for (Entry< Character, TimeWindowCalculations > e : ioaDiscrete.entrySet()) {
      Character c = e.getKey();
      TimeWindowCalculations calcs = e.getValue();

      row = s.createRow( r++ );
      row.createCell( 0 ).setCellValue( c.toString() );
      row.createCell( 1 ).setCellValue( calcs.result1 );
      row.createCell( 2 ).setCellValue( calcs.result2 );
    }

    // __Continuous Key Summary__
    r++; // Skip a row
    row = s.createRow( r++ );
    row.createCell( 0 ).setCellValue( "Continuous IOA Calculations" );

    row = s.createRow( r++ );
    row.createCell( 0 ).setCellValue( "Key" );
    row.createCell( 1 ).setCellValue( "IOA Coefficient" );

    for (Entry< Character, Double > e : ioaContinuous.entrySet()) {
      Character c = e.getKey();
      double v = e.getValue();

      row = s.createRow( r++ );
      row.createCell( 0 ).setCellValue( c.toString() );
      row.createCell( 1 ).setCellValue( v );
    }

    FileOutputStream out = new FileOutputStream( f );
    wb.write( out );
    out.flush();
    wb.close();
    out.close();
  }
}
