package com.testTask.test.visit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class VisitServiceTest {

    @Mock
    private VisitRepository visitRepository;

    @InjectMocks
    private VisitService visitService;

    @Test
    void shouldCreateVisitSuccessfully() {
        // Arrange: Create a VisitRequestDTO (example data)
        VisitRequestDTO visitRequest = new VisitRequestDTO();
        // Set up your visitRequest object as needed

        // Arrange: Create a Visit entity that will be saved
        Visit visit = new Visit();
        // Set up your visit object as needed (e.g. visit.setPatientId(...))

        // Mock the repository behavior
        when(visitRepository.save(any(Visit.class))).thenReturn(visit);

        // Act: Call the method to test
        ResponseEntity<?> response = visitService.createVisit(visitRequest);

        // Assert: Verify repository interaction and response
        verify(visitRepository, times(1)).save(any(Visit.class));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Visit successfully created", response.getBody());
    }

    @Test
    void shouldHandleNullVisitRequest() {
        // Act: Call the method with null (if null handling is supported)
        ResponseEntity<?> response = visitService.createVisit(null);

        // Assert: Depending on your handling of nulls, assert the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid request", response.getBody());
    }

    // Add more test cases for edge cases and error handling as needed

    @Test
    void createVisit() {
    }

    @Test
    void findPatientsOnPage() {
    }

    @Test
    void countResults() {
    }
}