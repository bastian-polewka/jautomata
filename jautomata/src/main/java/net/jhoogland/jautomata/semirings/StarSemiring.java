package net.jhoogland.jautomata.semirings;

public interface StarSemiring<K> extends Semiring<K> {

	K star(K x);
}
