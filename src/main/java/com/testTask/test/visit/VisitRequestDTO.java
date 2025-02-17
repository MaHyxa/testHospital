package com.testTask.test.visit;

import com.testTask.test.utilities.ValidateTimeVariables;
import lombok.AllArgsConstructor;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;

@Data
@ValidateTimeVariables
@AllArgsConstructor
@NoArgsConstructor
public class VisitRequestDTO {

    @NotNull(message = "Start time is required")
    private String start;

    @NotNull(message = "End time is required")
    private String end;

    @NotNull(message = "Incorrect Patient ID")
    private Integer patientId;

    @NotNull(message = "Incorrect Doctor ID")
    private Integer doctorId;

}
