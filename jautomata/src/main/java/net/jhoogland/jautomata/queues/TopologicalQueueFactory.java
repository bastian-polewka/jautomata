package net.jhoogland.jautomata.queues;

import java.util.*;

import net.jhoogland.jautomata.Automaton;

/**
 * Instances of this class create queues that store states in topological order.
 * The states of the automaton are required to have a topological order, which
 * must be returned by the <code>topologicalOrder</code> method of the
 * automaton. Computation of the shortest distance runs in linear time with
 * respect to the number of states.
 *
 * @author Jasper Hoogland
 *
 * @param <K> weight type
 */
public class TopologicalQueueFactory<K> implements QueueFactory<K> {

	@Override
	public <L> Queue<Object> createQueue(Automaton<L, K> automaton, Map<Object, K> weightMap) {
		Comparator<Object> topologicalOrder = automaton.topologicalOrder();
		if (topologicalOrder == null) {
			throw new IllegalArgumentException("Automaton.topologicalOrder must be defined for TopologicalQueueFactory");
		}
		return new PriorityQueue<>(32, topologicalOrder);
	}
}
