package com.wonderx.rwe.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformEvent {
    private String eventType;
    private UUID prescriptionId;
    private UUID patientId;
    private UUID doctorId;
    private UUID studyId;
}
