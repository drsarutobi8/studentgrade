package studentgrade.app;

import java.io.IOException;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import studentgrade.app.service.ResultServiceImpl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResultServer {
    public static void main(String[] args) {
        Server server = ServerBuilder.forPort(9080).addService(new ResultServiceImpl()).build();

        try {
            server.start();
            log.info("RESULT SERVER STARTED ON PORT 9080");

            // This awaitTermination method will help to remain the server, otherwise the
            // server will shutdown quickly
            server.awaitTermination();
        } catch (IOException e) {
            log.error("RESULT SERVER DID NOT START");
        } catch (InterruptedException e) {
            log.error("RESULT SERVER SHUT DOWN ON INTERRUPTED");
        }
    }
}
