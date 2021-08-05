package grpc.service;

import java.util.NoSuchElementException;

import javax.inject.Inject;

import com.students_information.stubs.result.Grade;
import com.students_information.stubs.result.ResultCreateRequest;
import com.students_information.stubs.result.ResultCreateResponse;
import com.students_information.stubs.result.ResultDeleteRequest;
import com.students_information.stubs.result.ResultDeleteResponse;
import com.students_information.stubs.result.ResultReadRequest;
import com.students_information.stubs.result.ResultReadResponse;
import com.students_information.stubs.result.ResultServiceGrpc;
import com.students_information.stubs.result.ResultUpdateRequest;
import com.students_information.stubs.result.ResultUpdateResponse;

import domain.Result;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.quarkus.grpc.GrpcService;
import io.smallrye.common.annotation.Blocking;
import lombok.extern.slf4j.Slf4j;
import service.ResultService;

@GrpcService
@Blocking
@Slf4j
public class ResultServiceImpl extends ResultServiceGrpc.ResultServiceImplBase {

    @Inject
    ResultService resultService;

    @Override
    public void create(ResultCreateRequest request, StreamObserver<ResultCreateResponse> responseObserver) {
        String studentId = request.getStudentId();
        try {
            Result creatingResult = new Result();
            creatingResult.setArt(request.getArt().toString());
            creatingResult.setChemistry(request.getChemistry().toString());
            creatingResult.setMaths(request.getMaths().toString());
            creatingResult.setStudentId(request.getStudentId());
            Result result = resultService.create(creatingResult);
            ResultCreateResponse resultResponse = ResultCreateResponse.newBuilder()
                                                .setStudentId(result.getStudentId())
                                                .build();
            responseObserver.onNext(resultResponse);
            responseObserver.onCompleted();
        }//try
        catch (NoSuchElementException e) {
            log.error("NO RESULT FOUND WITH THE STUDENT ID :- " + studentId);
            responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
        }//catch
    }

    @Override
    public void read(ResultReadRequest request, StreamObserver<ResultReadResponse> responseObserver) {
        String studentId = request.getStudentId();
        try {
            Result result = resultService.read(studentId);
            ResultReadResponse.Builder builder = ResultReadResponse.newBuilder()
                                                .setStudentId(studentId);
            if (result!=null) {
                builder.setMaths(Grade.valueOf(result.getMaths()))
                    .setArt(Grade.valueOf(result.getArt()))
                    .setChemistry(Grade.valueOf(result.getChemistry()));
            }//if
            ResultReadResponse resultResponse = builder.build();
            responseObserver.onNext(resultResponse);
            responseObserver.onCompleted();
        }//try
        catch (NoSuchElementException e) {
            log.error("NO RESULT FOUND WITH THE STUDENT ID :- " + studentId);
            responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
        }//catch
    }

    @Override
    public void update(ResultUpdateRequest request, StreamObserver<ResultUpdateResponse> responseObserver) {
        String studentId = request.getStudentId();
        try {
            Result updatingResult = new Result();
            updatingResult.setArt(request.getArt().toString());
            updatingResult.setChemistry(request.getChemistry().toString());
            updatingResult.setMaths(request.getMaths().toString());
            updatingResult.setStudentId(request.getStudentId());
            Result result = resultService.update(updatingResult);
            ResultUpdateResponse resultResponse = ResultUpdateResponse.newBuilder()
                                                .setStudentId(result.getStudentId())
                                                .build();
            responseObserver.onNext(resultResponse);
            responseObserver.onCompleted();
        }//try
        catch (NoSuchElementException e) {
            log.error("NO RESULT FOUND WITH THE STUDENT ID :- " + studentId);
            responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
        }//catch
    }

    public void delete(ResultDeleteRequest request, StreamObserver<ResultDeleteResponse> responseObserver) {
        String studentId = request.getStudentId();
        try {
            long deletedCount = resultService.delete(studentId);
            ResultDeleteResponse resultResponse = ResultDeleteResponse.newBuilder()
                                                .setDeletedCount(deletedCount).build();
            responseObserver.onNext(resultResponse);
            responseObserver.onCompleted();
        }//try
        catch (NoSuchElementException e) {
            log.error("NO RESULT FOUND WITH THE STUDENT ID :- " + studentId);
            responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
        }//catch
    }
}
