package net.jhoogland.jautomata.weightfilter;

public class DefaultWeightFilter<K> implements WeightFilter<K> {

	@Override
	public FilterResult filterState(Object state, K stateWeight) {
		return FilterResult.CONTINUE;
	}

	@Override
	public FilterResult filterTransition(Object state, K stateWeight, Object transition, Object nextState, K nextWeight) {
		return FilterResult.CONTINUE;
	}

	@Override
	public K filterWeight(K weight) {
		return weight;
	}
}
