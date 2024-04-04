package net.jhoogland.jautomata.queues;

import java.util.*;

import net.jhoogland.jautomata.Automata;
import net.jhoogland.jautomata.Automaton;
import net.jhoogland.jautomata.semirings.KTropicalSemiring;
import net.jhoogland.jautomata.semirings.PathWeight;

/**
 * Implementation of a {@link QueueFactory} that creates queues for the n
 * shortest paths algorithm.
 *
 * @author Jasper Hoogland
 */
public class KTropicalQueueFactory<K> implements QueueFactory<List<PathWeight<K>>> {

	@Override
	public <L> Queue<Object> createQueue(Automaton<L, List<PathWeight<K>>> automaton, Map<Object, List<PathWeight<K>>> weightMap) {
		if (!(automaton.semiring() instanceof KTropicalSemiring)) {
			throw new AssertionError("KTropicalQueueFactory must be used with KTropicalSemiring");
		}

		Map<Object, Integer> numExtractions = new HashMap<>();
		KTropicalSemiring<K> semiring = (KTropicalSemiring<K>) automaton.semiring();
		Comparator<Object> comparator = Comparator.comparing(state -> {
			List<PathWeight<K>> weight = weightMap.get(state);
			Integer num = numExtractions.getOrDefault(state, 0);
			int k = num < semiring.getK() ? num : semiring.getK() - 1;
			k = k < weight.size() ? k : weight.size() - 1;
			return weight.get(k).weight;
		}, semiring.getSrc().getComparator());

		return new PriorityQueue<Object>(32, comparator) {
			private static final long serialVersionUID = -994774619331389273L;

			@Override
			public Object poll() {
				Object s = super.poll();
				if (s != null) {
					Integer n = numExtractions.getOrDefault(s, 0);
					n++;
					numExtractions.put(s, n);
					if (semiring.getK() == n && Automata.isFinalState(automaton, s)) {
						clear();
					}
				}
				return s;
			}
		};
	}
}
