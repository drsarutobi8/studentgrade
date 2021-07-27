package service;

//import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import dao.StudentDao;
import domain.Student;
//import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

//import org.eclipse.microprofile.jwt.JsonWebToken;

@ApplicationScoped
//@RolesAllowed({"teacher"})
@Slf4j
public class StudentService {

//    @Inject
//    SecurityIdentity securityIdentity;

    @Inject
    StudentDao studentDao;

    public Uni<Student> read(String studentId) {
        log.info("reading by studentId=".concat(studentId));
        Uni<Student> studentUni = studentDao.findByStudentId(studentId); // Let's find the student information from the student table
        return studentUni;
    }
}
