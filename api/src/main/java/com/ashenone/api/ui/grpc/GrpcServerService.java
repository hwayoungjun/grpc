package com.ashenone.api.ui.grpc;


import io.grpc.stub.StreamObserver;
import org.springframework.grpc.sample.proto.HelloReply;
import org.springframework.grpc.sample.proto.HelloRequest;
import org.springframework.grpc.sample.proto.SimpleGrpc;
import org.springframework.stereotype.Service;

@Service
public class GrpcServerService extends SimpleGrpc.SimpleImplBase {

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        HelloReply reply = HelloReply.newBuilder()
                .setMessage("안녕하세요, " + request.getName())
                .build();
        responseObserver.onNext(reply);    // 단 한 번만 호출
        responseObserver.onCompleted();

    }

    @Override
    public void streamHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        // 여러 번의 응답 전송 가능
        responseObserver.onNext(HelloReply.newBuilder().setMessage("첫번째 응답").build());
        responseObserver.onNext(HelloReply.newBuilder().setMessage("두번째 응답").build());
        responseObserver.onNext(HelloReply.newBuilder().setMessage("세번째 응답").build());
        responseObserver.onCompleted();

    }
}