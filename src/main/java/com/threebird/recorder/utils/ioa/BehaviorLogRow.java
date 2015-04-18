package com.threebird.recorder.utils.ioa;

class BehaviorLogRow
{
  public final String discrete;
  public final String continuous;

  public BehaviorLogRow( String discrete, String continuous )
  {
    this.discrete = discrete;
    this.continuous = continuous;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((continuous == null) ? 0 : continuous.hashCode());
    result = prime * result + ((discrete == null) ? 0 : discrete.hashCode());
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
    BehaviorLogRow other = (BehaviorLogRow) obj;
    if (continuous == null) {
      if (other.continuous != null)
        return false;
    } else if (!continuous.equals( other.continuous ))
      return false;
    if (discrete == null) {
      if (other.discrete != null)
        return false;
    } else if (!discrete.equals( other.discrete ))
      return false;
    return true;
  }
}