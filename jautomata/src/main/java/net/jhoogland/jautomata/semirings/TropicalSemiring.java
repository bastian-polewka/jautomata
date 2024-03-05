package net.jhoogland.jautomata.semirings;

import java.util.Comparator;

/**
 * Implementation of the tropical semiring, which is a semiring of the set of
 * non-negative real numbers (represented by <code>double</code> values in this
 * implementation). Shortest distance algorithms use this semiring to compute
 * the shortest distance of weighted automata. The tropical semiring is a
 * commutative semifield. It is idempotent and k-closed for any k > 0. Its
 * multiplication operation is <code>+</code> with identity <code>0</code>, and
 * its addition operation is <code>min(x1, x2)</code> with identity
 * <code>infinity</code>. The multiplicative inverse is <code>-x</code>.
 *
 * @author Jasper Hoogland
 */
public class TropicalSemiring extends AbstractSemifield<Double> {

	public TropicalSemiring() {
		super(Comparator.naturalOrder());
	}

	@Override
	public Double multiply(Double x1, Double x2) {
		return x1 + x2;
	}

	@Override
	public Double add(Double x1, Double x2) {
		return Math.min(x1, x2);
	}

	@Override
	public Double one() {
		return 0.0;
	}

	@Override
	public Double zero() {
		return Double.POSITIVE_INFINITY;
	}

	@Override
	public Double inverse(Double x) {
		return -x;
	}

	@Override
	public boolean isIdempotent() {
		return true;
	}

	@Override
	public boolean isCommutative() {
		return true;
	}

	@Override
	public boolean isKClosed(int k) {
		return true;
	}
}
