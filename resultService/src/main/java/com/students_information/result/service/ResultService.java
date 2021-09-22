package com.students_information.result.service;

import com.students_information.common.grpc.interceptor.BearerAuthHolder;
import com.students_information.common.tenant.InvalidTenantException;
import com.students_information.common.tenant.TenantValidator;
import com.students_information.common.value.DeletedEntity;
import com.students_information.common.value.StudentPK;

import com.students_information.result.domain.Result;
import org.hibernate.service.spi.InjectService;
import org.hibernate.sql.Delete;

import java.sql.Timestamp;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.NoSuchElementException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class ResultService {

    @Inject
    BearerAuthHolder authHolder;

    @Transactional
    public Result create(Result creating) throws InvalidTenantException {
        log.info("creating studentPK=".concat(creating.getPK().toString()));
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
            TenantValidator.validate(authHolder.getTenantId(), creating);

            //FORCE USING CREATE USER ID FROM TOKEN
            creating.setCreateId(authHolder.getAccessToken().getPreferredUsername());
        }//if
        creating.setCreateTime(new java.sql.Timestamp(System.currentTimeMillis()));

        creating.persist();
        return creating;
    }

    @Transactional
    public Result read(StudentPK studentPK) throws InvalidTenantException {
        log.info("reading studentPK=".concat(studentPK.toString()));
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
            TenantValidator.validate(authHolder.getTenantId(), studentPK);
        }//if
        return Result.findById(studentPK);
    }

    @Transactional
    public Result update(Result updating) throws InvalidTenantException {
        log.info("updating studentPK=".concat(updating.getPK().toString()));

        StudentPK studentPK = new StudentPK(authHolder.getTenantId(),updating.getStudentId());
        Result updatingResult = Result.findById(studentPK);
        if (updatingResult==null) {
            throw new NoSuchElementException("Unknown Result with studentId=".concat(updating.getStudentId()));
        }//if
        else {
            if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
                log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
                TenantValidator.validate(authHolder.getTenantId(), updating);
                //FORCE USING CREATE USER ID FROM TOKEN
                updatingResult.setUpdateId(authHolder.getAccessToken().getPreferredUsername());
            }//if
            updatingResult.setArt(updating.getArt());
            updatingResult.setChemistry(updating.getChemistry());
            updatingResult.setMaths(updating.getMaths());
            updatingResult.setUpdateTime(new java.sql.Timestamp(System.currentTimeMillis()));
            updatingResult.persist();
        }//else

        return updatingResult;
    }

    @Transactional
    public DeletedEntity delete(StudentPK studentPK) throws InvalidTenantException {
        log.info("deleting studentPK=".concat(studentPK.toString()));
        DeletedEntity deletedEntity = new DeletedEntity();
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
            TenantValidator.validate(authHolder.getTenantId(), studentPK);
            deletedEntity.setDeleteId(authHolder.getAccessToken().getPreferredUsername());
        }//if
        deletedEntity.setDeleteTime(new Timestamp(System.currentTimeMillis()));
        boolean deleted = Result.deleteById(studentPK);
        if (!deleted) {
            throw new IllegalStateException("Cannot delete studentPK=".concat(studentPK.toString()));
        }//if
        deletedEntity.setDeletedCount(1l);
        return deletedEntity;
    }

    @Transactional
    public List<Result> listAll() {
        log.info("listing All");
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
            log.info("by tenantId=".concat(authHolder.getTenantId()));
            return Result.findBySchooldId(authHolder.getTenantId());
        }//if
        List<Result> resultList = Result.listAll();
        return resultList;
    }

}
