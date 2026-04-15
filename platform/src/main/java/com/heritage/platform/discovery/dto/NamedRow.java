package com.heritage.platform.discovery.dto;

/*
 * 下拉框用的 id + 名称，分类与标签共用这一形状。
 */
public class NamedRow {

	private long id;
	private String name;

	public NamedRow() {
	}

	public NamedRow(long id, String name) {
		this.id = id;
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
