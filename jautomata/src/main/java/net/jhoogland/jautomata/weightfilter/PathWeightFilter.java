package net.jhoogland.jautomata.weightfilter;

import java.util.List;
import java.util.stream.Collectors;

import net.jhoogland.jautomata.semirings.PathWeight;

public abstract class PathWeightFilter<K> implements WeightFilter<List<PathWeight<K>>> {

	@Override
	public FilterResult filterState(Object state, List<PathWeight<K>> stateWeight) {
		return stateWeight.stream()
				.noneMatch(pathWeight -> isWeightIncluded(pathWeight))
					? FilterResult.SKIP
					: FilterResult.CONTINUE;
	}

	@Override
	public FilterResult filterTransition(Object state, List<PathWeight<K>> stateWeight, Object transition, Object nextState, List<PathWeight<K>> nextWeight) {
		return nextWeight.stream()
				.noneMatch(pathWeight -> isWeightIncluded(pathWeight))
					? FilterResult.SKIP
					: FilterResult.CONTINUE;
	}

	@Override
	public List<PathWeight<K>> filterWeight(List<PathWeight<K>> weight) {
		return weight.stream()
				.filter(pathWeight -> isWeightIncluded(pathWeight))
				.collect(Collectors.toList());
	}

	protected abstract boolean isWeightIncluded(PathWeight<K> pathWeight);
}
