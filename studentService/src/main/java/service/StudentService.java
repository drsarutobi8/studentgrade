package service;

import java.util.NoSuchElementException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.students_information.stubs.result.Grade;
import com.students_information.stubs.result.MutinyResultServiceGrpc;
import com.students_information.stubs.result.ResultReadRequest;
import com.students_information.stubs.result.ResultReadResponse;

import dao.StudentDao;
import domain.Student;
import grpc.interceptor.BearerAuthHolder;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import value.StudentInfo;

@ApplicationScoped
@Slf4j
public class StudentService {

    @Inject
    StudentDao studentDao;

    @Inject
    BearerAuthHolder authHolder;

    public Uni<Student> create(Student student) {
        log.info("creating studentId=".concat(student.getStudentId()));

        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
        }//if
        return Panache.withTransaction(() -> studentDao.persist(student));
    }

    public Uni<Student> read(String studentId) {
        log.info("reading studentId=".concat(studentId));
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
        }//if
        Uni<Student> studentUni = studentDao.findByStudentId(studentId); // Let's find the student information from the student table
        return studentUni;
    }

    public Uni<Student> update(Student student) {
        log.info("updating studentId=".concat(student.getStudentId()));
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
        }//if

        return Panache.withTransaction(() -> studentDao.findByStudentId(student.getStudentId())
                                            .onItem()
                                                .ifNotNull()
                                                    .invoke(st -> {
                                                            st.setAge(student.getAge());
                                                            st.setGender(student.getGender());
                                                            st.setName(student.getName());
                                                        })
                                        )
                                        .onItem()
                                            .ifNull()
                                                .failWith(new NoSuchElementException("Unknown Student with studentId=".concat(student.getStudentId())));
    }

    public Uni<Long> delete(String studentId) {
        log.info("deleting studentId=".concat(studentId));
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
        }//if

        return Panache.withTransaction(() -> studentDao.deleteByStudentId(studentId))
                .onItem()
                    .ifNull()
                        .failWith(new NoSuchElementException("Unknown Student with studentId=".concat(studentId)));
    }

    @GrpcClient("result")
    MutinyResultServiceGrpc.MutinyResultServiceStub resultClient;

    public Uni<StudentInfo> getInfo(String studentId) {
        log.info("getting info studentId=".concat(studentId));
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
        }//if
        Uni<Student> studentUni = read(studentId);
        Uni<StudentInfo> studentInfoUni = studentUni
                                            .onItem()
                                                .ifNull()
                                                    .failWith(new UnknownStudentServiceException(studentId))
                                            .onItem()
                                                .ifNotNull()
                                                    .transformToUni(student -> 
                                                        resultClient.read(prepareResultRequest(student))
                                                            .onItem()
                                                                .transformToUni(resultRes -> prepareStudentInfo(student, resultRes))
                                                            .onFailure()
                                                                .recoverWithItem(StudentInfo.builder()
                                                                    .age(student.getAge())
                                                                    .gender(student.getGender())
                                                                    .name(student.getName())
                                                                    .studentId(student.getStudentId())
                                                                    .art(Grade.UNKNOWN.name())
                                                                    .maths(Grade.UNKNOWN.name())
                                                                    .chemistry(Grade.UNKNOWN.name())
                                                                    .build())
                                                    );
        return studentInfoUni;
    }

    private static ResultReadRequest prepareResultRequest(Student student) {
        return ResultReadRequest.newBuilder().setStudentId(student.getStudentId()).build();
    }

    private static Uni<StudentInfo> prepareStudentInfo(Student student, ResultReadResponse resultResponse) {
        return Uni.createFrom().item(
            StudentInfo.builder()
            .studentId(student.getStudentId())
            .name(student.getName())
            .age(student.getAge())
            .gender(student.getGender())
            .maths(resultResponse.getMaths().toString())
            .art(resultResponse.getArt().toString())
            .chemistry(resultResponse.getChemistry().toString())
            .build());
    }

}
