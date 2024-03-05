package net.jhoogland.jautomata.semirings;

import java.util.Comparator;

public abstract class AbstractSemifield<K> extends AbstractSemiring<K> implements Semifield<K> {

	public AbstractSemifield(Comparator<? super K> comparator) {
		super(comparator);
	}
}
