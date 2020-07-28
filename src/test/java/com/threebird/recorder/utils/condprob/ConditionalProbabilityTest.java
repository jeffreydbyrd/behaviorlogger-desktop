package com.threebird.recorder.utils.condprob;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.threebird.recorder.models.MappableChar;
import com.threebird.recorder.models.behaviors.BehaviorEvent;
import com.threebird.recorder.models.behaviors.ContinuousBehavior;
import com.threebird.recorder.models.behaviors.DiscreteBehavior;
import com.threebird.recorder.models.schemas.KeyBehaviorMapping;
import com.threebird.recorder.utils.ConditionalProbability;

public class ConditionalProbabilityTest
{
  @Test public void basic_calculation()
  {
    KeyBehaviorMapping target = new KeyBehaviorMapping( "target", "t", "target", false, false );
    KeyBehaviorMapping consequence = new KeyBehaviorMapping( "consequence", "c", "consequence", true, false );
    List< BehaviorEvent > events = Lists.newArrayList();
    events.add( new DiscreteBehavior( "target", MappableChar.T, "target", 0 ) );
    events.add( new ContinuousBehavior( "consequence", MappableChar.C, "consequence", 4000, 6000 ) );
    Map< Integer, Float > results = ConditionalProbability.calculate( events, target, consequence );
    Assert.assertEquals( 1f, results.get( 5 ), 0.00001 );
  }
}
