package com.threebird.recorder.utils.ioa;

public class TimeWindowCalculations
{
  public final double result1;
  public final double result2;

  public TimeWindowCalculations( double result1, double result2 )
  {
    this.result1 = result1;
    this.result2 = result2;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits( result1 );
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits( result2 );
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    TimeWindowCalculations other = (TimeWindowCalculations) obj;
    if (Double.doubleToLongBits( result1 ) != Double.doubleToLongBits( other.result1 ))
      return false;
    if (Double.doubleToLongBits( result2 ) != Double.doubleToLongBits( other.result2 ))
      return false;
    return true;
  }
}
