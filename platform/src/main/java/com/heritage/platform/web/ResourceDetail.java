package com.heritage.platform.web;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class ResourceDetail {
	public Long id;
	public String title;
	public String submitterName;
	public Instant submittedAt;
	public String category;
	public String locationName;
	public String description;
	public String copyrightDeclaration;
	public String status;
	public Long version;
	public String rejectionReason;
	public List<Map<String, Object>> tags;
	public List<Map<String, Object>> attachments;
}
