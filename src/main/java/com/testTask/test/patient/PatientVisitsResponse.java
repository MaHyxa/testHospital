package com.testTask.test.patient;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PatientVisitsResponse {
    private List<PatientVisitDTO> data;
    private int count;
}
