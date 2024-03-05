package net.jhoogland.jautomata.semirings;

import java.util.Comparator;
import java.util.Objects;

public abstract class AbstractSemiring<K> implements Semiring<K> {

	private Comparator<? super K> comparator;

	public AbstractSemiring(Comparator<? super K> comparator) {
		this.comparator = Objects.requireNonNull(comparator);
	}

	@Override
	public Comparator<? super K> getComparator() {
		return comparator;
	}

	public void setComparator(Comparator<? super K> comparator) {
		this.comparator = comparator;
	}
}
