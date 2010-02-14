package com.inexas.pl.entity;

public interface TupleMember {

	String getKey();

	Tuple getParentTuple();

	void register();

	void deregister();

	void recalculate();

}
