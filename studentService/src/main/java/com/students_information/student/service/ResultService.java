package com.students_information.student.service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;

import com.students_information.common.grpc.interceptor.BearerAuthHolder;
import com.students_information.common.tenant.InvalidTenantException;
import com.students_information.common.tenant.TenantValidator;
import com.students_information.common.value.StudentPK;
import com.students_information.student.domain.Result;
import com.students_information.student.domain.Student;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class ResultService {

    @Inject
    BearerAuthHolder authHolder;

    Uni<Result> create(@Valid Result result) {
        log.info("creating result schooldId=".concat(result.getSchoolId())
                .concat(" studentId=").concat(result.getStudentId()));
        return Panache.withTransaction(result::persist);
     }

    public Uni<Result> read(StudentPK studentPK) throws InvalidTenantException {
        log.info("reading studentId=".concat(studentPK.getStudentId()));
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
            TenantValidator.validate(authHolder.getTenantId(), studentPK);
        }//if
        return Result.findBySchoolIdStudentId(studentPK.getSchoolId(), studentPK.getStudentId());
    }

    Uni<Result> update(@Valid Result result) {
        log.info("updating result schooldId=".concat(result.getSchoolId())
            .concat(" studentId=").concat(result.getStudentId()));
        return Panache.withTransaction(()-> Result.findBySchoolIdStudentId(result.getSchoolId(), result.getStudentId())
                                            .onItem()
                                                .ifNotNull()
                                                    .invoke(rs -> {
                                                            rs.setArt(result.getArt());
                                                            rs.setChemistry(result.getChemistry());
                                                            rs.setMaths(result.getMaths());
                                                            rs.persist();
                                                        })
                                            .onItem()
                                                .ifNull()
                                                    .failWith(
                                                        new NoSuchElementException("Unknown Student with studentPK=".concat(result.getPK().toString()))
                                                    ));
    }

    Uni<Long> delete(StudentPK studentPK) {
        log.info("deleting result schooldId=".concat(studentPK.getSchoolId())
        .concat(" studentId=").concat(studentPK.getStudentId()));

        return Panache.withTransaction(()-> Result.deleteBySchoolIdStudentId(studentPK.getSchoolId(), studentPK.getStudentId()))
                                            .onItem()
                                                .ifNull()
                                                    .failWith(new NoSuchElementException("Unknown Student with studentId=".concat(studentPK.getStudentId())));
    }

    public Uni<List<Result>> listAll() {
        log.info("listing All");
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
            if (authHolder.getTenantId()!=null) {
                return Result.findBySchooldId(authHolder.getTenantId());
            }//if
        }//if
        return Result.listAll();
    }

}