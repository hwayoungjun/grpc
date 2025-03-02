package com.ashenone.api.domain;

import java.util.List;

public record User(
        long id,
        String name,
        String email,
        int age,
        String phoneNumber,
        Address address,
        List<String> preferences
) {}

