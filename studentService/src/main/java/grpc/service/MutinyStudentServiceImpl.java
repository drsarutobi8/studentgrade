package grpc.service;

import javax.inject.Inject;

import com.students_information.stubs.student.Gender;
import com.students_information.stubs.student.Grade;
import com.students_information.stubs.student.MutinyStudentServiceGrpc;
import com.students_information.stubs.student.StudentRequest;
import com.students_information.stubs.student.StudentResponse;

import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import service.StudentService;
import value.StudentInfo;

@GrpcService
@Slf4j
public class MutinyStudentServiceImpl extends MutinyStudentServiceGrpc.StudentServiceImplBase {

    @Inject
    StudentService studentService;

    // We have to override the getStudentInfo that was defined in the StudentService class
    // The StudentService class is an autogenerated class by the proto file
    // So, let's override the getStudentInfo method here.
    @Override
    public Uni<StudentResponse> getStudentInfo(StudentRequest request) {
        String studentId = request.getStudentId();// the student ID should be passed with the request message
        log.info("start grpcService.getStudentInfo studentId=".concat(studentId));    
        Uni<StudentInfo> studentInfoUni = studentService.getInfo(studentId);
        Uni<StudentResponse> response = studentInfoUni
                                            .onItem()
                                            .transformToUni(info -> Uni.createFrom().item(
                                                StudentResponse.newBuilder()
                                                .setStudentId(info.getStudentId())
                                                .setName(info.getName())
                                                .setAge(info.getAge())
                                                .setGender(Gender.valueOf(info.getGender()))
                                                .setMaths(Grade.valueOf(info.getMaths()))
                                                .setArt(Grade.valueOf(info.getArt()))
                                                .setChemistry(Grade.valueOf(info.getChemistry()))
                                                .build()
                                            ));
        return response;
    }
}
