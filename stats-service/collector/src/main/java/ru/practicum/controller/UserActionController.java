package ru.practicum.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.grpc.stats.event.UserActionControllerGrpc;
import ru.practicum.grpc.stats.event.UserActionProto;

@GrpcService
public class UserActionController extends UserActionControllerGrpc.UserActionControllerImplBase {
    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        try {
            // здесь реализуется бизнес-логика
            // ...

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }
}
