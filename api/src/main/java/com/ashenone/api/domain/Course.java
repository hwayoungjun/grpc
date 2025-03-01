package com.ashenone.api.domain;

import java.util.List;

public record Course(
        int id,
        String courseName,
        List<Student> student
) {

    public record Student(
            int id,
            String firstName,
            String lastName,
            String email,
            List<PhoneNumber> phone
    ) {
        public record PhoneNumber(
                String number,
                PhoneType type
        ) {}

        public enum PhoneType {
            MOBILE,
            LANDLINE
        }
    }
}