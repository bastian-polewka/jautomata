package net.jhoogland.jautomata.semirings;

import java.util.Comparator;

/**
 * Implementation of the log semiring, which is a semiring of the set of
 * non-negative real numbers (represented by <code>double</code> values in this
 * implementation). The log semiring is a commutative semifield. It is not
 * idempotent and it is not k-closed for any k. Its multiplication operation is
 * <code>+</code> with identity <code>0</code>, and its addition operation is
 * <code>-log(exp(-x1)+exp(-x2))</code> with identity <code>infinity</code>. The
 * multiplicative inverse is <code>-x</code>.
 *
 * @author Jasper Hoogland
 */
public class LogSemiring extends AbstractSemifield<Double> {

	public LogSemiring() {
		super(Comparator.naturalOrder());
	}

	@Override
	public Double multiply(Double x1, Double x2) {
		return x1 + x2;
	}

	@Override
	public Double add(Double x1, Double x2) {
		return -Math.log(Math.exp(-x1) + Math.exp(-x2));
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
	public boolean isIdempotent() {
		return false;
	}

	@Override
	public boolean isCommutative() {
		return true;
	}

	@Override
	public boolean isKClosed(int k) {
		return false;
	}

	@Override
	public Double inverse(Double x) {
		return -x;
	}
}
