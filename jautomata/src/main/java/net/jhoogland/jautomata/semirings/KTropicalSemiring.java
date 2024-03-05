package net.jhoogland.jautomata.semirings;

import java.util.*;

/**
 * This semiring is used by the shortest distance algorithm to determine the n
 * shortest distances to a state. If the storePath field has value
 * <code>true</code>, then the paths are stored that led to the n shortest
 * distances.\
 *
 * @author Jasper Hoogland
 */
public class KTropicalSemiring<K> extends AbstractSemiring<List<PathWeight<K>>> {

	private final int k;
	private final boolean storePath;
	private final Semiring<K> src;
	private final Comparator<? super PathWeight<K>> weightComparator;

	public KTropicalSemiring(int k, boolean storePath, Semiring<K> src) {
		super(Comparator.comparing(w -> w.isEmpty() ? src.zero() : w.get(0).getWeight(), src.getComparator()));
		this.k = k;
		this.storePath = storePath;
		this.src = Objects.requireNonNull(src);
		this.weightComparator = Comparator.comparing(PathWeight::getWeight, src.getComparator());
	}

	@Override
	public List<PathWeight<K>> multiply(List<PathWeight<K>> x1, List<PathWeight<K>> x2) {
		List<PathWeight<K>> sorted = new ArrayList<>();
		for (PathWeight<K> pw1 : x1) {
			for (PathWeight<K> pw2 : x2) {
				K res = src.multiply(pw1.weight, pw2.weight);
				if (storePath) {
					sorted.add(new PathWeight<>(pw1, res, src, pw2.transition));
				} else {
					sorted.add(new PathWeight<>(null, res, src, null));
				}
			}
		}
		Collections.sort(sorted, weightComparator);
		List<PathWeight<K>> sum = new ArrayList<>();
		Iterator<PathWeight<K>> it = sorted.iterator();
		while (sum.size() < k && it.hasNext()) {
			sum.add(it.next());
		}
		return sum;
	}

	@Override
	public List<PathWeight<K>> add(List<PathWeight<K>> x1, List<PathWeight<K>> x2) {
		List<PathWeight<K>> sorted = new ArrayList<>(x1);
		sorted.addAll(x2);
		Collections.sort(sorted, weightComparator);
		List<PathWeight<K>> sum = new ArrayList<>();
		Iterator<PathWeight<K>> it = sorted.iterator();
		while (sum.size() < k && it.hasNext()) {
			sum.add(it.next());
		}
		return sum;
	}

	@Override
	public List<PathWeight<K>> one() {
		return Collections.singletonList(new PathWeight<>(null, src.one(), src));
	}

	@Override
	public List<PathWeight<K>> zero() {
		return Collections.emptyList();
	}

	@Override
	public boolean isIdempotent() {
		return k < 2;
	}

	@Override
	public boolean isCommutative() {
		return !storePath;
	}

	@Override
	public boolean isKClosed(int k) {
		return k >= this.k - 1;
	}

	public int getK() {
		return k;
	}

	public Semiring<K> getSrc() {
		return src;
	}
}
