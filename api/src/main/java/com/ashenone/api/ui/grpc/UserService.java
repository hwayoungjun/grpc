package com.ashenone.api.ui.grpc;

import com.ashenone.proto.Address;
import com.ashenone.proto.User;
import com.ashenone.proto.UserRequest;
import com.ashenone.proto.UserServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class UserService extends UserServiceGrpc.UserServiceImplBase {

    @Override
    public void getUser(UserRequest request, StreamObserver<User> responseObserver) {
//        log.info("Getting user with id: {}", request.getId());
        User user = User.newBuilder()
            .setId(1)
            .setName("John Doe")
            .setEmail("john.doe@example.com")
            .setAge(30)
            .setPhoneNumber("+1234567890")
            .setAddress(Address.newBuilder()
                .setStreet("123 Main St")
                .setCity("Springfield")
                .setState("IL")
                .setPostalCode("62701")
                .setCountry("USA")
                .build())
            .addAllPreferences(List.of("sports", "music", "travel"))
            .build();
        responseObserver.onNext(user);
        responseObserver.onCompleted();
    }
}
