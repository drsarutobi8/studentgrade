package com.students_information.result.service;

import com.students_information.common.grpc.interceptor.BearerAuthHolder;
import com.students_information.common.tenant.InvalidTenantException;
import com.students_information.common.tenant.TenantValidator;
import com.students_information.common.value.DeletedEntity;
import com.students_information.result.domain.Result;
import com.students_information.result.domain.ResultPK;

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
        log.info("creating resultPK=".concat(creating.getPK().toString()));
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
    public Result read(ResultPK resultPK) throws InvalidTenantException {
        log.info("reading resultPK=".concat(resultPK.toString()));
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
            TenantValidator.validate(authHolder.getTenantId(), resultPK);
        }//if
        return Result.findById(resultPK);
    }

    @Transactional
    public Result update(Result updating) throws InvalidTenantException {
        log.info("updating resultPK=".concat(updating.getPK().toString()));

        ResultPK resultPK = new ResultPK(authHolder.getTenantId(),updating.getStudentId());
        Result updatingResult = Result.findById(resultPK);
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
    public DeletedEntity delete(ResultPK resultPK) throws InvalidTenantException {
        log.info("deleting resultPK=".concat(resultPK.toString()));
        DeletedEntity deletedEntity = new DeletedEntity();
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
            TenantValidator.validate(authHolder.getTenantId(), resultPK);
            deletedEntity.setDeleteId(authHolder.getAccessToken().getPreferredUsername());
        }//if
        deletedEntity.setDeleteTime(new Timestamp(System.currentTimeMillis()));
        boolean deleted = Result.deleteById(resultPK);
        if (!deleted) {
            throw new IllegalStateException("Cannot delete resultPK=".concat(resultPK.toString()));
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
