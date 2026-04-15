package com.heritage.platform.discovery.taxonomy;

/**
 * 下拉选项：稳定 id + 业务 code + 展示名。
 */
public class TaxonomyOption {

	private final long id;
	private final String code;
	private final String name;

	public TaxonomyOption(long id, String code, String name) {
		this.id = id;
		this.code = code;
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}
}
