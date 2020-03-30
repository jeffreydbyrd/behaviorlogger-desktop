package com.threebird.recorder.persistence;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.google.common.collect.Multiset;
import com.google.common.io.Files;
import com.threebird.recorder.utils.ioa.KeyToInterval;

public class WriteBinIntervals
{
  public static void write( KeyToInterval keyToInterval,
                            boolean appendToFile,
                            File f )
      throws Exception
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

    // fill out headers
    Row headersRow = s.createRow( 0 );
    int c = 0;
    headersRow.createCell( c++ ).setCellValue( "Interval" );
    headersRow.createCell( c++ ).setCellValue( "Time Range (s)" );
    for (String key : keyToInterval.keyToIntervals.keySet()) {
      headersRow.createCell( c++ ).setCellValue( key );
    }

    // zero out the body
    Row[] mainRows = new Row[keyToInterval.totalIntervals];
    for (int r = 0; r < mainRows.length; r++) {
      Row row = s.createRow( r + 1 );
      mainRows[r] = row;
      c = 0;
      row.createCell( c++ ).setCellValue( r );
      row.createCell( c++ ).setCellValue( (r * keyToInterval.blockSizeSeconds) + " - "
          + (((r + 1) * keyToInterval.blockSizeSeconds) - 1) );
      for (; c < keyToInterval.keyToIntervals.size() + 2; c++) {
        row.createCell( c ).setCellValue( 0 );
      }
    }
    
    // fill in the counts
    c = 2;
    for (Entry< String, Multiset< Integer > > entry : keyToInterval.keyToIntervals.entrySet()) {
      Multiset< Integer > intervals = entry.getValue();
      for (Integer interval : intervals) {
        int count = intervals.count( interval );
        mainRows[interval].getCell( c ).setCellValue( count );
      }
      c++;
    }

    FileOutputStream out = new FileOutputStream( f );
    wb.write( out );
    out.flush();
    wb.close();
    out.close();
  }
}
