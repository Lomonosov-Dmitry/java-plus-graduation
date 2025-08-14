package ru.practicum.client;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.grpc.stats.event.UserActionControllerGrpc;
import ru.practicum.grpc.stats.event.UserActionProto;

@Component
public class CollectorClient {
    @GrpcClient("collector")
    UserActionControllerGrpc.UserActionControllerBlockingStub collector;

    public void newUserAction(UserActionProto actionProto) {
        collector.collectUserAction(actionProto);
    }
}
