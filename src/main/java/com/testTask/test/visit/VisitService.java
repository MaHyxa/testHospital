package com.testTask.test.visit;

import com.testTask.test.patient.PatientVisitsResponse;
import org.springframework.http.ResponseEntity;

public interface VisitService {
    ResponseEntity<?> createVisit(VisitRequestDTO visitRequest);

    PatientVisitsResponse findPatientsAndLastVisits(int page, int size, String search, String doctorIds);

}