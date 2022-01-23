package com.students_information.result.grpc.service;

import java.util.NoSuchElementException;

import javax.inject.Inject;

import com.students_information.common.tenant.InvalidTenantException;
import com.students_information.result.grpc.controller.ResultController;
import com.students_information.result.stubs.ResultCreateRequest;
import com.students_information.result.stubs.ResultCreateResponse;
import com.students_information.result.stubs.ResultDeleteRequest;
import com.students_information.result.stubs.ResultDeleteResponse;
import com.students_information.result.stubs.ResultListAllRequest;
import com.students_information.result.stubs.ResultListResponse;
import com.students_information.result.stubs.ResultReadRequest;
import com.students_information.result.stubs.ResultReadResponse;
import com.students_information.result.stubs.ResultServiceGrpc;
import com.students_information.result.stubs.ResultUpdateRequest;
import com.students_information.result.stubs.ResultUpdateResponse;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.quarkus.grpc.GrpcService;
import io.smallrye.common.annotation.Blocking;
import lombok.extern.slf4j.Slf4j;

@GrpcService
@Blocking
@Slf4j
public class ResultServiceImpl extends ResultServiceGrpc.ResultServiceImplBase {

    @Inject
    ResultController resultController;

    @Override
    public void create(ResultCreateRequest request, StreamObserver<ResultCreateResponse> responseObserver) {
        String schoolId = request.getSchoolId();
        String studentId = request.getStudentId();
        try {
            ResultCreateResponse resultResponse = resultController.create(request);
            responseObserver.onNext(resultResponse);
            responseObserver.onCompleted();
        }//try
        catch (NoSuchElementException e) {
            log.error("NO RESULT FOUND WITH THE SCHOOL ID " + schoolId + ", STUDENT ID :- " + studentId);
            responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
        }//catch
        catch (InvalidTenantException e) {
            log.error("INVALID TENANT ID :- " + e.getInvalidTenantId());
            responseObserver.onError(Status.INVALID_ARGUMENT.asException());
        }//catch
    }

    @Override
    public void read(ResultReadRequest request, StreamObserver<ResultReadResponse> responseObserver) {
        String schoolId = request.getSchoolId();
        String studentId = request.getStudentId();
        try {
            ResultReadResponse resultResponse = resultController.read(request);
            responseObserver.onNext(resultResponse);
            responseObserver.onCompleted();
        }//try
        catch (NoSuchElementException e) {
            log.error("NO RESULT FOUND WITH THE SCHOOL ID " + schoolId + ", STUDENT ID :- " + studentId);
            responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
        }//catch
        catch (InvalidTenantException e) {
            log.error("INVALID TENANT ID :- " + e.getInvalidTenantId());
            responseObserver.onError(Status.INVALID_ARGUMENT.asException());
        }//catch
    }

    @Override
    public void update(ResultUpdateRequest request, StreamObserver<ResultUpdateResponse> responseObserver) {
        String schoolId = request.getSchoolId();
        String studentId = request.getStudentId();
        try {
            ResultUpdateResponse resultResponse = resultController.update(request);
            responseObserver.onNext(resultResponse);
            responseObserver.onCompleted();
        }//try
        catch (NoSuchElementException e) {
            log.error("NO RESULT FOUND WITH THE SCHOOL ID " + schoolId + ", STUDENT ID :- " + studentId);
            responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
        }//catch
        catch (InvalidTenantException e) {
            log.error("INVALID TENANT ID :- " + e.getInvalidTenantId());
            responseObserver.onError(Status.INVALID_ARGUMENT.asException());
        }//catch
    }

    @Override
    public void delete(ResultDeleteRequest request, StreamObserver<ResultDeleteResponse> responseObserver) {
        String schoolId = request.getSchoolId();
        String studentId = request.getStudentId();
        try {
            ResultDeleteResponse resultResponse = resultController.delete(request);
            responseObserver.onNext(resultResponse);
            responseObserver.onCompleted();
        }//try
        catch (NoSuchElementException e) {
            log.error("NO RESULT FOUND WITH THE SCHOOL ID " + schoolId + ", STUDENT ID :- " + studentId);
            responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
        }//catch
        catch (InvalidTenantException e) {
            log.error("INVALID TENANT ID :- " + e.getInvalidTenantId());
            responseObserver.onError(Status.INVALID_ARGUMENT.asException());
        }//catch
    }

    @Override
    public void listAll(ResultListAllRequest request, StreamObserver<ResultListResponse> responseObserver) {
        ResultListResponse listResponse = resultController.listAll(request);
        responseObserver.onNext(listResponse);
        responseObserver.onCompleted();
    }

}
