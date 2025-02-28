package jautomata.generator;

import java.util.Map;
import java.util.Set;

import jautomata.extensions.semirings.MultisetSemiring;

public record TestConfiguration(
		Map<String, Integer> configuration,
		boolean nonEmptinessGroundTruth,
		boolean universalityGroundTruth) {

	public Map<String, Integer> expandedConfiguration(Set<String> featureSet) {
		
		// The maximum value is assumed for features not listed in the sample configurations
		return MultisetSemiring.expandMap(configuration, featureSet, Integer.MAX_VALUE);
	}
}
