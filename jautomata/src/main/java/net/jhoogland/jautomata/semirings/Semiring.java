package net.jhoogland.jautomata.semirings;

import java.util.Comparator;

/**
 * Semirings must implement this interface. A semiring is defined by the methods
 * <code>multiply(x1, x2)</code>, <code>add(x1, x2)</code>, <code>one()</code>
 * and <code>zero()</code>. The methods <code>isIdempotent()</code>,
 * <code>isCommutative()</code>, and <code>isKClosed(int k)</code> specify
 * properties of the semiring.
 *
 * @author Jasper Hoogland
 *
 * @param <K> the type of the elements of the semiring.
 */
public interface Semiring<K> {

	// Definition

	/**
	 * @return the product of the specified weights
	 */
	K multiply(K x1, K x2);

	/**
	 * @return the sum of the specified weights
	 */
	K add(K x1, K x2);

	/**
	 * @return the multiplication identity of this semiring
	 */
	K one();

	/**
	 * @return the addition identity of this semiring
	 */
	K zero();

	// Properties

	boolean isIdempotent();

	boolean isCommutative();

	boolean isKClosed(int k);

	Comparator<? super K> getComparator();
}
