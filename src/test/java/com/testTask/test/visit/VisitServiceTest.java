package com.testTask.test.visit;

import com.testTask.test.patient.PatientRepository;
import com.testTask.test.patient.PatientVisitDTO;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
public class VisitServiceTest {

    @Autowired
    private MockMvc mockMvc;
    @Mock
    private VisitRepository visitRepository;
    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private VisitServiceImpl visitService;

    @Test
    void shouldCreateVisitSuccessfully() {
        // Example data
        VisitRequestDTO visitRequest = new VisitRequestDTO("2025-04-16T16:00:54", "2025-04-16T16:01:54", 10, 10);

        Visit visit = new Visit();

        Object[] mockExistingVisits = new Object[]{0, 5};
        when(visitRepository.checkDoctorExistingVisits(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                anyInt()
        )).thenReturn(Collections.singletonList(mockExistingVisits));

        when(visitRepository.save(any(Visit.class))).thenReturn(visit);

        ResponseEntity<?> response = visitService.createVisit(visitRequest);

        verify(visitRepository, times(1)).save(any(Visit.class));
        verify(patientRepository, times(1)).findPatientIdById(visitRequest.getPatientId());
        verify(visitRepository, times(1)).checkDoctorExistingVisits(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                anyInt());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Visit successfully created", response.getBody());
    }

    @Test
    void shouldReturnNotFound_WhenPatientWasNotFoundInDB() {
        // Example data
        VisitRequestDTO visitRequest = new VisitRequestDTO("2025-04-16T16:00:54", "2025-04-16T16:01:54", 10, 10);

        when(patientRepository.findPatientIdById(10)).thenReturn(null);

        ResponseEntity<?> response = visitService.createVisit(visitRequest);

        verify(patientRepository, times(1)).findPatientIdById(visitRequest.getPatientId());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Unable to create visit. Selected patient wasn't found", response.getBody());
    }

    @Test
    void shouldFindPatientSuccessfully() {
        // Example data
        VisitRequestDTO visitRequest = new VisitRequestDTO("2025-04-16T16:00:54", "2025-04-16T16:01:54", 10, 10);

        Object[] mockExistingVisits = new Object[]{5L, (short) 3};
        when(visitRepository.checkDoctorExistingVisits(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                anyInt()
        )).thenReturn(Collections.singletonList(mockExistingVisits));
        when(patientRepository.findPatientIdById(10)).thenReturn(10);

        visitService.createVisit(visitRequest);

        verify(patientRepository, times(1)).findPatientIdById(visitRequest.getPatientId());

        Integer foundPatientId = patientRepository.findPatientIdById(visitRequest.getPatientId());
        assertNotNull(foundPatientId);
        assertEquals(10, foundPatientId);
    }

    @Test
    void shouldReturnNotFound_WhenDoctorWasNotFoundInDB() {
        // Example data
        VisitRequestDTO visitRequest = new VisitRequestDTO("2025-04-16T16:00:54", "2025-04-16T16:01:54", 10, 10);

        Object[] mockExistingVisits = new Object[]{0L, null};
        when(visitRepository.checkDoctorExistingVisits(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                anyInt()
        )).thenReturn(Collections.singletonList(mockExistingVisits));

        ResponseEntity<?> response = visitService.createVisit(visitRequest);

        verify(visitRepository, times(1)).checkDoctorExistingVisits(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                anyInt());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Unable to create visit. Selected Doctor wasn't found", response.getBody());
    }

    @Test
    void shouldReturnConflict_WhenTimeIsOccupied() {
        // Example data
        VisitRequestDTO visitRequest = new VisitRequestDTO("2025-04-16T16:00:54", "2025-04-16T16:01:54", 10, 10);

        Object[] mockExistingVisits = new Object[]{10, 5};
        when(visitRepository.checkDoctorExistingVisits(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                anyInt()
        )).thenReturn(Collections.singletonList(mockExistingVisits));

        ResponseEntity<?> response = visitService.createVisit(visitRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Unable to create visit. Selected time is occupied, please choose another time.", response.getBody());
    }


    @Test
    void shouldHandleNullPatientId() {

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            visitService.createVisit(new VisitRequestDTO("2025-04-16T16:00:54", "2025-04-16T16:01:54", null, null));
        });

