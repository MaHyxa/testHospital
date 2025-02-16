package com.testTask.test.doctor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Integer> {
    @Query("SELECT d.id FROM Doctor d WHERE d.id = :id")
    Integer findDoctorIdById(@Param("id") int id);
}
