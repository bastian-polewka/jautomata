package net.jhoogland.jautomata.queues;

import java.util.Map;
import java.util.Queue;

import net.jhoogland.jautomata.Automaton;

/**
 * This queue factory creates a topological state queue for automata that
 * provide a topological order and a shortest-first queue for all other
 * automata.
 *
 * @author Jasper Hoogland
 *
 * @param <K> weight type
 */
public class DefaultQueueFactory<K> implements QueueFactory<K> {

	@Override
	public <L> Queue<Object> createQueue(Automaton<L, K> automaton, Map<Object, K> weightMap) {
		QueueFactory<K> delegate;
		if (automaton.topologicalOrder() == null) {
			delegate = new ShortestFirstQueueFactory<>();
		} else {
			delegate = new TopologicalQueueFactory<>();
		}
		return delegate.createQueue(automaton, weightMap);
	}
}
