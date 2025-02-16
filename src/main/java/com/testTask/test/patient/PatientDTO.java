package com.testTask.test.patient;

import com.testTask.test.visit.VisitDTO;

import java.util.List;

public record PatientDTO(String firstName, String lastName, List<VisitDTO> lastVisits) {}
