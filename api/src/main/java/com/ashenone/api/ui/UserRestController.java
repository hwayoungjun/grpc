package com.ashenone.api.ui;

import com.ashenone.api.domain.Address;
import com.ashenone.api.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserRestController {

    @GetMapping("/{userIdx}")
    public ResponseEntity<User> getUser(@PathVariable Long userIdx) {
//        log.info("Getting user with idx: {}", userIdx);
        User user = new User(
            1,
            "John Doe",
            "john.doe@example.com",
            30,
            "+1234567890",
            new Address("123 Main St", "Springfield", "IL", "62701", "USA"),
            List.of("sports", "music", "travel")
        );
        return ResponseEntity.ok(user);
    }
}
