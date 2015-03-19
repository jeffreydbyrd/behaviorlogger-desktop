package com.threebird.recorder.utils.ioa;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Arrays;

import org.junit.Test;

public class IoaUtilsTest
{
  @Test public void mapKeysToInterval_standard() throws Exception
  {
    URL url = IoaUtilsTest.class.getResource( "test-0.csv" );
    File f = new File( url.toURI() );
    String[] actual = IoaUtils.timesToKeys( f );
    String[] expected = new String[] {
        "cd", "dcd", "", "ddc", "c", ""
    };

    assertTrue( Arrays.equals( actual, expected ) );
  }

  @Test public void mapKeysToInterval_empty() throws Exception
  {}
}
