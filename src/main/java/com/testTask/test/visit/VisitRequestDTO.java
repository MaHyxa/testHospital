package com.testTask.test.visit;

import com.testTask.test.utilities.ValidateTimeVariables;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
@ValidateTimeVariables
public class VisitRequestDTO {

    @NotNull(message = "Start time is required")
    private String start;

    @NotNull(message = "End time is required")
    private String end;

    @NotNull(message = "Incorrect Patient ID")
    private int patientId;

    @NotNull(message = "Incorrect Doctor ID")
    private int doctorId;

}
