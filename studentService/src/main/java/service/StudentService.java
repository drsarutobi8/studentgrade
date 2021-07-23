package service;

import dao.StudentDao;
import domain.Student;
//import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
//import javax.annotation.security.RolesAllowed;
import io.smallrye.mutiny.Uni;

//import org.eclipse.microprofile.jwt.JsonWebToken;

@ApplicationScoped
//@RolesAllowed({"student","teacher"})
@Slf4j
public class StudentService {
//    @Inject 
//    JsonWebToken jwt;

    @Inject
    StudentDao studentDao;

    public Uni<Student> read(String studentId) {
        Uni<Student> studentUni = studentDao.findByStudentId(studentId); // Let's find the student information from the student table
        return studentUni;
    }
}
