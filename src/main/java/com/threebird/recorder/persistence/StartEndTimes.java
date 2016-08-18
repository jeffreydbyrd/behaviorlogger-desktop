package com.threebird.recorder.persistence;

public class StartEndTimes
{
  public int start;
  public int end;

  public StartEndTimes( int start, int end )
  {
    this.start = start;
    this.end = end;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + end;
    result = prime * result + start;
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
    StartEndTimes other = (StartEndTimes) obj;
    if (end != other.end)
      return false;
    if (start != other.start)
      return false;
    return true;
  }
}