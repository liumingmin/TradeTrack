package com.ming.trade.track.model;

public interface Callback<T> {
	public void invoke(T o);
}
