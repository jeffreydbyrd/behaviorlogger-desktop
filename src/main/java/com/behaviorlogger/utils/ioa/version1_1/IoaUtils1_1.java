package com.behaviorlogger.utils.ioa.version1_1;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.behaviorlogger.models.schemas.KeyBehaviorMapping;
import com.behaviorlogger.persistence.GsonUtils;
import com.behaviorlogger.persistence.WriteIoaIntervals;
import com.behaviorlogger.persistence.WriteIoaTimeWindows;
import com.behaviorlogger.persistence.recordings.RecordingRawJson1_1.ContinuousEvent;
import com.behaviorlogger.persistence.recordings.RecordingRawJson1_1.DiscreteEvent;
import com.behaviorlogger.persistence.recordings.RecordingRawJson1_1.SessionBean1_1;
import com.behaviorlogger.utils.ioa.IntervalCalculations;
import com.behaviorlogger.utils.ioa.IoaMethod;
import com.behaviorlogger.utils.ioa.KeyToInterval;
import com.behaviorlogger.utils.ioa.TimeWindowCalculations;
import com.behaviorlogger.views.ioa.IoaTimeBlockSummary;
import com.behaviorlogger.views.ioa.IoaTimeWindowSummary;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;

import javafx.scene.layout.VBox;

public class IoaUtils1_1 {
    public static KeyToInterval partition(HashMap<String, ArrayList<Integer>> idToSeconds, long totalTimeMilles,
	    int blockSizeInSeconds) {
	HashMap<String, Multiset<Integer>> idToIntervals = Maps.newHashMap();

	idToSeconds.forEach((buuid, ints) -> {
	    HashMultiset<Integer> times = HashMultiset.create();
	    idToIntervals.put(buuid, times);
	    ints.forEach(t -> {
		times.add(t / blockSizeInSeconds);
	    });
	});

	int numIntervals = (int) Math.ceil((totalTimeMilles / 1000.0) / blockSizeInSeconds);

	return new KeyToInterval(idToIntervals, numIntervals, blockSizeInSeconds);
    }

    public static VBox processTimeBlock(IoaMethod method, int blockSizeInSeconds, boolean appendToFile, File out,
	    SessionBean1_1 stream1, SessionBean1_1 stream2) throws Exception {
	blockSizeInSeconds = blockSizeInSeconds < 1 ? 1 : blockSizeInSeconds;
	HashMap<String, ArrayList<Integer>> stream1IdToSeconds = mapIdToSeconds(stream1);
	HashMap<String, ArrayList<Integer>> stream2IdToSeconds = mapIdToSeconds(stream2);

	KeyToInterval data1 = partition(stream1IdToSeconds, stream1.duration, blockSizeInSeconds);
	KeyToInterval data2 = partition(stream2IdToSeconds, stream2.duration, blockSizeInSeconds);

	Map<String, IntervalCalculations> intervals = method == IoaMethod.Exact_Agreement
		? IoaCalculations.exactAgreement(data1, data2)
		: IoaCalculations.partialAgreement(data1, data2);

	WriteIoaIntervals.write(intervals, appendToFile, out);
	return new IoaTimeBlockSummary(intervals);
    }

    public static HashMap<String, ArrayList<Integer>> mapIdToSeconds(SessionBean1_1 bean) {
	HashMap<String, ArrayList<Integer>> result = Maps.newHashMap();
	populateDiscrete(bean, result);
	populateContinuous(bean, result);
	return result;
    }

    /**
     * Mutates the map
     */
    public static void populateContinuous(SessionBean1_1 stream1, HashMap<String, ArrayList<Integer>> idToSeconds) {
	ImmutableMap<String, KeyBehaviorMapping> behaviors = Maps.uniqueIndex(stream1.schema.behaviors, b -> b.uuid);

	for (ContinuousEvent ce : stream1.continuousEvents) {
	    String buuid = ce.behaviorUuid;
	    String key = behaviors.get(buuid).key.toString();

	    if (!idToSeconds.containsKey(key)) {
		idToSeconds.put(key, Lists.newArrayList());
	    }

	    long start = ce.startTime / 1000;
	    long end = ce.endTime / 1000;
	    for (long t = start; t <= end; t += 1) {
		idToSeconds.get(key).add((int) t);
	    }
	}
    }

    /**
     * Mutates the map
     */
    public static void populateDiscrete(SessionBean1_1 stream1, HashMap<String, ArrayList<Integer>> idToSeconds) {
	ImmutableMap<String, KeyBehaviorMapping> behaviors = Maps.uniqueIndex(stream1.schema.behaviors, b -> b.uuid);

	for (DiscreteEvent de : stream1.discreteEvents) {
	    String buuid = de.behaviorUuid;
	    String key = behaviors.get(buuid).key.toString();

	    if (!idToSeconds.containsKey(key)) {
		idToSeconds.put(key, Lists.newArrayList());
	    }

	    idToSeconds.get(key).add((int) (de.time / 1000));
	}
    }

    public static VBox processTimeWindow(String file1, String file2, boolean appendToFile, File out, int thresholdSeconds,
	    SessionBean1_1 stream1, SessionBean1_1 stream2) throws Exception {
	HashMap<String, ArrayList<Integer>> discreteIdToSeconds1 = Maps.newHashMap();
	HashMap<String, ArrayList<Integer>> discreteIdToSeconds2 = Maps.newHashMap();
	populateDiscrete(stream1, discreteIdToSeconds1);
	populateDiscrete(stream2, discreteIdToSeconds2);
	KeyToInterval discrete1 = partition(discreteIdToSeconds1, stream1.duration, 1);
	KeyToInterval discrete2 = partition(discreteIdToSeconds2, stream2.duration, 1);

	HashMap<String, ArrayList<Integer>> continuous1 = Maps.newHashMap();
	HashMap<String, ArrayList<Integer>> continuous2 = Maps.newHashMap();
	populateContinuous(stream1, continuous1);
	populateContinuous(stream2, continuous2);
	KeyToInterval cont1 = partition(continuous1, stream1.duration, 1);
	KeyToInterval cont2 = partition(continuous2, stream2.duration, 1);

	Map<String, TimeWindowCalculations> ioaDiscrete = IoaCalculations.windowAgreementDiscrete(discrete1, discrete2,
		thresholdSeconds);
	Map<String, Double> ioaContinuous = IoaCalculations.windowAgreementContinuous(cont1, cont2);

	WriteIoaTimeWindows.write(ioaDiscrete, ioaContinuous, file1, file2, appendToFile, out);

	return new IoaTimeWindowSummary(ioaDiscrete, ioaContinuous);
    }

    /**
     * Calculates IOA and writes the output to 'out'
     * 
     * @param f1        the first raw input file
     * @param f2        the second raw input file
     * @param method    the {@link IoaMethod} used
     * @param blockSize the blocksize of intervals used
     * @param out       the output file
     * @return a JavaFX pane giving a summary of the output file
     * @throws IOException
     */
    public static VBox process(File f1, File f2, IoaMethod method, int blockSize, boolean appendToFile, File out)
	    throws Exception {
	SessionBean1_1 stream1 = GsonUtils.get(f1, new SessionBean1_1());
	SessionBean1_1 stream2 = GsonUtils.get(f2, new SessionBean1_1());

	if (method != IoaMethod.Time_Window) {
	    return processTimeBlock(method, blockSize, appendToFile, out, stream1, stream2);
	} else {
	    return processTimeWindow(f1.getName(), f2.getName(), appendToFile, out, blockSize, stream1, stream2);
	}
    }
}
