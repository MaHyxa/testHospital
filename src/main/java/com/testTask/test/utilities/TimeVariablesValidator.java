package com.testTask.test.utilities;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeVariablesValidator implements ConstraintValidator<ValidateTimeVariables, Object> {
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    public void initialize(ValidateTimeVariables constraintAnnotation) {
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            // Use reflection to get the "start" and "end" fields from the DTO object
            Field startField = value.getClass().getDeclaredField("start");
            Field endField = value.getClass().getDeclaredField("end");

            startField.setAccessible(true);
            endField.setAccessible(true);

            String start = (String) startField.get(value);
            String end = (String) endField.get(value);

            if(start == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Start time is required")
                        .addPropertyNode("start")
                        .addConstraintViolation();
                return false;
            }

            if(end == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("End time is required")
                        .addPropertyNode("end")
                        .addConstraintViolation();
                return false;
            }

            LocalDateTime startDateTime;
            LocalDateTime endDateTime;

            try {
                startDateTime = LocalDateTime.parse(start, formatter);
            } catch (Exception e) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Start time must be in the correct format: yyyy-MM-dd'T'HH:mm:ss")
                        .addPropertyNode("start")
                        .addConstraintViolation();
                return false;
            }

            try {
                endDateTime = LocalDateTime.parse(end, formatter);
            } catch (Exception e) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("End time must be in the correct format: yyyy-MM-dd'T'HH:mm:ss")
                        .addPropertyNode("end")
                        .addConstraintViolation();
                return false;
            }

            // Check if end time is after start time
            if (endDateTime.isBefore(startDateTime) || endDateTime.isEqual(startDateTime)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("End time must be after start time")
                        .addPropertyNode("end")
                        .addConstraintViolation();
                return false;
            }

            return true;

        } catch (Exception e) {
            return false;
        }
    }
}
