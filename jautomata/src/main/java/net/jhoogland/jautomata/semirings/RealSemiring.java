package net.jhoogland.jautomata.semirings;

import java.util.Comparator;

/**
 * Implementation of the semiring of non-negative real numbers (represented by
 * <code>double</code> values in this implementation). Shortest distance
 * algorithms use this semiring to compute the sum of paths weights of weighted
 * automata. This semiring is a commutative semifield. It is not idempotent and
 * it is not k-closed for any k. Its multiplication operation is <code>*</code>
 * with identity <code>1</code>, and its addition operation is <code>+</code>
 * with identity <code>0</code>, The multiplicative inverse is
 * <code>1 / x</code>.
 *
 * @author Jasper Hoogland
 */

public class RealSemiring extends AbstractSemifield<Double> {

	public RealSemiring() {
		super(Comparator.reverseOrder());
	}

	@Override
	public Double multiply(Double x1, Double x2) {
		return x1 * x2;
	}

	@Override
	public Double add(Double x1, Double x2) {
		return x1 + x2;
	}

	@Override
	public Double one() {
		return 1.0;
	}

	@Override
	public Double zero() {
		return 0.0;
	}

	@Override
	public Double inverse(Double x) {
		return 1.0 / x;
	}

	/**
	 * @return <code>false</code>
	 */
	@Override
	public boolean isIdempotent() {
		return false;
	}

	/**
	 *
	 * @return <code>true</code>
	 */
	@Override
	public boolean isCommutative() {
		return true;
	}

	/**
	 *
	 * @return <code>false</code>
	 */
	@Override
	public boolean isKClosed(int k) {
		return false;
	}
}
