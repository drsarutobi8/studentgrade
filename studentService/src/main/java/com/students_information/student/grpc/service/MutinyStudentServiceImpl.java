package com.students_information.student.grpc.service;

import java.util.List;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import com.students_information.common.tenant.InvalidTenantException;
import com.students_information.common.value.StudentPK;
import com.students_information.result.stubs.MutinyResultServiceGrpc;
import com.students_information.result.stubs.ResultReadRequest;
import com.students_information.result.stubs.ResultReadResponse;
import com.students_information.student.domain.Result;
import com.students_information.student.domain.Student;
import com.students_information.student.service.ResultService;
import com.students_information.student.service.StudentService;
import com.students_information.student.stubs.Gender;
import com.students_information.student.stubs.Grade;
import com.students_information.student.stubs.MutinyStudentServiceGrpc;
import com.students_information.student.stubs.StudentCreateRequest;
import com.students_information.student.stubs.StudentCreateResponse;
import com.students_information.student.stubs.StudentDeleteRequest;
import com.students_information.student.stubs.StudentDeleteResponse;
import com.students_information.student.stubs.StudentInfoListAllRequest;
import com.students_information.student.stubs.StudentInfoListResponse;
import com.students_information.student.stubs.StudentInfoRequest;
import com.students_information.student.stubs.StudentInfoResponse;
import com.students_information.student.stubs.StudentListAllRequest;
import com.students_information.student.stubs.StudentListResponse;
import com.students_information.student.stubs.StudentReadRequest;
import com.students_information.student.stubs.StudentReadResponse;
import com.students_information.student.stubs.StudentUpdateRequest;
import com.students_information.student.stubs.StudentUpdateResponse;
import com.students_information.student.value.StudentInfo;

import io.quarkus.grpc.GrpcClient;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

@GrpcService
@Slf4j
public class MutinyStudentServiceImpl extends MutinyStudentServiceGrpc.StudentServiceImplBase {

    @Inject
    StudentService studentService;
    @Inject
    ResultService resultService;

    @GrpcClient("result")
    MutinyResultServiceGrpc.MutinyResultServiceStub resultClient;

    public static class UnknownResult extends Result {
        public UnknownResult(StudentPK studentPK) {
            setSchoolId(studentPK.getSchoolId());
            setStudentId(studentPK.getStudentId());
            setArt(Grade.UNKNOWN.name());
            setChemistry(Grade.UNKNOWN.name());
            setMaths(Grade.UNKNOWN.name());
        }
    }

    // We have to override the getStudentInfo that was defined in the StudentService class
    // The StudentService class is an autogenerated class by the proto file
    // So, let's override the getStudentInfo method here.
    @Override
    public Uni<StudentInfoResponse> getInfo(StudentInfoRequest request) {
        String schoolId = request.getSchoolId();
        String studentId = request.getStudentId();// the student Id should be passed with the request message
        log.info("start grpcService.getInfo studentId=".concat(studentId));    
        StudentPK studentPK = new StudentPK(schoolId, studentId);
        try {
            Uni<Student> studentUni = studentService.read(studentPK);
            Uni<StudentInfo> studentInfoUni = studentUni
                                                .onItem()
                                                    .ifNull()
                                                        .failWith(new NoSuchElementException())
                                                .onItem()
                                                    .ifNotNull()
                                                        .transformToUni(student -> prepareStudentInfoUni(student));
                                                            Uni<StudentInfoResponse> response = studentInfoUni
                                                                        .onItem()
                                                                            .transformToUni(info -> Uni.createFrom().item(
                                                                                StudentInfoResponse.newBuilder()
                                                                                .setSchoolId(info.getSchoolId())
                                                                                .setStudentId(info.getStudentId())
                                                                                .setName(info.getName())
                                                                                .setAge(info.getAge())
                                                                                .setGender((info.getGender()==null)?null:Gender.valueOf(info.getGender()))
                                                                                .setMaths((info.getMaths()==null)?Grade.UNKNOWN:Grade.valueOf(info.getMaths()))
                                                                                .setArt((info.getArt()==null)?Grade.UNKNOWN:Grade.valueOf(info.getArt()))
                                                                                .setChemistry((info.getChemistry()==null)?Grade.UNKNOWN:Grade.valueOf(info.getChemistry()))
                                                                                .build()
                                                                            )
                                                                        .onFailure()
                                                                            .recoverWithItem(StudentInfoResponse.newBuilder().build()));
            return response;
        }//try
        catch (InvalidTenantException e) {
            e.printStackTrace();
            return Uni.createFrom().item(StudentInfoResponse.newBuilder().build());
        }//catch
    }

