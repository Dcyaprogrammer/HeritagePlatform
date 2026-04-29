package com.heritage.platform.model;

public enum ReviewAction {
	APPROVE,
	REJECT,
	APPROVED,
	REJECTED;

	public boolean isApproved() {
		return this == APPROVE || this == APPROVED;
	}

	public boolean isRejected() {
		return this == REJECT || this == REJECTED;
	}

	public String toDisplayValue() {
		return isApproved() ? "APPROVED" : "REJECTED";
	}
}
