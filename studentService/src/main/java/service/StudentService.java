package service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import dao.StudentDao;
import domain.Student;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

import org.keycloak.representations.AccessToken;

import grpc.ref.Constants;

@ApplicationScoped
@Slf4j
public class StudentService {

    @Inject
    StudentDao studentDao;

    public Uni<Student> read(String studentId) {
        log.info("reading by studentId=".concat(studentId));

        log.info("Calling GRPC ACCESS_TOKEN_CONTEXT_KEY");
        AccessToken token = Constants.ACCESS_TOKEN_CONTEXT_KEY.get();
        if (token!=null) {
            log.info("by userId=".concat(token.getPreferredUsername()));
        }//if
        Uni<Student> studentUni = studentDao.findByStudentId(studentId); // Let's find the student information from the student table
        return studentUni;
    }
}
