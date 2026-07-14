package com.wonderx.rwe.service;

import com.wonderx.rwe.dto.SupportQueryRequest;
import com.wonderx.rwe.entity.Doctor;
import com.wonderx.rwe.entity.SupportQuery;
import com.wonderx.rwe.enums.QueryStatus;
import com.wonderx.rwe.exception.ResourceNotFoundException;
import com.wonderx.rwe.repository.DoctorRepository;
import com.wonderx.rwe.repository.SupportQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupportQueryService {

    private final SupportQueryRepository supportQueryRepository;
    private final DoctorRepository doctorRepository;

    @Transactional
    public SupportQuery createQuery(UUID doctorId, SupportQueryRequest request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        return supportQueryRepository.save(SupportQuery.builder()
                .doctor(doctor)
                .category(request.getCategory())
                .subject(request.getSubject())
                .detail(request.getDetail())
                .status(QueryStatus.OPEN)
                .build());
    }

    @Transactional(readOnly = true)
    public List<SupportQuery> getQueriesByDoctor(UUID doctorId) {
        return supportQueryRepository.findByDoctorId(doctorId);
    }
}
