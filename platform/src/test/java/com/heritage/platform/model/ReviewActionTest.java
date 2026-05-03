package com.heritage.platform.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class ReviewActionTest {

    @Test
    @DisplayName("验证批准动作的判定逻辑")
    void testIsApproved() {
        assertTrue(ReviewAction.APPROVE.isApproved());
        assertTrue(ReviewAction.APPROVED.isApproved());
        assertFalse(ReviewAction.REJECT.isApproved());
    }

    @Test
    @DisplayName("验证驳回动作的判定逻辑")
    void testIsRejected() {
        assertTrue(ReviewAction.REJECT.isRejected());
        assertTrue(ReviewAction.REJECTED.isRejected());
        assertFalse(ReviewAction.APPROVE.isRejected());
    }

    @ParameterizedTest
    @EnumSource(ReviewAction.class)
    @DisplayName("验证显示值转换逻辑")
    void testToDisplayValue(ReviewAction action) {
        String display = action.toDisplayValue();
        if (action.isApproved()) {
            assertEquals("APPROVED", display);
        } else {
            assertEquals("REJECTED", display);
        }
    }
}