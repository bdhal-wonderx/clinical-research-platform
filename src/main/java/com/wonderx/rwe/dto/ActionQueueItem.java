package com.wonderx.rwe.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActionQueueItem {
    private String type;
    private String description;
    private String priority;
    private String entityId;
}
