package com.ashenone.api.ui;

import com.ashenone.api.domain.Course;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/json")
public class JsonController {

    @PostMapping(value = "/receive", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Course> receive(@RequestBody Course course) {
//        log.info("Course received: {}", course);
        return ResponseEntity.ok(course);
    }
}
