package com.heritage.platform.security;
import com.heritage.platform.model.HeritageUser;

public class HeritageUserDetails {
    private final long id;
    private final String displayName;

    public HeritageUserDetails(HeritageUser user,long id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public long getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }
}