        assertEquals("Patient ID cannot be null", thrown.getMessage());
    }

    @Test
    void shouldHandleNullDoctorId() {

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            visitService.createVisit(new VisitRequestDTO("2025-04-16T16:00:54", "2025-04-16T16:01:54", 5, null));
        });

        assertEquals("Doctor ID cannot be null", thrown.getMessage());
    }

    @Test
    void shouldCheckProperDateTimeFormat() {

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            visitService.createVisit(new VisitRequestDTO("2025-99-16T16:00:54", "2025-99-16T16:01:54", 5, 4));
        });

        assertEquals("Time must be in the correct format: yyyy-MM-dd'T'HH:mm:ss", thrown.getMessage());
    }


    @Test
    void shouldHandleNullVisitRequest() {

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> visitService.createVisit(null));

        assertEquals("Visit request cannot be null", thrown.getMessage());
    }

    @Test
    void shouldReturnBadRequest_WhenVisitRequestIsNull() throws Exception {
        mockMvc.perform(post("/api/hospital/createVisit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("")) // Sending null
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid data format or value out of range"));
    }

    @Test
    void shouldReturnBadRequest_WhenVisitRequestIsEmpty() throws Exception {
        mockMvc.perform(post("/api/hospital/createVisit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")) // Sending empty JSON
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"start\": \"Start time is required\", \"end\": \"End time is required\", \"patientId\": \"Incorrect Patient ID\", \"doctorId\": \"Incorrect Doctor ID\"}"));
    }

    @Test
    void shouldReturnBadRequest_WhenVisitRequestHasInvalidData() throws Exception {
        mockMvc.perform(post("/api/hospital/createVisit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"34b4\": \"rtbdd'T'HH:mm:ss\", \"435c\": \"ertbert\"}")) // Sending random JSON
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"start\": \"Start time is required\", \"end\": \"End time is required\", \"patientId\": \"Incorrect Patient ID\", \"doctorId\": \"Incorrect Doctor ID\"}"));
    }

    @Test
    void shouldReturnBadRequest_WhenVisitRequestStartDateHasInvalidFormat() throws Exception {
        mockMvc.perform(post("/api/hospital/createVisit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"start\": \"2025-99-19T14:01:54\"}")) // Sending wrong start date
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"end\": \"End time is required\", \"patientId\": \"Incorrect Patient ID\", \"doctorId\": \"Incorrect Doctor ID\"}"));
    }

    @Test
    void shouldReturnBadRequest_WhenVisitRequestStartDateIsMissing() throws Exception {
        mockMvc.perform(post("/api/hospital/createVisit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"end\": \"2025-02-19T14:01:54\"}")) // Missing start date
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"start\": \"Start time is required\", \"patientId\": \"Incorrect Patient ID\", \"doctorId\": \"Incorrect Doctor ID\"}"));

    }

    @Test
    void shouldReturnBadRequest_WhenVisitRequestEndDateIsMissing() throws Exception {
        mockMvc.perform(post("/api/hospital/createVisit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"start\": \"2025-04-19T14:01:54\"}")) // Missing end date
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"end\": \"End time is required\", \"patientId\": \"Incorrect Patient ID\", \"doctorId\": \"Incorrect Doctor ID\"}"));
    }

    @Test
    void shouldReturnBadRequest_WhenVisitRequestEndDateHasInvalidFormat() throws Exception {
        mockMvc.perform(post("/api/hospital/createVisit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"start\": \"2025-04-19T14:01:54\", \"end\": \"2025-99-19T14:01:54\"}")) // Wrong end date
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"end\": \"End time must be in the correct format: yyyy-MM-dd'T'HH:mm:ss\", \"patientId\": \"Incorrect Patient ID\", \"doctorId\": \"Incorrect Doctor ID\"}"));
    }

    @Test
    void shouldReturnBadRequest_WhenVisitRequestStartDateBeforeEndDate() throws Exception {
        mockMvc.perform(post("/api/hospital/createVisit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"start\": \"2025-04-19T18:01:54\", \"end\": \"2025-04-19T14:01:54\"}")) // Start date is ahead of end date
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"end\": \"End time must be after start time\", \"patientId\": \"Incorrect Patient ID\", \"doctorId\": \"Incorrect Doctor ID\"}"));
    }

    @Test
    void shouldReturnBadRequest_WhenVisitRequestPatientIdHasInvalidFormat() throws Exception {
        mockMvc.perform(post("/api/hospital/createVisit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"start\": \"2025-04-19T18:01:54\", \"end\": \"2025-04-19T14:01:54\", \"patientId\": 789789789789789789, \"doctorId\": 789789789789789789}")) // Patient or Doctor has invalid format
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid data format or value out of range"));
    }

    @Test
    void shouldReturnBadRequest_WhenVisitRequestStartDateIsInPast() throws Exception {
        mockMvc.perform(post("/api/hospital/createVisit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"start\": \"2024-04-19T13:01:54\", \"end\": \"2024-04-19T14:01:54\", \"patientId\": 4, \"doctorId\": 5}")) // Start date is in past
                .andExpect(status().isConflict())
                .andExpect(content().string("Unable to create visit. Visit can not be in past."));
    }

    @Test
    void shouldReturnCorrectCount_WhenValidSearchAndDoctorIdsProvided() {

        String search = "test";
        String doctorIds = "1,2,3";
        when(visitRepository.countResults(search, doctorIds)).thenReturn(5);

        int resultCount = visitService.countResults(search, doctorIds);

        assertEquals(5, resultCount);
        verify(visitRepository, times(1)).countResults(search, doctorIds);
    }

    @Test
    void shouldReturnEmptyList_WhenNoPatientsFound() {

        int page = 0;
        int size = 5;
        String search = "testName";
        String doctorIds = "1,2,3";

        when(visitRepository.findPatientsOnPage(page, size, search, doctorIds)).thenReturn(Collections.emptyList());

        List<PatientVisitDTO> result = visitService.findPatientsOnPage(page, size, search, doctorIds);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldCallVisitRepositoryMethodWithCorrectArguments() {

        int page = 0;
        int size = 5;
        String search = "John";
        String doctorIds = "1,2,3";

        when(visitRepository.findPatientsOnPage(page, size, search, doctorIds)).thenReturn(Collections.emptyList());

        visitService.findPatientsOnPage(page, size, search, doctorIds);

        verify(visitRepository, times(1)).findPatientsOnPage(page, size, search, doctorIds);
    }

}