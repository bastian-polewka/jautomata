package net.jhoogland.jautomata;

import java.util.*;

import net.jhoogland.jautomata.convergence.ExactConvergence;
import net.jhoogland.jautomata.convergence.WeightConvergenceCondition;
import net.jhoogland.jautomata.queues.DefaultQueueFactory;
import net.jhoogland.jautomata.queues.QueueFactory;
import net.jhoogland.jautomata.semirings.Semiring;
import net.jhoogland.jautomata.weightfilter.DefaultWeightFilter;
import net.jhoogland.jautomata.weightfilter.WeightFilter;

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
	private final QueueFactory<K> queueFactory;
	private final WeightFilter<K> weightFilter;
	private final WeightConvergenceCondition<K> equalityDef;

	public SingleSourceShortestDistances(QueueFactory<K> queueFactory, WeightFilter<K> weightFilter, WeightConvergenceCondition<K> equalityDef) {
		this.queueFactory = queueFactory;
		this.weightFilter = weightFilter;
		this.equalityDef = equalityDef;
	}

	public SingleSourceShortestDistances() {
		this(new DefaultQueueFactory<>(), new DefaultWeightFilter<>(), new ExactConvergence<>());
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

		queue: while (!queue.isEmpty())
		{
			Object q = queue.poll();
			K rQ = r.remove(q);

			switch(weightFilter.filterState(q, rQ)) {
			case CONTINUE:
				break;
			case SKIP:
				continue queue;
			case STOP:
				break queue;
			}

			transitions: for (Object e : automaton.transitionsOut(q))
			{
				Object ne = automaton.to(e);
				K dne = distances.getOrDefault(ne, sr.zero());
				K rwe = sr.multiply(rQ, automaton.transitionWeight(e));
				K sumDneRwe = sr.add(dne, rwe);

				sumDneRwe = weightFilter.filterWeight(sumDneRwe);

				switch(weightFilter.filterTransition(q, rQ, e, ne, sumDneRwe)) {
				case CONTINUE:
					break;
				case SKIP:
					continue transitions;
				case STOP:
					break queue;
				}

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
