package studentgrade.app.service;

import javax.inject.Inject;
import java.util.NoSuchElementException;

import io.quarkus.grpc.GrpcService;

import studentgrade.gprc.result.Grade;
import studentgrade.gprc.result.ResultRequest;
import studentgrade.gprc.result.ResultResponse;
import studentgrade.gprc.result.ResultServiceGrpc;
import studentgrade.gprc.result.MutinyResultServiceGrpc;

import studentgrade.app.entity.Result;
import studentgrade.app.entity.ResultRepository;

import io.smallrye.mutiny.Uni;

import lombok.extern.slf4j.Slf4j;

@GrpcService
@Slf4j
public class ResultServiceImpl extends MutinyResultServiceGrpc.ResultServiceImplBase {

    @Inject
    ResultRepository resultRepo;

    @Override
    public Uni<ResultResponse> getResultForStudent(ResultRequest request) {
        String studentId = request.getStudentId(); // the student ID should be passed with the request message

        try {
            Uni<Result> resultUni = resultRepo.findById(studentId); // Use the dao class to retrieve data

            Result result = resultUni.await().indefinitely();
            /*
                In gRPC everything we create according to the builder pattern,
                here we have to generate the response message,
                in order to create that response message we use the response builder
                and then set the values for that, 
            */
            ResultResponse resultResponse = ResultResponse.newBuilder()
                    .setStudentId(studentId)
                    .setMaths(Grade.valueOf(result.getMaths()))
                    .setArt(Grade.valueOf(result.getArt()))
                    .setChemistry(Grade.valueOf(result.getChemistry()))
                    .build();

            return Uni.createFrom().item(resultResponse);
            
        } catch (NoSuchElementException e) {
            log.error("NO RESULT FOUND WITH THE STUDENT ID :- " + studentId);
            throw e;
        }

    }

}
