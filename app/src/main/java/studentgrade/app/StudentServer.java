package studentgrade.app;

import java.io.IOException;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import studentgrade.app.service.StudentServiceImpl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StudentServer {
    public static void main(String[] args) {
        Server server = ServerBuilder.forPort(9081).addService(new StudentServiceImpl()).build();

        try {
            server.start();
            log.info("STUDENT SERVER STARTED ON PORT 9081");

            // This awaitTermination method will help to remain the server, otherwise the
            // server will shutdown quickly
            server.awaitTermination();
        } catch (IOException e) {
            log.error("STUDENT SERVER DID NOT START");
        } catch (InterruptedException e) {
            log.error("STUDENT SERVER SHUT DOWN ON INTERRUPTED");
        }
    }
}
