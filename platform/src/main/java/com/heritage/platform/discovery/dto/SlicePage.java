package com.heritage.platform.discovery.dto;

import java.util.List;

/*
 * 分页结果：items 为当前页数据，total 为符合条件的总条数。
 */
public class SlicePage<T> {

	private List<T> items;
	private long total;
	private int page;
	private int size;

	public List<T> getItems() {
		return items;
	}

	public void setItems(List<T> items) {
		this.items = items;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getTotalPages() {
		if (size <= 0) {
			return 0;
		}
		return (int) ((total + size - 1) / size);
	}
}
