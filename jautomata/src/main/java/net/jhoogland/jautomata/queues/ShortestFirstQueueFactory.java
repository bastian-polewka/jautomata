package net.jhoogland.jautomata.queues;

import java.util.*;

import net.jhoogland.jautomata.Automaton;

public class ShortestFirstQueueFactory<K> implements QueueFactory<K> {

	@Override
	public <L> Queue<Object> createQueue(Automaton<L, K> automaton, Map<Object, K> weightMap) {
		return new PriorityQueue<>(11, (o1, o2) -> {
			K w1 = weightMap.get(o1);
			K w2 = weightMap.get(o2);
			if (w1 == null && w2 == null) {
				return 0;
			} else if (w1 == null) {
				return 1;
			} else if (w2 == null) {
				return -1;
			} else {
				return automaton.semiring().getComparator().compare(w1, w2);
			}
		});
	}
}
