package com.heritage.platform.discovery.taxonomy;

import java.util.List;

/**
 * 文物类型：一大类下挂若干叶子类型。
 */
public class HeritageTypeGroup {

	private final String groupCode;
	private final String groupName;
	private final List<TaxonomyOption> types;

	public HeritageTypeGroup(String groupCode, String groupName, List<TaxonomyOption> types) {
		this.groupCode = groupCode;
		this.groupName = groupName;
		this.types = types;
	}

	public String getGroupCode() {
		return groupCode;
	}

	public String getGroupName() {
		return groupName;
	}

	public List<TaxonomyOption> getTypes() {
		return types;
	}
}
