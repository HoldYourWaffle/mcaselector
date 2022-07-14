package net.querz.mcaselector.filter.filters;

import net.querz.mcaselector.filter.Comparator;
import net.querz.mcaselector.filter.FilterType;
import net.querz.mcaselector.filter.IntFilter;
import net.querz.mcaselector.filter.Operator;
import net.querz.mcaselector.io.anvil.chunk.ChunkData;
import net.querz.nbt.ListTag;

public class ProtoEntityAmountFilter extends IntFilter {

	public ProtoEntityAmountFilter() {
		this(Operator.AND, Comparator.EQUAL, 0);
	}

	private ProtoEntityAmountFilter(Operator operator, Comparator comparator, int value) {
		super(FilterType.PROTO_ENTITY_AMOUNT, operator, comparator, value);
	}

	@Override
	protected Integer getNumber(ChunkData data) {
		if (data.region() == null || data.region().getData() == null) {
			return 0;
		}
		ListTag protoEntities = data.region().getData().getList("entities");
		if (protoEntities == null) {
			return 0;
		}
		return protoEntities.size();
	}

	@Override
	public void setFilterValue(String raw) {
		super.setFilterValue(raw);
		if (isValid() && getFilterValue() < 0) {
			setFilterNumber(0);
			setValid(false);
		}
	}

	@Override
	public ProtoEntityAmountFilter clone() {
		return new ProtoEntityAmountFilter(getOperator(), getComparator(), value);
	}
}
