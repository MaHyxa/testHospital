package com.testTask.test.visit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {

    @Query(nativeQuery = true, value = """
                SELECT COUNT(v.id), d.doctor_time_zone
                FROM doctor d
                LEFT JOIN visit v ON d.id = v.doctor_id
                AND v.start_date_time < CONVERT_TZ(:endTime, d.doctor_time_zone, 'UTC')
                AND v.end_date_time > CONVERT_TZ(:startTime, d.doctor_time_zone, 'UTC')
                WHERE d.id = :doctorId
            """)
    CheckDoctorExistingVisitsDTO checkDoctorExistingVisits(@Param("startTime") LocalDateTime startTime,
                                                           @Param("endTime") LocalDateTime endTime,
                                                           @Param("doctorId") int doctorId);
}