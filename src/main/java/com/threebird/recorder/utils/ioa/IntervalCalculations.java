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
    return "IntervalCalculations [\n"
        + "  c=" + c + ",\n"
        + "  intervals1=" + Arrays.toString( intervals1 ) + ",\n"
        + "  intervals2=" + Arrays.toString( intervals2 ) + ",\n"
        + "  result=" + Arrays.toString( result ) + ",\n"
        + "  avg=" + avg + "\n"
        + "]";
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits( avg );
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((c == null) ? 0 : c.hashCode());
    result = prime * result + Arrays.hashCode( intervals1 );
    result = prime * result + Arrays.hashCode( intervals2 );
    result = prime * result + Arrays.hashCode( this.result );
    return result;
  }

  @Override public boolean equals( Object obj )
  {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    IntervalCalculations other = (IntervalCalculations) obj;
    if (Double.doubleToLongBits( avg ) != Double.doubleToLongBits( other.avg ))
      return false;
    if (c == null) {
      if (other.c != null)
        return false;
    } else if (!c.equals( other.c ))
      return false;
    if (!Arrays.equals( intervals1, other.intervals1 ))
      return false;
    if (!Arrays.equals( intervals2, other.intervals2 ))
      return false;
    if (!Arrays.equals( result, other.result ))
      return false;
    return true;
  }
}