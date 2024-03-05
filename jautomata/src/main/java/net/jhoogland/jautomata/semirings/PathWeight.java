package net.jhoogland.jautomata.semirings;

import java.util.LinkedList;
import java.util.Objects;

import net.jhoogland.jautomata.Automaton;
import net.jhoogland.jautomata.Path;

/**
 * A chain of instances of this class specify a path over an automaton and its
 * weight.
 *
 * @author Jasper Hoogland
 *
 * @param <K> weight type
 */
public class PathWeight<K> {
	public PathWeight<K> previous;
	public Object transition;
	public K weight;
	public Semiring<K> src;

	public PathWeight(PathWeight<K> previous, K weight, Semiring<K> src) {
		this.previous = previous;
		this.weight = weight;
		this.src = src;
	}

	public PathWeight(PathWeight<K> previous, K weight, Semiring<K> src, Object transition) {
		this(previous, weight, src);
		this.transition = transition;
	}

	public <L> Path<L, K> path(Automaton<L, K> automaton) {
		LinkedList<Object> transitions = new LinkedList<>();
		PathWeight<K> cur = this;
		while (cur != null) {
			Object t = cur.transition;
			if (t != null) {
				transitions.addFirst(t);
			}
			cur = cur.previous;
		}
		return new Path<>(transitions, weight, automaton);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof PathWeight)) {
			return false;
		}
		PathWeight<?> other = (PathWeight<?>) obj;
		return Objects.equals(this.previous, other.previous) && this.weight.equals(other.weight);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(previous, weight);
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[transition=" + transition + ", weight=" + weight + ", previous=" + previous + "]";
	}

	public K getWeight() {
		return weight;
	}
}
