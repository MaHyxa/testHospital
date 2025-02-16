package com.testTask.test.patient;

import com.testTask.test.visit.VisitDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PatientVisitDTO {
    private String firstName;
    private String lastName;
    private List<VisitDTO> lastVisits;
}
