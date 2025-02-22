package com.testTask.test.visit;

import com.testTask.test.doctor.Doctor;
import com.testTask.test.doctor.DoctorDTO;
import com.testTask.test.patient.Patient;
import com.testTask.test.patient.PatientRepository;
import com.testTask.test.patient.PatientVisitDTO;
import com.testTask.test.utilities.TimeVariablesValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

@Primary
@Service
@RequiredArgsConstructor
public class VisitServiceImpl implements VisitService {

    private final PatientRepository patientRepository;
    private final VisitRepository visitRepository;

    @Override
    public ResponseEntity<?> createVisit(VisitRequestDTO visitRequest) {

        if (visitRequest == null) {
            throw new IllegalArgumentException("Visit request cannot be null");
        }

        if (visitRequest.getPatientId() == null) {
            throw new IllegalArgumentException("Patient ID cannot be null");
        }

        if (visitRequest.getDoctorId() == null) {
            throw new IllegalArgumentException("Doctor ID cannot be null");
        }

        Visit visit = new Visit();
        LocalDateTime start;
        LocalDateTime end;
        int doctorId = visitRequest.getDoctorId();

        try {
            start = LocalDateTime.parse(visitRequest.getStart());
            end = LocalDateTime.parse(visitRequest.getEnd());
        } catch (Exception e) {
            throw new IllegalArgumentException("Time must be in the correct format: yyyy-MM-dd'T'HH:mm:ss");
        }

        Integer patientId = patientRepository.findPatientIdById(visitRequest.getPatientId());

        if (patientId != null) {
            Patient patient = new Patient();
            patient.setId(patientId);
            visit.setPatient(patient);
        } else
            return new ResponseEntity<>("Unable to create visit. Selected patient wasn't found", HttpStatus.NOT_FOUND);

        CheckDoctorExistingVisitsDTO checkVisits = visitRepository.checkDoctorExistingVisits(start, end, doctorId);

        long visitCount = checkVisits.visitCount();
        String docTimeZone = checkVisits.doctor_timeZone();

        if (docTimeZone != null) {
            Doctor doctor = new Doctor();
            doctor.setId(doctorId);
            visit.setDoctor(doctor);
        } else {
            return new ResponseEntity<>("Unable to create visit. Selected Doctor wasn't found", HttpStatus.NOT_FOUND);
        }

        if (visitCount == 0) {
            int offsetMinutes = getTimezoneOffsetInMinutes(docTimeZone);
            if (start.minusMinutes(offsetMinutes).isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
                return new ResponseEntity<>("Unable to create visit. Visit can not be in past.", HttpStatus.CONFLICT);
            } else {
                visit.setStartDateTime(start.minusMinutes(offsetMinutes));
                visit.setEndDateTime(end.minusMinutes(offsetMinutes));
            }
        } else {
            return new ResponseEntity<>("Unable to create visit. Selected time is occupied, please choose another time.", HttpStatus.CONFLICT);
        }

        try {
            visitRepository.save(visit);
        } catch (Exception e) {
            throw new RuntimeException("An unexpected error occurred while saving visit", e);
        }

        return new ResponseEntity<>("Visit successfully created", HttpStatus.OK);
    }

    @Override
    @Cacheable(value = "patientVisitDataCache", key = """
            T(String).valueOf(#page) +
            T(String).valueOf(#size) +
            T(String).valueOf(#search != null ? #search : 'null') +
            T(String).valueOf(#doctorIds != null ? #doctorIds : 'null')
            """)
    public List<PatientVisitDTO> findPatientsOnPageWithLastVisits(int page, int size, String search, String doctorIds) {

        List<FindPatientsAndLastVisitsDTO> patients = visitRepository.findPatientsAndLastVisits(page, size, search, doctorIds);

        HashMap<Integer, PatientVisitDTO> result = new HashMap<>();

        for (FindPatientsAndLastVisitsDTO patient : patients) {

            // Handle patient with no visits
            if (patient.doctorTimeZone() == null || patient.doctorTimeZone().isEmpty()) {
                result.computeIfAbsent(patient.patientID(), id -> new PatientVisitDTO(
                        patient.patientFirstName(),
                        patient.patientLastName(),
                        new ArrayList<>()));
            } else {
                int doctorTime = getTimezoneOffsetInMinutes(patient.doctorTimeZone());

                LocalDateTime visitStart = patient.visitStart().toLocalDateTime().plusMinutes(doctorTime);
                LocalDateTime visitEnd = patient.visitEnd().toLocalDateTime().plusMinutes(doctorTime);

                String formattedVisitStart = visitStart.format(TimeVariablesValidator.formatter);
                String formattedVisitEnd = visitEnd.format(TimeVariablesValidator.formatter);

                String doctorFirstName = patient.doctorFirstName();
                String doctorLastName = patient.doctorLastName();
                long totalPatients = patient.totalPatients();

                DoctorDTO doctor = new DoctorDTO(doctorFirstName, doctorLastName, (int) totalPatients);
                VisitDTO visit = new VisitDTO(formattedVisitStart, formattedVisitEnd, doctor);

                // If patient already exists, add visit; otherwise, create a new entry
                result.computeIfAbsent(patient.patientID(), id -> new PatientVisitDTO(
                        patient.patientFirstName(),
                        patient.patientLastName(),
                        new ArrayList<>())
                ).getLastVisits().add(visit);
            }
        }

        return new ArrayList<>(result.values());
    }

    @Override
    @Cacheable(value = "patientVisitCountCache", key = "T(String).valueOf(#search != null ? #search : 'null') + T(String).valueOf(#doctorIds != null ? #doctorIds : 'null')")
    public int countResults(String search, String doctorIds) {
        return visitRepository.countResults(search, doctorIds);
    }

    public static String convertSearch(String search) {
        if (search == null || search.trim().isEmpty()) {
            return null;
        }

        String[] searchTerms = search.trim().split("\\s+");

        StringBuilder queryString = new StringBuilder();
        for (String term : searchTerms) {
            queryString.append("+").append(term).append(" ");
        }

        return queryString.toString().trim();
    }

    private static int getTimezoneOffsetInMinutes(String timeZoneId) {
        if (timeZoneId == null || timeZoneId.isBlank()) {
            throw new IllegalArgumentException("Time zone ID cannot be null or empty.");
        }

        try {
            ZoneId zoneId = ZoneId.of(timeZoneId);
            ZonedDateTime now = ZonedDateTime.now(zoneId);
            return now.getOffset().getTotalSeconds() / 60;
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("Invalid time zone ID: " + timeZoneId, e);
        }
    }


//    public static String convertDoctorIdsToString(String doctorIds) {
//        if (doctorIds == null || doctorIds.isEmpty()) {
//            return null;
//        }
//
//        // Check if the string contains only digits and commas
//        if (isValidDoctorIds(doctorIds)) {
//            return doctorIds;
//        }
//
//        return Arrays.stream(doctorIds.split(","))
//                .map(str -> {
//                    try {
//                        return Integer.parseInt(str.trim());
//                    } catch (NumberFormatException e) {
//                        return null; // Return null if invalid entry
//                    }
//                })
//                .filter(Objects::nonNull) // Filter out invalid entries
//                .map(String::valueOf) // Convert valid integers back to strings
//                .collect(Collectors.joining(","));
//    }
//
//    private static boolean isValidDoctorIds(String doctorIds) {
//        return doctorIds.matches("^[0-9,]+$");
//    }

}
