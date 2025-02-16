package com.testTask.test.visit;

import com.testTask.test.patient.PatientVisitDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;
public interface VisitService {
    ResponseEntity<?> createVisit(VisitRequestDTO visitRequest);

    List<PatientVisitDTO> findPatientsOnPage(int page, int size, String search, String doctorIds);

    int countResults(String search, String doctorIds);
}