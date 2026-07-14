package com.wonderx.rwe.service;

import com.wonderx.rwe.dto.StudyCreateRequest;
import com.wonderx.rwe.dto.StudyResponse;
import com.wonderx.rwe.entity.Study;
import com.wonderx.rwe.enums.StudyStatus;
import com.wonderx.rwe.exception.BusinessException;
import com.wonderx.rwe.exception.ResourceNotFoundException;
import com.wonderx.rwe.mapper.DoctorMapper;
import com.wonderx.rwe.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;
    private final DoctorMapper doctorMapper;
    private final AuditService auditService;

    @Transactional
    public StudyResponse createStudy(StudyCreateRequest request) {
        if (studyRepository.findByStudyCode(request.getStudyCode()).isPresent()) {
            throw new BusinessException("Study code already exists: " + request.getStudyCode());
        }

        Study study = Study.builder()
                .studyCode(request.getStudyCode())
                .studyName(request.getStudyName())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(StudyStatus.DRAFT)
                .build();

        studyRepository.save(study);
        auditService.log("STUDY", study.getId(), "CREATED", "SYSTEM", null, null);
        return doctorMapper.toStudyResponse(study);
    }

    @Transactional(readOnly = true)
    public StudyResponse getStudyById(UUID studyId) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new ResourceNotFoundException("Study not found: " + studyId));
        return doctorMapper.toStudyResponse(study);
    }

    @Transactional(readOnly = true)
    public List<StudyResponse> getActiveStudies() {
        return doctorMapper.toStudyResponseList(studyRepository.findByStatus(StudyStatus.ACTIVE));
    }

    @Transactional(readOnly = true)
    public List<StudyResponse> getAllStudies() {
        return doctorMapper.toStudyResponseList(studyRepository.findAll());
    }

    @Transactional
    public StudyResponse activateStudy(UUID studyId) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new ResourceNotFoundException("Study not found: " + studyId));
        study.setStatus(StudyStatus.ACTIVE);
        studyRepository.save(study);
        auditService.log("STUDY", study.getId(), "ACTIVATED", "SYSTEM", null, null);
        return doctorMapper.toStudyResponse(study);
    }
}
