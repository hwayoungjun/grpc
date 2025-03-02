package com.ashenone.api;

import com.ashenone.api.config.WebClientTestConfig;
import com.ashenone.proto.User;
import com.ashenone.proto.UserRequest;
import com.ashenone.proto.UserServiceGrpc;
import com.google.common.util.concurrent.AtomicDouble;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(classes = WebClientTestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ApiPerformanceTest {

    @Autowired
    private WebClient webClient;

    private UserServiceGrpc.UserServiceBlockingStub blockingStub;

    private static final int THREAD_COUNT = 100;
    private static final int TEST_ROUNDS = 20;
    private static final int WARMUP_ROUNDS = 3;

    @BeforeAll
    public void setUp() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
            .usePlaintext()
            .build();
        blockingStub = UserServiceGrpc.newBlockingStub(channel);
    }

//    @Test
    public double grpc() throws InterruptedException {
        UserRequest userRequest = UserRequest.newBuilder().setId("1").build();

        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        AtomicDouble totalDuration = new AtomicDouble();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, THREAD_COUNT)
                .forEach(i -> executor.submit(() -> {
                    long start = System.nanoTime();
                    User user = blockingStub.getUser(userRequest);
                    long end = System.nanoTime();
                    latch.countDown();
                    totalDuration.addAndGet((end - start) / 1_000_000.0);
                }));
            latch.await();
            double averageDurationInMillis = totalDuration.get() / THREAD_COUNT;
            System.out.println("[gRPC] Average response time: " + String.format("%.2f", averageDurationInMillis) + " ms");
            return averageDurationInMillis;
        }
    }

//    @Test
    public double rest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        AtomicDouble totalDuration = new AtomicDouble();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, THREAD_COUNT)
                .forEach(i -> executor.submit(() -> {
                    ResponseEntity<com.ashenone.api.domain.User> response = webClient.get()
                        .uri("http://localhost:8080/api/user/1")
                        .retrieve()
                        .toEntity(com.ashenone.api.domain.User.class)
                        .transformDeferred(mono -> {
                            long startTime = System.nanoTime();
                            return mono.doOnTerminate(() -> {
                                long duration = System.nanoTime() - startTime;
                                totalDuration.addAndGet(duration / 1_000_000.0);
                                latch.countDown();
                            });
                        })
                        .block();
                }));
            latch.await();
            double averageDurationInMillis = totalDuration.get() / THREAD_COUNT;
            System.out.println("[REST] Average response time: " + String.format("%.2f", averageDurationInMillis) + " ms");
            return averageDurationInMillis;

        }
    }

    @Test
    public void compare_grpc_rest() throws InterruptedException {
        List<Double> grpcResults = new ArrayList<>();
        List<Double> restResults = new ArrayList<>();

        System.out.println("웜업 시작");
        for (int i = 0; i < WARMUP_ROUNDS; i++) {
            grpc();
            rest();
            Thread.sleep(100);
        }
        System.out.println("웜업 완료\n");


        for (int i = 0; i < TEST_ROUNDS; i++) {
            Thread.sleep(1000);
            System.out.println("\n실행 #" + (i + 1));
            grpcResults.add(grpc());
            restResults.add(rest());
        }

        printStatistics("gRPC", grpcResults);
        printStatistics("REST", restResults);
    }

    private void printStatistics(String testName, List<Double> results) {
        double avg = results.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double min = results.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        double max = results.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);

        System.out.println("\n" + testName + " 통계:");
        System.out.printf("  평균 응답 시간: %.2f ms%n", avg);
        System.out.printf("  최소 응답 시간: %.2f ms%n", min);
        System.out.printf("  최대 응답 시간: %.2f ms%n", max);
    }
}
