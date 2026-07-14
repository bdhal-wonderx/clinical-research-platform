package com.wonderx.rwe.mapper;

import com.wonderx.rwe.dto.*;
import com.wonderx.rwe.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DoctorMapper {

    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "mouAgreements", ignore = true)
    @Mapping(target = "studyAssignments", ignore = true)
    DoctorResponse toResponse(Doctor doctor);

    DoctorProfileResponse toProfileResponse(DoctorProfile profile);

    @Mapping(target = "studyId", source = "study.id")
    @Mapping(target = "studyCode", source = "study.studyCode")
    DoctorMouResponse toMouResponse(DoctorMou mou);

    List<DoctorMouResponse> toMouResponseList(List<DoctorMou> mouList);

    @Mapping(target = "studyId", source = "study.id")
    @Mapping(target = "studyCode", source = "study.studyCode")
    @Mapping(target = "studyName", source = "study.studyName")
    DoctorStudyResponse toStudyAssignmentResponse(DoctorStudy doctorStudy);

    List<DoctorStudyResponse> toStudyAssignmentResponseList(List<DoctorStudy> assignments);

    StudyResponse toStudyResponse(Study study);

    List<StudyResponse> toStudyResponseList(List<Study> studies);
}
