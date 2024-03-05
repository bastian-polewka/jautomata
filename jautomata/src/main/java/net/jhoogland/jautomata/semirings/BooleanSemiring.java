package net.jhoogland.jautomata.semirings;

import java.util.Comparator;

/**
 * Implementation of the Boolean semiring, which is a semiring of the Boolean
 * values <code>true</code> and <code>false</code>. Shortest distance algorithms
 * use this semiring to compute whether unweighted automata have complete paths.
 * The Boolean semiring is a commutative semifield. It is idempotent and it
 * k-closed for any k > 0. Its multiplication operation is <code>and</code> with
 * identity <code>true</code>, and its addition operation is <code>or</code>
 * with identity <code>false</code>. The multiplicative inverse is
 * <code>true</code>.
 *
 * @author Jasper Hoogland
 */
public class BooleanSemiring extends AbstractSemifield<Boolean> {

	public BooleanSemiring() {
		super(Comparator.reverseOrder());
	}

	@Override
	public Boolean multiply(Boolean x1, Boolean x2) {
		return x1 && x2;
	}

	@Override
	public Boolean add(Boolean x1, Boolean x2) {
		return x1 || x2;
	}

	@Override
	public Boolean one() {
		return true;
	}

	@Override
	public Boolean zero() {
		return false;
	}

	@Override
	public Boolean inverse(Boolean x) {
		return true;
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