    private Uni<StudentInfo> prepareStudentInfoUni(Student student) {
        return resultClient.read(prepareResultReadRequest(student))
        .onItem()
            .transformToUni(resultRes -> prepareStudentInfoUni(student, resultRes))
        .onFailure()
            .recoverWithItem(StudentInfo.builder()
                .age(student.getAge())
                .gender(student.getGender())
                .name(student.getName())
                .schoolId(student.getSchoolId())
                .studentId(student.getStudentId())
                .art(Grade.UNKNOWN.name())
                .maths(Grade.UNKNOWN.name())
                .chemistry(Grade.UNKNOWN.name())
                .build());
    }

    private static ResultReadRequest prepareResultReadRequest(Student student) {
        return ResultReadRequest.newBuilder()
            .setSchoolId(student.getSchoolId())
            .setStudentId(student.getStudentId())
            .build();
    }

    private static Uni<StudentInfo> prepareStudentInfoUni(Student student, ResultReadResponse resultRes) {
        return Uni.createFrom().item(prepareStudentInfo(student, resultRes));
    }

    private static StudentInfo prepareStudentInfo(Student student, ResultReadResponse resultRes) {
        return StudentInfo.builder()
                .schoolId(student.getSchoolId())
                .studentId(student.getStudentId())
                .name(student.getName())
                .age(student.getAge())
                .gender(student.getGender())
                .maths(resultRes.getMaths().toString())
                .art(resultRes.getArt().toString())
                .chemistry(resultRes.getChemistry().toString())
                .build();
    }

    @Override
    public Uni<StudentInfoListResponse> listAllInfo(StudentInfoListAllRequest request) {
        log.info("start grpcService.listAllInfo");    

        Uni<List<Result>> resultsUni = resultService.listAll();
        log.info("before prepare reponseUni");
        Uni<StudentInfoListResponse> responseUni = resultsUni
                                                            .onItem()
                                                                .transformToUni(results -> prepareInfoListResponse(results));
        log.info("done prepare reponseUni");
        return responseUni;
    }

    private Uni<StudentInfoListResponse> prepareInfoListResponse(List<Result> results) {
        StudentInfoListResponse.Builder builder = StudentInfoListResponse.newBuilder();
        if (results!=null && !results.isEmpty()) {
            log.info("results.size=".concat(String.valueOf(results.size())));
            results.stream().
                forEach(result -> 
                    builder.addStudentsInfo(StudentInfoResponse.newBuilder()
                        .setAge(result.getStudent().getAge())
                        .setGender(Gender.valueOf(result.getStudent().getGender()))
                        .setName(result.getStudent().getName())
                        .setSchoolId(result.getSchoolId())
                        .setStudentId(result.getStudentId())
                        .setArt(Grade.valueOf(result.getArt()))
                        .setChemistry(Grade.valueOf(result.getChemistry()))
                        .setMaths(Grade.valueOf(result.getMaths()))
                        .build())
                );
        }//if
        log.info("builder count=".concat(String.valueOf(builder.getStudentsInfoCount())));
        return Uni.createFrom().item(builder.build());
    }

    @Override
    public Uni<StudentCreateResponse> create(StudentCreateRequest request) {
        String studentId = request.getStudentId();
        log.info("start grpcService.create studentId=".concat(studentId));
        Student newStudent = new Student();
        newStudent.setAge(request.getAge());
        newStudent.setGender(request.getGender().toString());
        newStudent.setName(request.getName());
        newStudent.setSchoolId(request.getSchoolId());
        newStudent.setStudentId(request.getStudentId());
        Uni<Student> studentUni;
        try {
            studentUni = studentService.create(newStudent);
            Uni<StudentCreateResponse> response = studentUni
                                                    .onItem()
                                                        .transformToUni(student -> Uni.createFrom().item(
                                                            StudentCreateResponse.newBuilder()
                                                            .setSchoolId(student.getSchoolId())
                                                            .setStudentId(student.getStudentId())
                                                            .build()
                                                        )
                                                    .onFailure()
                                                        .recoverWithItem(StudentCreateResponse.newBuilder().build()));
            return response;
        }//try
        catch (InvalidTenantException e) {
            e.printStackTrace();
            return Uni.createFrom().item(StudentCreateResponse.newBuilder().build());
        }//catch
    }

