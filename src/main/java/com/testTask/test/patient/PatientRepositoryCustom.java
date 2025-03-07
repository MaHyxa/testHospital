package com.testTask.test.patient;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public class PatientRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    public List<FindPatientsAndLastVisitsDTO> findPatientsAndLastVisits(int page, int size, String search, String doctorIds) {

        String searchWithoutDoctors = """
                        WITH
                        patient_filter AS (
                            SELECT id, first_name, last_name, DENSE_RANK() OVER (ORDER BY last_name, id) AS ranking
                            FROM patient
                            WHERE (:search IS NULL OR MATCH(first_name, last_name) AGAINST (LOWER(:search) IN BOOLEAN MODE))
                        ),
                        paginated_patients AS (
                            SELECT id, first_name, last_name
                            FROM patient_filter
                            WHERE ranking BETWEEN (:page * :size + 1) AND ((:page + 1) * :size)
                        ),
                        latest_visits AS (
                            SELECT patient_id, doctor_id, start_date_time, end_date_time
                            FROM (SELECT v.patient_id, v.doctor_id, v.start_date_time, v.end_date_time,
                                       ROW_NUMBER() OVER (
                                           PARTITION BY v.patient_id, v.doctor_id
                                           ORDER BY v.end_date_time DESC)
                                        AS rn
                                FROM paginated_patients pp
                                JOIN visit v ON v.patient_id = pp.id
                                WHERE v.end_date_time <= NOW()) visits_filter
                            WHERE rn = 1
                        ),
                        count_patients AS (
                            SELECT v2.doctor_id, COUNT(DISTINCT v2.patient_id) AS totalPatients
                            FROM latest_visits lv
                            JOIN visit v2 ON lv.doctor_id = v2.doctor_id
                            GROUP BY v2.doctor_id
                        )
                        
                        SELECT pp2.id as patient_id,
                        pp2.first_name as patientFirstName,
                        pp2.last_name as patientLastName,
                        lv2.start_date_time AS visitStart,
                        lv2.end_date_time AS visitEnd,
                        d.first_name AS doctorFirstName,
                        d.last_name AS doctorLastName,
                        d.doctor_time_zone AS doctorTime,
                        dc.totalPatients AS totalPatients,
                        patient_count.countResults AS countResults
                        FROM paginated_patients pp2
                        LEFT JOIN latest_visits lv2 ON pp2.id = lv2.patient_id
                        LEFT JOIN doctor d ON lv2.doctor_id = d.id
                        LEFT JOIN count_patients dc ON dc.doctor_id = lv2.doctor_id
                        CROSS JOIN (SELECT COUNT(*) AS countResults FROM patient_filter) patient_count
                """;

        String searchWithDoctorAndPatient = """
                        WITH
                        patient_filter AS (
                            SELECT id, first_name, last_name
                            FROM patient
                            WHERE (:search IS NULL OR MATCH(first_name, last_name) AGAINST (LOWER(:search) IN BOOLEAN MODE))
                        ),
                        latest_visits AS (
                            SELECT patient_id, doctor_id, start_date_time, end_date_time
                            FROM (SELECT v.patient_id, v.doctor_id, v.start_date_time, v.end_date_time,
                                       ROW_NUMBER() OVER (
                                           PARTITION BY v.patient_id, v.doctor_id
                                           ORDER BY v.end_date_time DESC)
                                        AS rn
                                FROM patient_filter pf
                                JOIN visit v ON pf.id=v.patient_id
                                WHERE v.end_date_time <= NOW()
                                AND v.doctor_id IN (:doctorIdList)) visits_filter
                            WHERE rn = 1
                        ),
                        count_patients AS (
                            SELECT v2.doctor_id, COUNT(DISTINCT v2.patient_id) AS totalPatients
                            FROM latest_visits lv
                            JOIN visit v2 ON lv.doctor_id = v2.doctor_id
                            GROUP BY v2.doctor_id
                        ),
                        paginate_patients AS (
                            SELECT *, DENSE_RANK() OVER (ORDER BY pf2.id) as ranking
                            FROM patient_filter pf2
                            JOIN latest_visits lv3 ON pf2.id = lv3.patient_id
                        )
                        
                        SELECT pp.id as patient_id,
                        pp.first_name as patientFirstName,
                        pp.last_name as patientLastName,
                        pp.start_date_time AS visitStart,
                        pp.end_date_time AS visitEnd,
                        d.first_name AS doctorFirstName,
                        d.last_name AS doctorLastName,
                        d.doctor_time_zone AS doctorTime,
                        dc.totalPatients AS totalPatients,
                        patient_count.countResults AS countResults
                        FROM paginate_patients pp
                        JOIN doctor d ON pp.doctor_id = d.id
                        JOIN count_patients dc ON dc.doctor_id = pp.doctor_id
                        CROSS JOIN (SELECT COUNT(DISTINCT id) AS countResults FROM paginate_patients) patient_count
                        WHERE ranking BETWEEN (:page * :size + 1) AND ((:page + 1) * :size)
                """;

        String searchWithDoctors = """
                        WITH
                        latest_visits AS (
                            SELECT patient_id, doctor_id, start_date_time, end_date_time, DENSE_RANK() OVER (ORDER BY patient_id) AS ranking
                            FROM (SELECT v.patient_id, v.doctor_id, v.start_date_time, v.end_date_time,
                                       ROW_NUMBER() OVER (
                                           PARTITION BY v.patient_id, v.doctor_id
                                           ORDER BY v.end_date_time DESC)
                                        AS rn
                                FROM visit v
                                WHERE v.end_date_time <= NOW()
                                AND v.doctor_id IN (:doctorIdList)) visits_filter
                            WHERE rn = 1
                        ),
                        count_patients AS (
                            SELECT v2.doctor_id, COUNT(DISTINCT v2.patient_id) AS totalPatients
                            FROM latest_visits lv
                            JOIN visit v2 ON lv.doctor_id = v2.doctor_id
                            GROUP BY v2.doctor_id
                        )
                        
                        SELECT p.id as patient_id,
                        p.first_name as patientFirstName,
                        p.last_name as patientLastName,
                        lv2.start_date_time AS visitStart,
                        lv2.end_date_time AS visitEnd,
                        d.first_name AS doctorFirstName,
                        d.last_name AS doctorLastName,
                        d.doctor_time_zone AS doctorTime,
                        dc.totalPatients AS totalPatients,
                        patient_count.countResults AS countResults
                        FROM latest_visits lv2
                        JOIN doctor d ON lv2.doctor_id = d.id
                        JOIN patient p ON p.id = lv2.patient_id
                        JOIN count_patients dc ON dc.doctor_id = lv2.doctor_id
                        CROSS JOIN (SELECT COUNT(DISTINCT patient_id) AS countResults FROM latest_visits) patient_count
                        WHERE ranking BETWEEN (:page * :size + 1) AND ((:page + 1) * :size)
                """;


        if (doctorIds == null || doctorIds.isEmpty()) {
            Query query = entityManager.createNativeQuery(searchWithoutDoctors, FindPatientsAndLastVisitsDTO.class);
            query.setParameter("page", page);
            query.setParameter("size", size);
            query.setParameter("search", search);
            return query.getResultList();
        } else if (search != null) {
            List<Integer> doctorIdList = Arrays.stream(doctorIds.split(","))
                    .map(Integer::parseInt)
                    .toList();
            Query query = entityManager.createNativeQuery(searchWithDoctorAndPatient, FindPatientsAndLastVisitsDTO.class);
            query.setParameter("search", search);
            query.setParameter("page", page);
            query.setParameter("size", size);
            query.setParameter("doctorIdList", doctorIdList);
            return query.getResultList();
        } else {
            List<Integer> doctorIdList = Arrays.stream(doctorIds.split(","))
                    .map(Integer::parseInt)
                    .toList();
            Query query = entityManager.createNativeQuery(searchWithDoctors, FindPatientsAndLastVisitsDTO.class);
            query.setParameter("page", page);
            query.setParameter("size", size);
            query.setParameter("doctorIdList", doctorIdList);
            return query.getResultList();
        }
    }
}
