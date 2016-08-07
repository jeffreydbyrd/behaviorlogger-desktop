package com.threebird.recorder.utils.ioa;

import java.util.Arrays;

public class IntervalCalculations
{
  public final String buuid;
  public final int[] intervals1;
  public final int[] intervals2;
  public final double[] result;
  public final double avg;

  public IntervalCalculations( String buuid, int[] intervals1, int[] intervals2, double[] result )
  {
    this.buuid = buuid;
    this.intervals1 = intervals1;
    this.intervals2 = intervals2;
    this.result = result;
    this.avg = Arrays.stream( result ).average().orElse( 0 );
  }

  @Override public String toString()
  {
    return "IntervalCalculations [\n"
        + "  buuid=" + buuid + ",\n"
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
    result = prime * result + ((buuid == null) ? 0 : buuid.hashCode());
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
    if (buuid == null) {
      if (other.buuid != null)
        return false;
    } else if (!buuid.equals( other.buuid ))
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