package com.ashenone.api.config;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;

@Configuration
public class GrpcServerConfig {

    @Bean
    public Server grpcServer(List<BindableService> services) {
        ServerBuilder<?> serverBuilder = ServerBuilder.forPort(9090);
        services.forEach(serverBuilder::addService);
        Server server = serverBuilder.build();

        try {
            server.start();
            System.out.println("gRPC 서버가 포트 9090에서 시작되었습니다.");

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