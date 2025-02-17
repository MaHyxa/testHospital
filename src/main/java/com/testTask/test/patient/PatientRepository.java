package com.testTask.test.patient;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Integer> {
    @Query("SELECT p.id FROM Patient p WHERE p.id = :id")
    @Cacheable(value = "patients", key = "#id")
    Integer findPatientIdById(@Param("id") int id);
}
