package edu.careflow.util;

import java.time.LocalDate;
import java.time.Period;

public class DateHelper {

    private static DateHelper instance;

    private DateHelper() {
    }

    public static DateHelper getInstance() {
        if (instance == null) {
            instance = new DateHelper();
        }
        return instance;
    }

    public  int calculateAgeAtDate(String birthDateStr) {
        LocalDate birthDate = LocalDate.parse(birthDateStr);
        LocalDate referenceDate = LocalDate.parse(LocalDate.now().toString());
        return Period.between(birthDate, referenceDate).getYears();
    }
}