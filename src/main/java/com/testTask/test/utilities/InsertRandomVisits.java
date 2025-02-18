package com.testTask.test.utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.Date;

public class InsertRandomVisits {

    private static final String JDBC_URL = "jdbc:mysql://localhost:3308/hospital";
    private static final String USERNAME = "test_hospital";
    private static final String PASSWORD = "test_password";

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "INSERT INTO visit (doctor_id, patient_id, start_date_time, end_date_time) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);

            Random rand = new Random();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

            for (int i = 0; i < 30000; i++) {
                int doctorId = rand.nextInt(999) + 1;
                int patientId = rand.nextInt(4999) + 1;

                long currentTime = System.currentTimeMillis();
                long randomTime = currentTime + (rand.nextInt(7 * 24 * 60 * 60 * 1000));
                Date startDate = new Date(randomTime);
                String startTimeStr = sdf.format(startDate);

                long endTime = randomTime + (10 * 60 * 1000); // add 10 minutes
                Date endDate = new Date(endTime);
                String endTimeStr = sdf.format(endDate);


                statement.setInt(1, doctorId);
                statement.setInt(2, patientId);
                statement.setString(3, startTimeStr);
                statement.setString(4, endTimeStr);


                statement.addBatch();

                // Execute batch every 5000 records to avoid memory overload
                if (i % 5000 == 0 && i != 0) {
                    statement.executeBatch();
                }
            }

            statement.executeBatch();
            System.out.println("Successfully inserted 30,000 random visits!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

