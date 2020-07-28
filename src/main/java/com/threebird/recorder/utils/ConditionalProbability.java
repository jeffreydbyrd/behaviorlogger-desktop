package com.threebird.recorder.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.threebird.recorder.models.behaviors.BehaviorEvent;
import com.threebird.recorder.models.schemas.KeyBehaviorMapping;

public class ConditionalProbability
{

  private static List< Integer > RANGES = Lists.newArrayList( 5, 10, 15, 20 );

  public static Map< Integer, Float >
    calculate( List< BehaviorEvent > events, KeyBehaviorMapping target, KeyBehaviorMapping consequence )
  {
    Iterable< BehaviorEvent > targetEvents =
        Iterables.filter( events, ( event ) -> event.key == target.key );
    Iterable< BehaviorEvent > consequentEvents =
        Iterables.filter( events, ( event ) -> event.key == consequence.key );

    Iterable< Iterable< Integer > > tmp =
        Iterables.transform( targetEvents, ( e ) -> convertToIntervals( e ) );
    List< Integer > targetIntervals = Lists.newArrayList( Iterables.concat( tmp ) );
    tmp = Iterables.transform( consequentEvents, ( event ) -> convertToIntervals( event ) );
    Iterable< Integer > consequentIntervals = Iterables.concat( tmp );

    Map< Integer, Float > results = Maps.newHashMap();
    float numOccurrencesOfConsequenceFollowingTarget;
    for (Integer range : RANGES) {
      numOccurrencesOfConsequenceFollowingTarget = 0f;
      for (Integer targetInterval : targetIntervals) {
        for (Integer consequentInterval : consequentIntervals) {
          int end = (targetInterval) + range;
          if (consequentInterval < end) {
            numOccurrencesOfConsequenceFollowingTarget++;
            break;
          }
        }
      }
      results.put( range, numOccurrencesOfConsequenceFollowingTarget / targetIntervals.size() );
    }
    return results;
  }

  private static Iterable< Integer > convertToIntervals( BehaviorEvent event )
  {
    ArrayList< Integer > result = Lists.newArrayList();
    result.add( event.startTime / 1000 );
    return result;
  }
}
