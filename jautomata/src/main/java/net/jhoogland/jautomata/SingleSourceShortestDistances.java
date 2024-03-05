package net.jhoogland.jautomata;

import java.util.*;

import net.jhoogland.jautomata.queues.QueueFactory;
import net.jhoogland.jautomata.semirings.Semiring;

/**
 * <p>
 * This class implements the generic single-source shortest distance
 * algorithm described by [1].
 * </p>
 * <p>
 * [1] M. Mohri. General algebraic frameworks and algorithms for shortest distance
 *     problems. 1998
 * </p>
 *
 * @author Jasper Hoogland
 *
 * @param <K>
 * weight type
 * (Boolean for regular automata and Double for weighted automata)
 */

public class SingleSourceShortestDistances<K> implements SingleSourceShortestDistancesInterface<K>
{
	public QueueFactory<K> queueFactory;
	public WeightConvergenceCondition<K> equalityDef;

	public SingleSourceShortestDistances(QueueFactory<K> queueFactory, WeightConvergenceCondition<K> equalityDef)
	{
		this.queueFactory = queueFactory;
		this.equalityDef = equalityDef;
	}

	@Override
	public <L> Map<Object, K> computeShortestDistances(Automaton<L, K> automaton, Object source)
	{
		Map<Object, K> distances = new HashMap<>();
		Queue<Object> queue = this.queueFactory.createQueue(automaton, distances);
		Semiring<K> sr = automaton.semiring();
		Map<Object, K> r = new HashMap<>();
		K one = sr.one();
		distances.put(source, one);
		r.put(source, one);
		queue.add(source);

		while (!queue.isEmpty())
		{
			Object q = queue.poll();
			K rQ = r.remove(q);
			for (Object e : automaton.transitionsOut(q))
			{
				Object ne = automaton.to(e);
				K dne = distances.getOrDefault(ne, sr.zero());
				K rwe = sr.multiply(rQ, automaton.transitionWeight(e));
				K sumDneRwe = sr.add(dne, rwe);

				boolean eq = equalityDef.converged(dne, sumDneRwe);
				if (!eq)
				{
					queue.remove(ne);
					distances.put(ne, sumDneRwe);
					K rne = r.getOrDefault(ne, sr.zero());
					r.put(ne, sr.add(rne, rwe));
					queue.add(ne);
				}
			}
		}
		distances.put(source, one);
		return distances;
	}
}
