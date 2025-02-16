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

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Primary
@Service
@RequiredArgsConstructor
public class VisitServiceImpl implements VisitService {

    private final PatientRepository patientRepository;
    private final VisitRepository visitRepository;

    @Override
    public ResponseEntity<?> createVisit(VisitRequestDTO visitRequest) {

        Visit visit = new Visit();
        Integer patientId = patientRepository.findPatientIdById(visitRequest.getPatientId());
        LocalDateTime start = LocalDateTime.parse(visitRequest.getStart());
        LocalDateTime end = LocalDateTime.parse(visitRequest.getEnd());
        int doctorId = visitRequest.getDoctorId();

        if(patientId != null ) {
            Patient patient = new Patient();
            patient.setId(patientId);
            visit.setPatient(patient);
        }
        else return new ResponseEntity<>("Unable to create visit. Selected patient wasn't found", HttpStatus.NOT_FOUND);

        var checkVisits = visitRepository.checkDoctorExistingVisits(start, end, doctorId);

        Object[] data = checkVisits.get(0);
        long visitCount = ((Number) data[0]).longValue();
        Short docTimeZone = (data[1] != null) ? ((Number) data[1]).shortValue() : null;

        if(docTimeZone != null) {
            Doctor doctor = new Doctor();
            doctor.setId(doctorId);
            visit.setDoctor(doctor);
        }
        else {
            return new ResponseEntity<>("Unable to create visit. Selected Doctor wasn't found", HttpStatus.NOT_FOUND);
        }

        if(visitCount == 0) {
            if(start.plusMinutes(docTimeZone).isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
                return new ResponseEntity<>("Unable to create visit. Visit can not be in past.", HttpStatus.CONFLICT);
            }
            else {
                visit.setStartDateTime(start.plusMinutes(docTimeZone));
                visit.setEndDateTime(end.plusMinutes(docTimeZone));
            }
        }
        else {
            return new ResponseEntity<>("Unable to create visit. Selected time is occupied, please choose another time.", HttpStatus.CONFLICT);
        }

        visitRepository.save(visit);
        return new ResponseEntity<>("Visit successfully created", HttpStatus.OK);
    }

    @Override
//    @Cacheable(value = "patientVisitDataCache", key = """
//                 T(String).valueOf(#page) +
//                 T(String).valueOf(#size) +
//                 T(String).valueOf(#search != null ? #search : 'null') +
//                 T(String).valueOf(#doctorIds != null ? #doctorIds : 'null')
//                 """)
    public List<PatientVisitDTO> findPatientsOnPage(int page, int size, String search, String doctorIds) {

        List<Object[]> patients = visitRepository.findPatientsOnPage(page, size, search, doctorIds);

        //List for second query
        List<Integer> patientIdsList = new ArrayList<>();

        HashMap<Integer, PatientVisitDTO> result = new HashMap<>();

        for (Object[] patient : patients) {
            Integer patientId = (Integer) patient[0];
            String firstName = (String) patient[1];
            String lastName = (String) patient[2];

            patientIdsList.add(patientId);
            result.put(patientId, new PatientVisitDTO(firstName, lastName, Collections.emptyList()));
        }

        //Collect Visits for selected patients and put their visit to PatientVisitDTO
        Map<Integer, List<VisitDTO>> visitMap = collectVisitsForPatient(patientIdsList, doctorIds);

        for (Map.Entry<Integer, List<VisitDTO>> entry : visitMap.entrySet()) {
            Integer patientId = entry.getKey();
            List<VisitDTO> visits = entry.getValue();

            if (result.containsKey(patientId)) {
                result.get(patientId).setLastVisits(visits);
            }
        }

        return new ArrayList<>(result.values());
    }

    @Override
    @Cacheable(value = "patientVisitCountCache", key = "T(String).valueOf(#search != null ? #search : 'null') + T(String).valueOf(#doctorIds != null ? #doctorIds : 'null')")
    public int countResults(String search, String doctorIds) {
        return visitRepository.countResults(search, doctorIds);
    }

    private Map<Integer, List<VisitDTO>> collectVisitsForPatient(List<Integer> patientIdsList, String doctorIds) {
        List<Object[]> results = visitRepository.collectVisitsForPatients(patientIdsList, doctorIds);

        Map<Integer, List<VisitDTO>> patientVisitsMap = new HashMap<>();

        for (Object[] result : results) {
            Integer patientId = (Integer) result[0];
            Timestamp visitStartTimestamp = (Timestamp) result[1];
            Timestamp visitEndTimestamp = (Timestamp) result[2];
            Short doctorTime = (Short) result[5];

            LocalDateTime visitStart = visitStartTimestamp.toLocalDateTime();
            LocalDateTime visitEnd = visitEndTimestamp.toLocalDateTime();

            visitStart = visitStart.minusMinutes(doctorTime);
            visitEnd = visitEnd.minusMinutes(doctorTime);

            String formattedVisitStart = visitStart.format(TimeVariablesValidator.formatter);
            String formattedVisitEnd = visitEnd.format(TimeVariablesValidator.formatter);

            String doctorFirstName = (String) result[3];
            String doctorLastName = (String) result[4];
            int totalPatients = ((Number) result[6]).intValue();

            DoctorDTO doctor = new DoctorDTO(doctorFirstName, doctorLastName, totalPatients);
            VisitDTO visit = new VisitDTO(formattedVisitStart, formattedVisitEnd, doctor);

            // Add visit to the correct patient's list in the map
            patientVisitsMap.computeIfAbsent(patientId, k -> new ArrayList<>()).add(visit);
        }

        return patientVisitsMap;
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
