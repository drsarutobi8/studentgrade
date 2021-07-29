package service;

import com.students_information.stubs.result.MutinyResultServiceGrpc;
import com.students_information.stubs.result.ResultRequest;
import com.students_information.stubs.result.ResultResponse;
import dao.StudentDao;
import domain.Student;
import grpc.ref.Constants;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.AccessToken;
import value.StudentInfo;

@ApplicationScoped
@Slf4j
public class StudentService {

    @Inject
    StudentDao studentDao;

    public Uni<Student> create(Student student) {
        log.info("creating studentId=".concat(student.getStudentId()));
        AccessToken token = Constants.ACCESS_TOKEN_CONTEXT_KEY.get();
        if (token!=null) {
            log.info("by userId=".concat(token.getPreferredUsername()));
        }//if
        return Panache.withTransaction(() -> studentDao.persist(student));
    }

    public Uni<Student> read(String studentId) {
        log.info("reading studentId=".concat(studentId));
        AccessToken token = Constants.ACCESS_TOKEN_CONTEXT_KEY.get();
        if (token!=null) {
            log.info("by userId=".concat(token.getPreferredUsername()));
        }//if
        Uni<Student> studentUni = studentDao.findByStudentId(studentId); // Let's find the student information from the student table
        return studentUni;
    }

    public Uni<Long> delete(String studentId) {
        log.info("deleting studentId=".concat(studentId));
        AccessToken token = Constants.ACCESS_TOKEN_CONTEXT_KEY.get();
        if (token!=null) {
            log.info("by userId=".concat(token.getPreferredUsername()));
        }//if
        return Panache.withTransaction(() -> studentDao.deleteByStudentId(studentId));
    }

    @GrpcClient("result")
    MutinyResultServiceGrpc.MutinyResultServiceStub resultClient;

    public Uni<StudentInfo> getInfo(String studentId) {
        log.info("getting info studentId=".concat(studentId));
        Uni<Student> studentUni = read(studentId);
        Uni<StudentInfo> studentInfoUni = studentUni
                                            .onItem()
                                            .transformToUni(student -> 
                                                resultClient.getResultForStudent(prepareResultRequest(student))
                                                .onItem()
                                                    .transformToUni(resultRes -> prepareStudentInfo(student, resultRes))
                                                .onFailure()
                                                    .recoverWithItem(StudentInfo.builder()
                                                        .age(student.getAge())
                                                        .gender(student.getGender())
                                                        .name(student.getName())
                                                        .studentId(student.getStudentId())
                                                        .build())
                                            );
        return studentInfoUni;
    }

    private static ResultRequest prepareResultRequest(Student student) {
        return ResultRequest.newBuilder().setStudentId(student.getStudentId()).build();
    }

    private static Uni<StudentInfo> prepareStudentInfo(Student student, ResultResponse resultResponse) {
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
