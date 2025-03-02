package com.ashenone.api;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.ashenone.api.config.WebClientTestConfig;
import com.ashenone.api.domain.Course;
import com.ashenone.api.proto.BaeldungProto;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@SpringBootTest
@ContextConfiguration(classes = WebClientTestConfig.class)
public class DataFormatPerformanceTest {

    @Autowired
    private WebClient webClient;

    private static final int THREAD_COUNT = 2000;

    private static final String PROTOBUF_URL = "http://localhost:8080/protobuf/receive";
    private static final String JSON_URL = "http://localhost:8080/json/receive";

    private static BaeldungProto.Course testProtoCourse;
    private static Course testJsonCourse;

    @BeforeAll
    static void setUp() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).setLevel(Level.INFO);

        // Protobuf 데이터 생성
        BaeldungProto.Student.Builder studentBuilder = BaeldungProto.Student.newBuilder()
                .setId(1)
                .setFirstName("John")
                .setLastName("Doe")
                .setEmail("john.doe@example.com")
                .addPhone(BaeldungProto.Student.PhoneNumber.newBuilder()
                        .setNumber("123456789")
                        .setType(BaeldungProto.Student.PhoneType.MOBILE));

        BaeldungProto.Course.Builder protoCourseBuilder = BaeldungProto.Course.newBuilder()
                .setId(101)
                .setCourseName("Introduction to Protobuf")
                .addStudent(studentBuilder);

        testProtoCourse = protoCourseBuilder.build();

        // JSON 데이터 생성
        Course.Student.PhoneNumber phoneNumber = new Course.Student.PhoneNumber("123456789", Course.Student.PhoneType.MOBILE);
        Course.Student student = new Course.Student(1, "John", "Doe", "john.doe@example.com", List.of(phoneNumber));
        testJsonCourse = new Course(101, "Introduction to Protobuf", List.of(student));
    }

    @Test
    public void protobuf_performance() {
        // Protobuf 실행 및 측정
        ProtobufRequest protobufRequest = new ProtobufRequest(webClient, testProtoCourse);
        long protoStart = System.nanoTime();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, THREAD_COUNT)
                    .forEach(i -> executor.submit(protobufRequest));
        }
        long protoEnd = System.nanoTime();

        System.out.printf("Protobuf 실행 시간: %.2f초%n", (protoEnd - protoStart) / 1_000_000_000.0);
    }

    @Test
    public void json_performance() {
        // JSON 실행 및 측정
        JsonRequest jsonRequest = new JsonRequest(webClient, testJsonCourse);
        long jsonStart = System.nanoTime();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, THREAD_COUNT)
                    .forEach(i -> executor.submit(jsonRequest));
        }
        long jsonEnd = System.nanoTime();

        System.out.printf("JSON 실행 시간: %.2f초%n", (jsonEnd - jsonStart) / 1_000_000_000.0);
    }

    @RequiredArgsConstructor
    static class JsonRequest implements Runnable {
        private final WebClient webClient;
        private final Course course;

        @Override
        public void run() {
            webClient.post()
                    .uri(JSON_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(course)
                    .retrieve()
                    .toEntity(Course.class)
                    .block();
        }
    }

    @RequiredArgsConstructor
    static class ProtobufRequest implements Runnable {
        private final WebClient webClient;
        private final BaeldungProto.Course course;

        private static final MediaType PROTOBUF_MEDIA_TYPE = MediaType.valueOf("application/x-protobuf");

        @Override
        public void run() {
            webClient.post()
                    .uri(PROTOBUF_URL)
                    .contentType(PROTOBUF_MEDIA_TYPE)
                    .bodyValue(course.toByteArray())
                    .retrieve()
                    .toEntity(String.class)
                    .block();
        }
    }
}