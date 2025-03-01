package com.ashenone.api.config;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.sample.proto.SimpleGrpc;

import java.io.IOException;

@Configuration
public class GrpcServerConfigurer {

    @Bean
    public Server grpcServer(SimpleGrpc.SimpleImplBase simpleService) {  // 서비스 구현체를 주입받음
        Server server = ServerBuilder.forPort(9090)
                .addService(simpleService)
                .build();

        // 서버 시작
        try {
            server.start();
            System.out.println("gRPC 서버가 포트 9090에서 시작되었습니다.");

            // Shutdown Hook 등록
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("*** gRPC 서버 종료 ***");
                server.shutdown();
            }));

        } catch (IOException e) {
            throw new RuntimeException("gRPC 서버 시작 실패", e);
        }

        return server;
    }
}