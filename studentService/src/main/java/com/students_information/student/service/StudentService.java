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
        log.info("creating student=".concat(student.toString()));

        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
            log.info("by tenantId=".concat(authHolder.getTenantId()));
            TenantValidator.validate(authHolder.getTenantId(), student);

            student.setCreateId(authHolder.getAccessToken().getPreferredUsername());
            student.setCreateTime(new java.sql.Timestamp(System.currentTimeMillis()));
            student.setSchoolId(authHolder.getTenantId());
        }//if

        return Panache.withTransaction(() -> student.persist());
    }

    public Uni<Student> read(StudentPK studentPK) throws InvalidTenantException {
        log.info("reading studentPK=".concat(studentPK.toString()));
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
            TenantValidator.validate(authHolder.getTenantId(), studentPK);
        }//if
        return Student.findById(studentPK);
    }

    public Uni<Student> update(@Valid Student student) throws InvalidTenantException {
        log.info("updating student=".concat(student.toString()));
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
            TenantValidator.validate(authHolder.getTenantId(), student);

            student.setUpdateId(authHolder.getAccessToken().getPreferredUsername());
            student.setUpdateTime(new java.sql.Timestamp(System.currentTimeMillis()));
        }//if

        return Panache.withTransaction(() -> Student.findById(student.getPK())
                                            .onItem()
                                                .ifNotNull()
                                                    .invoke(stObj -> 
                                                        {
                                                             Student st = (Student)stObj;
                                                             st.copy(student);
                                                             //st.setAge(student.getAge());
                                                             //st.setGender(student.getGender());
                                                             //st.setName(student.getName());
                                                             st.setUpdateId(student.getUpdateId());
                                                             st.setUpdateTime(student.getUpdateTime());
                                                             st.persist();
                                                         }
                                                        )
                                        )
                                         .onItem()
                                             .ifNotNull()
                                                 .transformToUni(st -> Uni.createFrom().item((Student)st))
                                         .onItem()
                                             .ifNull()
                                                 .failWith(new NoSuchElementException("Unknown Student with studentPK=".concat(student.getPK().toString())));
    }

    public Uni<Boolean> delete(StudentPK studentPK) throws InvalidTenantException {
        log.info("deleting studentPK=".concat(studentPK.toString()));
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
            TenantValidator.validate(authHolder.getTenantId(), studentPK);
        }//if

        return Panache.withTransaction(() -> Student.deleteById(studentPK))
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