package com.students_information.student.grpc.controller;

import com.google.protobuf.util.Timestamps;

import com.students_information.common.tenant.InvalidTenantException;
import com.students_information.common.value.StudentPK;
import com.students_information.student.domain.Student;
import com.students_information.student.service.StudentService;
import com.students_information.student.stubs.Gender;
import com.students_information.student.stubs.Grade;
import com.students_information.student.stubs.StudentCreateRequest;
import com.students_information.student.stubs.StudentCreateResponse;
import com.students_information.student.stubs.StudentDeleteRequest;
import com.students_information.student.stubs.StudentDeleteResponse;
import com.students_information.student.stubs.StudentListAllRequest;
import com.students_information.student.stubs.StudentListResponse;
import com.students_information.student.stubs.StudentReadRequest;
import com.students_information.student.stubs.StudentReadResponse;
import com.students_information.student.stubs.StudentUpdateRequest;
import com.students_information.student.stubs.StudentUpdateResponse;

import io.smallrye.mutiny.Uni;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class StudentController {

    @Inject
    StudentService studentService;

    public Uni<StudentCreateResponse> create(StudentCreateRequest request) throws InvalidTenantException {
        log.info("creating schoolId=".concat(request.getSchoolId()).concat(" studentId=").concat(request.getStudentId()));
        Student creating = new Student();
        creating.setAge(request.getAge());
        creating.setGender(request.getGender().name());
        creating.setName(request.getName());
        creating.setSchoolId(request.getSchoolId());
        creating.setStudentId(request.getStudentId());
        try {
            return studentService.create(creating)
                                                    .onItem()
                                                        .transformToUni(student -> 
                                                            Uni.createFrom()
                                                                .item(
                                                                    StudentCreateResponse.newBuilder()
                                                                    .setSchoolId(student.getSchoolId())
                                                                    .setStudentId(student.getStudentId())
                                                                    .setAge(student.getAge())
                                                                    .setGender(Gender.valueOf(student.getGender()))
                                                                    .setName(student.getName())
                                                                    .setCreateId(student.getCreateId())
                                                                    .setCreateTime(Timestamps.fromMillis(student.getCreateTime().getTime()))
                                                                    .build()
                                                                )
                                                                .onFailure()
                                                                    .recoverWithItem(StudentCreateResponse.newBuilder().build())
                                                        );
        }//try
        catch (InvalidTenantException e) {
            log.error(e.getMessage());
            return Uni.createFrom().item(StudentCreateResponse.newBuilder().build());
        }//catch
    }

    public Uni<StudentReadResponse> read(StudentReadRequest request)  throws InvalidTenantException {
        log.info("reading schoolId=".concat(request.getSchoolId()).concat(" studentId=").concat(request.getStudentId()));
        StudentPK studentPK = new StudentPK(request.getSchoolId(), request.getStudentId());
        return studentService.read(studentPK)
                    .onItem()
                        .ifNotNull()
                            .transformToUni(student -> {
                                    StudentReadResponse.Builder builder = StudentReadResponse.newBuilder()
                                        .setSchoolId(student.getSchoolId())
                                        .setStudentId(student.getStudentId())
                                        .setName(student.getName())
                                        .setAge(student.getAge())
                                        .setGender(Gender.valueOf(student.getGender()))
                                        .setCreateId(student.getCreateId())
                                        .setUpdateId(student.getUpdateId());
                                    if (student.getCreateTime()!=null) {
                                        builder.setCreateTime(Timestamps.fromMillis(student.getCreateTime().getTime()));
                                    }//if
                                    if (student.getUpdateTime()!=null) {
                                        builder.setUpdateTime(Timestamps.fromMillis(student.getUpdateTime().getTime()));
                                    }//if
                                    return Uni.createFrom().item(builder.build());
                                }
                            )
                    .onItem()
                        .ifNull()
                            .continueWith(StudentReadResponse.newBuilder().build())
                    .onFailure()
                        .recoverWithItem(StudentReadResponse.newBuilder().build());
    }

    public Uni<StudentUpdateResponse> update(StudentUpdateRequest request) throws InvalidTenantException {
        log.info("updating schoolId=".concat(request.getSchoolId()).concat(" studentId=").concat(request.getStudentId()));
        Student updatingStudent = new Student();
        updatingStudent.setAge(request.getAge());
        updatingStudent.setGender(request.getGender().toString());
        updatingStudent.setName(request.getName());
        updatingStudent.setSchoolId(request.getSchoolId());
        updatingStudent.setStudentId(request.getStudentId());
        return studentService.update(updatingStudent)
                .onItem()
                    .transformToUni(student -> Uni.createFrom().item(
                        StudentUpdateResponse.newBuilder()
                        .setSchoolId(student.getSchoolId())
                        .setStudentId(student.getStudentId())
                        .setAge(student.getAge())
                        .setGender(Gender.valueOf(student.getGender()))
                        .setName(student.getName())
                        .setUpdateId(student.getUpdateId())
                        .setUpdateTime(Timestamps.fromMillis(student.getUpdateTime().getTime()))
                        .build()
                    )
                .onFailure()
                    .recoverWithItem(StudentUpdateResponse.newBuilder().build()));
    }

    public Uni<StudentDeleteResponse> delete(StudentDeleteRequest request) throws InvalidTenantException {
        log.info("deleting schoolId=".concat(request.getSchoolId()).concat(" studentId=").concat(request.getStudentId()));
        StudentPK studentPK = new StudentPK(request.getSchoolId(), request.getStudentId());
        return studentService.delete(studentPK)
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
    }

    public Uni<StudentListResponse> listAll(StudentListAllRequest request) {
        log.info("listAll");    
        return studentService.listAll()
                            .onItem()
                                .transformToUni(students -> prepareListResponse(students));
    }

    private static Uni<StudentListResponse> prepareListResponse(List<Student> students) {
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