package net.jhoogland.jautomata.weightfilter;

/**
 * Used to influence the execution of the single-source shortest distance algorithm.
 *
 * @param <K> weight type
 */
public interface WeightFilter<K> {

	enum FilterResult {
		CONTINUE,
		SKIP,
		STOP,
	}

	FilterResult filterState(Object state, K stateWeight);
	FilterResult filterTransition(Object state, K stateWeight, Object transition, Object nextState, K nextWeight);
	K filterWeight(K weight);
}
