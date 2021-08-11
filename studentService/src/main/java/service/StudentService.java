package service;

import java.util.List;
import java.util.NoSuchElementException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;

import dao.StudentDao;
import domain.Student;
import grpc.interceptor.BearerAuthHolder;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class StudentService {

    @Inject
    BearerAuthHolder authHolder;

    @Inject
    StudentDao studentDao;

    public Uni<Student> create(@Valid Student student) {
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

    public Uni<Student> update(@Valid Student student) {
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

    public Uni<List<Student>> listAll() {
        log.info("listing All");
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
        }//if
        Uni<List<Student>> studentListUni = studentDao.listAll();
        return studentListUni;
    }

}
