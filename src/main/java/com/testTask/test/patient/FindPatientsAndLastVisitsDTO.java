package com.testTask.test.patient;

import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.SqlResultSetMapping;

import java.sql.Timestamp;

@SqlResultSetMapping(
        name = "FindPatientsAndLastVisitsDTOMapping",
        classes = @ConstructorResult(
                targetClass = FindPatientsAndLastVisitsDTO.class,
                columns = {
                        @ColumnResult(name = "id", type = Integer.class),
                        @ColumnResult(name = "first_name", type = String.class),
                        @ColumnResult(name = "last_name", type = String.class),
                        @ColumnResult(name = "start_date_time", type = Timestamp.class),
                        @ColumnResult(name = "end_date_time", type = Timestamp.class),
                        @ColumnResult(name = "doctor_first_name", type = String.class),
                        @ColumnResult(name = "doctor_last_name", type = String.class),
                        @ColumnResult(name = "doctor_time_zone", type = String.class),
                        @ColumnResult(name = "totalPatients", type = Long.class),
                        @ColumnResult(name = "count_results", type = Long.class)
                }
        )
)
public record FindPatientsAndLastVisitsDTO(int patientID, String patientFirstName, String patientLastName,
                                           Timestamp visitStart, Timestamp visitEnd, String doctorFirstName,
                                           String doctorLastName, String doctorTimeZone, Long totalPatients,
                                           Long countResults) {
}
