package jautomata.generator;

import java.util.List;
import java.util.Map;

import net.jhoogland.jautomata.Automaton;

public record ExperimentSettings(
		Automaton<String, Map<String, Integer>> automaton,
		String automatonName,
		int numWarmupRuns,
		int numRealRuns,
		int[] numPathsCombinations,
		int convergenceThreshold, // if 0, there are GroundTruths, else it will be automatically determined
		int convergenceStepSize,
		List<TestConfiguration> testConfigurations,
		boolean lowerBoundednessGroundTruth,
		boolean upperBoundednessGroundTruth) {

	public long totalNumExperiments() {
		return (2 * testConfigurations().size() + 2) * numPathsCombinations().length * (numWarmupRuns() + numRealRuns());
	}
}
