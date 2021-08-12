package grpc.service;

import java.util.List;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import com.students_information.stubs.result.Grade;
import com.students_information.stubs.result.ResultCreateRequest;
import com.students_information.stubs.result.ResultCreateResponse;
import com.students_information.stubs.result.ResultDeleteRequest;
import com.students_information.stubs.result.ResultDeleteResponse;
import com.students_information.stubs.result.ResultListAllRequest;
import com.students_information.stubs.result.ResultListResponse;
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
import tenant.InvalidTenantException;
import value.StudentPK;

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
            creatingResult.setSchoolId(request.getSchoolId());
            creatingResult.setStudentId(request.getStudentId());
            Result result = resultService.create(creatingResult);
            ResultCreateResponse resultResponse = ResultCreateResponse.newBuilder()
                                                .setSchoolId(result.getSchoolId())
                                                .setStudentId(result.getStudentId())
                                                .build();
            responseObserver.onNext(resultResponse);
            responseObserver.onCompleted();
        }//try
        catch (NoSuchElementException e) {
            log.error("NO RESULT FOUND WITH THE STUDENT ID :- " + studentId);
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
        StudentPK studentPK = new StudentPK(schoolId, studentId);
        try {
            Result result = resultService.read(studentPK);
            ResultReadResponse.Builder builder = ResultReadResponse.newBuilder()
                                                .setSchoolId(schoolId)
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
        catch (InvalidTenantException e) {
            log.error("INVALID TENANT ID :- " + e.getInvalidTenantId());
            responseObserver.onError(Status.INVALID_ARGUMENT.asException());
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
            updatingResult.setSchoolId(request.getSchoolId());
            Result result = resultService.update(updatingResult);
            ResultUpdateResponse resultResponse = ResultUpdateResponse.newBuilder()
                                                .setSchoolId(result.getSchoolId())
                                                .setStudentId(result.getStudentId())
                                                .build();
            responseObserver.onNext(resultResponse);
            responseObserver.onCompleted();
        }//try
        catch (NoSuchElementException e) {
            log.error("NO RESULT FOUND WITH THE STUDENT ID :- " + studentId);
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
        StudentPK studentPK = new StudentPK(schoolId, studentId);
        try {
            long deletedCount = resultService.delete(studentPK);
            ResultDeleteResponse resultResponse = ResultDeleteResponse.newBuilder()
                                                .setDeletedCount(deletedCount).build();
            responseObserver.onNext(resultResponse);
            responseObserver.onCompleted();
        }//try
        catch (NoSuchElementException e) {
            log.error("NO RESULT FOUND WITH THE STUDENT ID :- " + studentId);
            responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
        }//catch
        catch (InvalidTenantException e) {
            log.error("INVALID TENANT ID :- " + e.getInvalidTenantId());
            responseObserver.onError(Status.INVALID_ARGUMENT.asException());
        }//catch
    }

    @Override
    public void listAll(ResultListAllRequest request, StreamObserver<ResultListResponse> responseObserver) {
        ResultListResponse.Builder builder = ResultListResponse.newBuilder();
        List<Result> results = resultService.listAll();
        results.stream()
            .forEach(result -> builder.addResults(
                                            ResultReadResponse.newBuilder()
                                                .setArt(Grade.valueOf(result.getArt()))
                                                .setChemistry(Grade.valueOf(result.getChemistry()))
                                                .setMaths(Grade.valueOf(result.getMaths()))
                                                .setSchoolId(result.getSchoolId())
                                                .setStudentId(result.getStudentId())
                                                .build()
                                        )
                                );
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

}
