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
import com.students_information.student.dao.ResultDao;
import com.students_information.student.domain.Result;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class ResultService {

    @Inject
    BearerAuthHolder authHolder;

    @Inject
    ResultDao resultDao;

    public Uni<Result> create(@Valid Result student) throws InvalidTenantException {
        log.info("creating studentId=".concat(student.getStudentId()));

        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
            TenantValidator.validate(authHolder.getTenantId(), student);
        }//if

        return Panache.withTransaction(() -> resultDao.persist(student));
    }

    public Uni<Result> read(StudentPK studentPK) throws InvalidTenantException {
        log.info("reading studentId=".concat(studentPK.getStudentId()));
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
            TenantValidator.validate(authHolder.getTenantId(), studentPK);
        }//if
        return resultDao.findBySchoolIdStudentId(studentPK.getSchoolId(), studentPK.getStudentId());
    }

    public Uni<Result> update(@Valid Result result) throws InvalidTenantException {
        log.info("updating schoolId=".concat(result.getSchoolId()));
        log.info("updating studentId=".concat(result.getStudentId()));
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
            TenantValidator.validate(authHolder.getTenantId(), result);
        }//if

        return Panache.withTransaction(() -> resultDao.findBySchoolIdStudentId(result.getSchoolId(), result.getStudentId())
                                            .onItem()
                                                .ifNotNull()
                                                    .invoke(rs -> {
                                                            rs.setArt(result.getArt());
                                                            rs.setChemistry(result.getChemistry());
                                                            rs.setMaths(result.getMaths());
                                                        })
                                        )
                                        .onItem()
                                            .ifNull()
                                                .failWith(new NoSuchElementException("Unknown Student with studentPK=".concat(result.getPK().toString())));
    }

    public Uni<Long> delete(StudentPK studentPK) throws InvalidTenantException {
        log.info("deleting schoolId=".concat(studentPK.getSchoolId()));
        log.info("deleting studentId=".concat(studentPK.getStudentId()));
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
            TenantValidator.validate(authHolder.getTenantId(), studentPK);
        }//if

        return Panache.withTransaction(() -> resultDao.deleteBySchoolIdStudentId(studentPK.getSchoolId(), studentPK.getStudentId()))
                                        .onItem()
                                            .ifNull()
                                                .failWith(new NoSuchElementException("Unknown Student with studentId=".concat(studentPK.getStudentId())));
    }

    public Uni<List<Result>> listAll() {
        log.info("listing All");
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
            if (authHolder.getTenantId()!=null) {
                return resultDao.findBySchooldId(authHolder.getTenantId());
            }//if
        }//if
        return resultDao.listAll();
    }

    public Uni<Map<StudentPK,Result>> mapAll() {
        log.info("mapAll");
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
            if (authHolder.getTenantId()!=null) {
                return resultDao.findBySchooldId(authHolder.getTenantId())
                            .onItem().transform(listAll -> listAll.stream().collect(
                                Collectors.toMap(
                                    Result::getPK, Function.identity(), (existing, replacement) -> existing)));
            }//if
        }//if
        return resultDao.listAll()
                    .onItem().transform(listAll -> listAll.stream().collect(
                        Collectors.toMap(
                            Result::getPK, Function.identity(), (existing, replacement) -> existing)));
    }

}