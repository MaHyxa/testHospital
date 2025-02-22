package com.testTask.test.doctor;

import com.testTask.test.visit.Visit;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 40, nullable = false)
    private String firstName;
    @Column(length = 100, nullable = false)
    private String lastName;

    @Column(length = 50, nullable = false)
    private String doctorTimeZone;

    @OneToMany(mappedBy = "doctor")
    private List<Visit> visits;
}
