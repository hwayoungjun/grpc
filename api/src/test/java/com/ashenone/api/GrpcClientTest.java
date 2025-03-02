package com.ashenone.api;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.grpc.sample.proto.HelloReply;
import org.springframework.grpc.sample.proto.HelloRequest;
import org.springframework.grpc.sample.proto.SimpleGrpc;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GrpcClientTest {

    @Autowired
    private Server server;

    private ManagedChannel channel;
    private SimpleGrpc.SimpleBlockingStub blockingStub;
    private SimpleGrpc.SimpleStub asyncStub;

    @BeforeAll
    void setUp() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).setLevel(Level.INFO);

        System.out.println("=== gRPC 클라이언트 테스트 시작 ===");
        channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();

        blockingStub = SimpleGrpc.newBlockingStub(channel);
        asyncStub = SimpleGrpc.newStub(channel);
        System.out.println("채널 및 스텁 초기화 완료");
    }

    @AfterAll
    void tearDown() {
        channel.shutdown();
        server.shutdown();
    }

    @Test
    void testSayHello() {
        // Given
        String testName = "테스터";
        System.out.println("\n=== SayHello 테스트 시작 ===");
        System.out.println("요청 이름: " + testName);

        HelloRequest request = HelloRequest.newBuilder()
                .setName(testName)
                .build();

        // When
        System.out.println("요청 전송 중...");
        HelloReply response = blockingStub.sayHello(request);
        System.out.println("응답 수신: " + response.getMessage());

        // Then
        assertNotNull(response);
        assertTrue(response.getMessage().contains(testName));
        System.out.println("테스트 완료 - 성공\n");
    }

    @Test
    void testStreamHello() {
        // Given
        String testName = "스트림테스터";
        System.out.println("\n=== StreamHello 테스트 시작 ===");
        System.out.println("요청 이름: " + testName);

        HelloRequest request = HelloRequest.newBuilder()
                .setName(testName)
                .build();
        List<HelloReply> responses = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        // When
        System.out.println("스트림 요청 전송 중...");
        asyncStub.streamHello(request, new StreamObserver<HelloReply>() {
            @Override
            public void onNext(HelloReply reply) {
                System.out.println("스트림 메시지 수신: " + reply.getMessage());
                responses.add(reply);
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("에러 발생: " + t.getMessage());
                fail("스트리밍 중 에러 발생: " + t.getMessage());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("스트리밍 완료");
                latch.countDown();
            }
        });

        // Then
        try {
            boolean completed = latch.await(5, TimeUnit.SECONDS);
            assertTrue(completed, "스트리밍이 시간 내에 완료되지 않았습니다");
            assertFalse(responses.isEmpty(), "스트림 응답이 비어있습니다");

            System.out.println("총 수신된 메시지: " + responses.size() + "개");
            responses.forEach(reply ->
                    System.out.println("검증된 응답: " + reply.getMessage())
            );

            System.out.println("스트림 테스트 완료 - 성공\n");
        } catch (InterruptedException e) {
            System.err.println("테스트 중단: " + e.getMessage());
            fail("테스트가 중단되었습니다: " + e.getMessage());
        }
    }
}