    @Override
    public Uni<StudentReadResponse> read(StudentReadRequest request) {
        String schoolId = request.getSchoolId();
        String studentId = request.getStudentId();
        log.info("start grpcService.read studentId=".concat(studentId));
        StudentPK studentPK = new StudentPK(schoolId, studentId);
        Uni<Student> studentUni;
        try {
            studentUni = studentService.read(studentPK);
            Uni<StudentReadResponse> response = studentUni
            .onItem()
                .ifNotNull()
                    .transformToUni(student -> Uni.createFrom().item(
                        StudentReadResponse.newBuilder()
                        .setSchoolId(student.getSchoolId())
                        .setStudentId(student.getStudentId())
                        .setName(student.getName())
                        .setAge(student.getAge())
                        .setGender(Gender.valueOf(student.getGender()))
                        .build()
                    )
            .onItem()
                .ifNull()
                    .continueWith(StudentReadResponse.newBuilder().build())
            .onFailure()
                .recoverWithItem(StudentReadResponse.newBuilder().build()));
            return response;
        }//try
        catch (InvalidTenantException e) {
            e.printStackTrace();
            return Uni.createFrom().item(StudentReadResponse.newBuilder().build());
        }//catch
    }

    @Override
    public Uni<StudentUpdateResponse> update(StudentUpdateRequest request) {
        String schoolId = request.getSchoolId();
        String studentId = request.getStudentId();
        log.info("start grpcService.update studentId=".concat(studentId));
        Student updatingStudent = new Student();
        updatingStudent.setAge(request.getAge());
        updatingStudent.setGender(request.getGender().toString());
        updatingStudent.setName(request.getName());
        updatingStudent.setSchoolId(schoolId);
        updatingStudent.setStudentId(studentId);
        try {
            Uni<Student> studentUni = studentService.update(updatingStudent);
            Uni<StudentUpdateResponse> response = studentUni
            .onItem()
                .transformToUni(student -> Uni.createFrom().item(
                    StudentUpdateResponse.newBuilder()
                    .setSchoolId(student.getSchoolId())
                    .setStudentId(student.getStudentId())
                    .build()
                )
            .onFailure()
                .recoverWithItem(StudentUpdateResponse.newBuilder().build()));
            return response;
        }//try
        catch (InvalidTenantException e) {
            e.printStackTrace();
            return Uni.createFrom().item(StudentUpdateResponse.newBuilder().build());
        }//catch
    }

    @Override
    public Uni<StudentDeleteResponse> delete(StudentDeleteRequest request) {
        String schoolId = request.getSchoolId();
        String studentId = request.getStudentId();
        log.info("start grpcService.delete studentId=".concat(studentId));
        StudentPK studentPK = new StudentPK(schoolId, studentId);
        try {
            Uni<Boolean> deletedCountUni = studentService.delete(studentPK);
            Uni<StudentDeleteResponse> response = deletedCountUni
                                                    .onItem()
                                                        .transformToUni(deletedCount -> Uni.createFrom().item(
                                                            StudentDeleteResponse.newBuilder()
                                                            .setDeletedCount((deletedCount)?1:0)
                                                            .build()
                                                        )
                                                    .onFailure()
                                                        .recoverWithItem(StudentDeleteResponse.newBuilder()
                                                                                                .setDeletedCount(0l)
                                                                                                .build()
                                                        ));
            return response;
        }//try
        catch (InvalidTenantException e) {
            e.printStackTrace();
            return Uni.createFrom().item(StudentDeleteResponse.newBuilder().build());
        }//catch
    }
 
    @Override
    public Uni<StudentListResponse> listAll(StudentListAllRequest request) {
        log.info("start grpcService.listAll");    

        Uni<List<Student>> studentsUni = studentService.listAll();
        Uni<StudentListResponse> listReponseUni = studentsUni
                                                        .onItem()
                                                            .transformToUni(students -> prepareListResponse(students));

        return listReponseUni;
    }

    private Uni<StudentListResponse> prepareListResponse(List<Student> students) {
        StudentListResponse.Builder listReponseBuilder = StudentListResponse.newBuilder();
        if (students!=null && !students.isEmpty()) {
            students.stream().
                forEach(student -> 
                    listReponseBuilder.addStudents(StudentReadResponse.newBuilder()
                        .setAge(student.getAge())
                        .setGender(Gender.valueOf(student.getGender()))
                        .setName(student.getName())
                        .setSchoolId(student.getSchoolId())
                        .setStudentId(student.getStudentId())
                        .build())
                );
        }//if
        return Uni.createFrom().item(listReponseBuilder.build());
    }
}