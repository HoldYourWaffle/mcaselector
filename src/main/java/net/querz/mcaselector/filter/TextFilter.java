package net.querz.mcaselector.filter;

import net.querz.mcaselector.io.anvil.chunk.ChunkData;

public abstract class TextFilter<T> extends Filter<T> {
	// SOON most (if not all) TextFilter's implementations seem near identical, could an abstract "getContainer"-like method be enough?

	private static final Comparator[] comparators = {
			Comparator.CONTAINS,
			Comparator.CONTAINS_NOT,
			Comparator.INTERSECTS
			// XXX why do not all TextFilter's support EQUAL and NOT_EQUAL?
	};

	protected T value;

	private Comparator comparator;

	public TextFilter(FilterType type, Operator operator, Comparator comparator, T value) {
		super(type, operator);
		this.comparator = comparator;
		this.value = value;
	}

	@Override
	public Comparator[] getComparators() {
		return comparators;
	}

	@Override
	public Comparator getComparator() {
		return comparator;
	}

	public void setComparator(Comparator comparator) {
		this.comparator = comparator;
	}

	@Override
	public boolean matches(ChunkData data) {
		return switch (comparator) {
			case CONTAINS -> contains(value, data);
			case CONTAINS_NOT -> containsNot(value, data);
			case INTERSECTS -> intersects(value, data);
			default -> false;
		};
	}

	@Override
	public T getFilterValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public abstract String getFormatText();

	public abstract boolean contains(T value, ChunkData data);

	public abstract boolean containsNot(T value, ChunkData data);

	public abstract boolean intersects(T value, ChunkData data);
}
