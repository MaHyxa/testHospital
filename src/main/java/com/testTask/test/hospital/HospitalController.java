package com.testTask.test.hospital;

import com.testTask.test.patient.PatientVisitsResponse;
import com.testTask.test.visit.VisitRequestDTO;
import com.testTask.test.visit.VisitService;
import com.testTask.test.visit.VisitServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hospital")
public class HospitalController {

    private final VisitService visitService;

    public HospitalController(VisitService visitService) {
        this.visitService = visitService;
    }

    @PostMapping("/createVisit")
    public ResponseEntity<?> createVisit(@Valid @RequestBody VisitRequestDTO visitRequest) {
//        for (int i = 0; i < 9000; i++) {
//            visitService.createVisit(VisitGenerator.generate());
//        }
        return visitService.createVisit(visitRequest);
    }


    @GetMapping("/patientVisits")
    public ResponseEntity<?> getPatientVisits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String doctorIds) {

        if (search != null && !search.matches("^[a-zA-Z ]+$")) {
            return ResponseEntity.badRequest().body("Invalid or empty search parameter! Only alphabets and spaces are allowed.");
        }
        if (doctorIds != null && !doctorIds.matches("^[0-9,]+$")) {
            return ResponseEntity.badRequest().body("Invalid or empty search parameter! Only numbers and comas are allowed.");
        }

        String convertedSearch = VisitServiceImpl.convertSearch(search);
        int correctPage = page * size;

        PatientVisitsResponse response = new PatientVisitsResponse();
        response.setCount(visitService.countResults(convertedSearch, doctorIds));
        response.setData(visitService.findPatientsOnPageWithLastVisits(correctPage, size, convertedSearch, doctorIds));
        return ResponseEntity.ok(response);
    }
}