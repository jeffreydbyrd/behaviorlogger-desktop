package com.threebird.recorder.utils.ioa;

import java.util.Arrays;

public class IntervalCalculations
{
  public final Character c;
  public final int[] intervals1;
  public final int[] intervals2;
  public final double[] result;
  public final double avg;

  public IntervalCalculations( Character c, int[] intervals1, int[] intervals2, double[] result )
  {
    this.c = c;
    this.intervals1 = intervals1;
    this.intervals2 = intervals2;
    this.result = result;
    this.avg = Arrays.stream( result ).average().orElse( 0 );
  }

  @Override public String toString()
  {
    return "IntervalCalculations [\n c=" + c + ",\n intervals1=" + Arrays.toString( intervals1 ) + ",\n intervals2="
        + Arrays.toString( intervals2 ) + ",\n result=" + Arrays.toString( result ) + ",\n avg=" + avg + "\n]";
  }
}