package com.students_information.student.service;

import com.students_information.common.grpc.interceptor.BearerAuthHolder;
import com.students_information.common.tenant.InvalidTenantException;
import com.students_information.common.tenant.TenantValidator;
import com.students_information.common.value.StudentPK;
import com.students_information.student.domain.Student;

import java.util.List;
import java.util.NoSuchElementException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class StudentService {

    @Inject
    BearerAuthHolder authHolder;

    public Uni<Student> create(@Valid Student student) throws InvalidTenantException {
        log.info("creating studentId=".concat(student.getStudentId()));

        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
            TenantValidator.validate(authHolder.getTenantId(), student);
        }//if

        return Panache.withTransaction(() -> student.persist());
    }

    public Uni<Student> read(StudentPK studentPK) throws InvalidTenantException {
        log.info("reading studentId=".concat(studentPK.getStudentId()));
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
            TenantValidator.validate(authHolder.getTenantId(), studentPK);
        }//if
        return Student.findBySchoolIdStudentId(studentPK.getSchoolId(), studentPK.getStudentId());
    }

    public Uni<Student> update(@Valid Student student) throws InvalidTenantException {
        log.info("updating schoolId=".concat(student.getSchoolId()));
        log.info("updating studentId=".concat(student.getStudentId()));
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
            TenantValidator.validate(authHolder.getTenantId(), student);
        }//if

        return Panache.withTransaction(() -> Student.findBySchoolIdStudentId(student.getSchoolId(), student.getStudentId())
                                            .onItem()
                                                .ifNotNull()
                                                    .invoke(st -> {
                                                            st.setAge(student.getAge());
                                                            st.setGender(student.getGender());
                                                            st.setName(student.getName());
                                                            st.persist();
                                                        })
                                        )
                                        .onItem()
                                            .ifNull()
                                                .failWith(new NoSuchElementException("Unknown Student with studentPK=".concat(student.getPK().toString())));
    }

    public Uni<Long> delete(StudentPK studentPK) throws InvalidTenantException {
        log.info("deleting schoolId=".concat(studentPK.getSchoolId()));
        log.info("deleting studentId=".concat(studentPK.getStudentId()));
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
            TenantValidator.validate(authHolder.getTenantId(), studentPK);
        }//if

        return Panache.withTransaction(() -> Student.deleteBySchoolIdStudentId(studentPK.getSchoolId(), studentPK.getStudentId()))
                                        .onItem()
                                            .ifNull()
                                                .failWith(new NoSuchElementException("Unknown Student with studentId=".concat(studentPK.getStudentId())));
    }

    public Uni<List<Student>> listAll() {
        log.info("listing All");
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
            if (authHolder.getTenantId()!=null) {
                return Student.findBySchooldId(authHolder.getTenantId());
            }//if
        }//if
        return Student.listAll();
    }

}