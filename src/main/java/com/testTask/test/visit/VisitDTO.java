package com.testTask.test.visit;

import com.testTask.test.doctor.DoctorDTO;

public record VisitDTO(String start, String end, DoctorDTO doctor) {}

