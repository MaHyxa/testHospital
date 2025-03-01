package com.testTask.test.visit;

import java.sql.Timestamp;

public record FindPatientsAndLastVisitsDTO(int patientID, String patientFirstName, String patientLastName, Timestamp visitStart, Timestamp visitEnd, String doctorFirstName, String doctorLastName, String doctorTimeZone, Long totalPatients) {
}
