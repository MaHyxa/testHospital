package com.testTask.test.visit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

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

    @Query(value = """
                SELECT COUNT(DISTINCT p.id)
                FROM patient p
                LEFT JOIN visit v ON p.id = v.patient_id
                LEFT JOIN doctor d ON v.doctor_id = d.id
                WHERE (:search IS NULL OR MATCH(p.first_name, p.last_name) AGAINST (LOWER(:search) IN BOOLEAN MODE))
                AND (:doctorIds IS NULL OR FIND_IN_SET(v.doctor_id, :doctorIds) > 0)
                AND (v.patient_id IS NULL
                       OR v.end_date_time = (
                            SELECT MAX(v2.end_date_time)
                            FROM visit v2
                            WHERE v2.patient_id = v.patient_id
                            AND v2.doctor_id = v.doctor_id
                            AND v2.end_date_time <= NOW()))
            """, nativeQuery = true)
    Integer countResults(@Param("search") String search,
                         @Param("doctorIds") String doctorIds);


    @Query(value = """
                WITH patient_filter AS (
                    SELECT p.id as patient_id, p.first_name, p.last_name
                    FROM patient p
                    LEFT JOIN visit v ON p.id = v.patient_id
                    LEFT JOIN doctor d ON v.doctor_id = d.id
                    WHERE (:search IS NULL OR MATCH(p.first_name, p.last_name) AGAINST (LOWER(:search) IN BOOLEAN MODE))
                    AND (:doctorIds IS NULL OR FIND_IN_SET(d.id, :doctorIds) > 0)
                    AND (v.patient_id IS NULL
                       OR v.end_date_time = (
                            SELECT MAX(v2.end_date_time)
                            FROM visit v2
                            WHERE v2.patient_id = v.patient_id
                            AND v2.doctor_id = v.doctor_id
                            AND v2.end_date_time <= NOW()))
                    GROUP BY p.id
                    LIMIT :size OFFSET :page
                )
                
                SELECT
                    pf.patient_id as patient,
                    pf.first_name as patientFirstName,
                    pf.last_name as patientLastName,
                    v.start_date_time AS visitStart,
                    v.end_date_time AS visitEnd,
                    d.first_name AS doctorFirstName,
                    d.last_name AS doctorLastName,
                    d.doctor_time_zone as doctorTime,
                    (SELECT COUNT(DISTINCT v2.patient_id) FROM visit v2 WHERE v2.doctor_id = d.id AND v2.end_date_time <= NOW()) AS totalPatients
                FROM patient_filter pf
                LEFT JOIN visit v ON pf.patient_id = v.patient_id
                LEFT JOIN doctor d ON v.doctor_id = d.id
                WHERE (v.patient_id IS NULL
                       OR v.end_date_time = (
                            SELECT MAX(v2.end_date_time)
                            FROM visit v2
                            WHERE v2.patient_id = v.patient_id
                            AND v2.doctor_id = v.doctor_id
                            AND v2.end_date_time <= NOW()))
                AND (:doctorIds IS NULL OR FIND_IN_SET(v.doctor_id, :doctorIds) > 0)
            """, nativeQuery = true)
    List<FindPatientsAndLastVisitsDTO> findPatientsAndLastVisits(@Param("page") int page,
                                                                 @Param("size") int size,
                                                                 @Param("search") String search,
                                                                 @Param("doctorIds") String doctorIds);

}