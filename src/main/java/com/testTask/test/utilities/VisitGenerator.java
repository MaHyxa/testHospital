package com.testTask.test.utilities;

import com.testTask.test.visit.VisitRequestDTO;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class VisitGenerator {
    public static VisitRequestDTO generate() {
        Random rand = new Random();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        VisitRequestDTO vr = new VisitRequestDTO();

        vr.setDoctorId(rand.nextInt(999) + 1);

        vr.setPatientId(rand.nextInt(999) + 1);

        long currentTime = System.currentTimeMillis();
        long randomTime = currentTime + (rand.nextInt(7 * 24 * 60 * 60 * 1000)); // random time within 7 days

        Date startDate = new Date(randomTime);
        vr.setStart(sdf.format(startDate));

        long endTime = randomTime + (10 * 60 * 1000); // add 10 minutes
        Date endDate = new Date(endTime);
        vr.setEnd(sdf.format(endDate));
        return vr;
    }
}
