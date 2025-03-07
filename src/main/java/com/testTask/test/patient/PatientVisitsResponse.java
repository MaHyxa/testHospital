package com.testTask.test.patient;

import java.util.List;

public record PatientVisitsResponse(List<PatientVisitDTO> data, long count) {}
