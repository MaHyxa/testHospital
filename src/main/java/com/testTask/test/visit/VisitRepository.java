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
            AND v.start_date_time < DATE_ADD(:endTime, INTERVAL d.doctor_time_zone MINUTE)
            AND v.end_date_time > DATE_ADD(:startTime, INTERVAL d.doctor_time_zone MINUTE)
            WHERE d.id = :doctorId
            """)
    List<Object[]> checkDoctorExistingVisits(@Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime,
                                             @Param("doctorId") int doctorId);

    @Query(value = """
                SELECT DISTINCT p.id, p.first_name, p.last_name
                FROM patient p
                LEFT JOIN visit v ON p.id = v.patient_id
                LEFT JOIN doctor d ON v.doctor_id = d.id
                WHERE (:search IS NULL OR MATCH(p.first_name, p.last_name) AGAINST (LOWER(:search) IN BOOLEAN MODE))
                AND (:doctorIds IS NULL OR FIND_IN_SET(v.doctor_id, :doctorIds) > 0)
                ORDER BY p.last_name
                LIMIT :size OFFSET :page
            """, nativeQuery = true)
    List<Object[]> findPatientsOnPage(@Param("page") int page,
                                     @Param("size") int size,
                                     @Param("search") String search,
                                     @Param("doctorIds") String doctorIds);

    @Query(value = """
             SELECT
                    v.patient_id as patient,
                    v.start_date_time AS visitStart,
                    v.end_date_time AS visitEnd,
                    d.first_name AS doctorFirstName,
                    d.last_name AS doctorLastName,
                    d.doctor_time_zone as doctorTime,
                    (SELECT COUNT(DISTINCT v2.patient_id) FROM visit v2 WHERE v2.doctor_id = d.id) AS totalPatients
             FROM visit v
             JOIN doctor d ON v.doctor_id = d.id
             WHERE v.patient_id IN :patientIds
             AND (:doctorIds IS NULL OR FIND_IN_SET(v.doctor_id, :doctorIds) > 0)
         """, nativeQuery = true)
    List<Object[]> collectVisitsForPatients(@Param("patientIds") List<Integer> patientIds,
                                            @Param("doctorIds") String doctorIds);

    @Query(value = """
                SELECT COUNT(DISTINCT p.id)
                FROM patient p
                LEFT JOIN visit v ON p.id = v.patient_id
                LEFT JOIN doctor d ON v.doctor_id = d.id
                WHERE (:search IS NULL OR MATCH(p.first_name, p.last_name) AGAINST (LOWER(:search) IN BOOLEAN MODE))
                AND (:doctorIds IS NULL OR FIND_IN_SET(v.doctor_id, :doctorIds) > 0)
            """, nativeQuery = true)
    Integer countResults(@Param("search") String search,
                         @Param("doctorIds") String doctorIds);

}