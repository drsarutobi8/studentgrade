package com.students_information.result.service;

import com.students_information.common.grpc.interceptor.BearerAuthHolder;
import com.students_information.common.tenant.InvalidTenantException;
import com.students_information.common.tenant.TenantValidator;
import com.students_information.common.value.StudentPK;

import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.NoSuchElementException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import com.students_information.result.dao.ResultDao;
import com.students_information.result.domain.Result;

import lombok.extern.slf4j.Slf4j;

//import org.eclipse.microprofile.reactive.messaging.Channel;
//import org.eclipse.microprofile.reactive.messaging.Emitter;
//import org.eclipse.microprofile.reactive.messaging.Message;

@ApplicationScoped
@Slf4j
public class ResultService {

    @Inject
    ResultDao resultDao;

    @Inject
    BearerAuthHolder authHolder;

//    @Inject @Channel("out-grades") Emitter<Result> resultEmitter;
    
    @Transactional
    public Result create(Result result) throws InvalidTenantException {
        log.info("creating studentId=".concat(result.getStudentId()));
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
            TenantValidator.validate(authHolder.getTenantId(), result);
        }//if
        resultDao.persist(result);
        // resultEmitter.send(Message.of(result)
        //     .withAck(() -> {
        //         // Called when the message is acked
        //         return CompletableFuture.completedFuture(null);
        //     })
        //     .withNack(throwable -> {
        //         // Called when the message is nacked
        //         return CompletableFuture.completedFuture(null);
        //     }));
        return result;
    }

    @Transactional
    public Result read(StudentPK studentPK) throws InvalidTenantException {
        log.info("reading studentId=".concat(studentPK.getSchoolId()));
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
            TenantValidator.validate(authHolder.getTenantId(), studentPK);
        }//if
        return resultDao.findBySchoolIdStudentId(studentPK.getSchoolId(), studentPK.getStudentId());
    }

    @Transactional
    public Result update(Result result) throws InvalidTenantException {
        log.info("updating studentId=".concat(result.getStudentId()));
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
            TenantValidator.validate(authHolder.getTenantId(), result);
        }//if

        Result updatingResult = resultDao.findBySchoolIdStudentId(authHolder.getTenantId(),result.getStudentId());
        if (updatingResult==null) {
            throw new NoSuchElementException("Unknown Result with studentId=".concat(result.getStudentId()));
        }//if
        else {
            updatingResult.setArt(result.getArt());
            updatingResult.setChemistry(result.getChemistry());
            updatingResult.setMaths(result.getMaths());
            resultDao.persist(updatingResult);
        }//else
        return updatingResult;
    }

    @Transactional
    public long delete(StudentPK studentPK) throws InvalidTenantException {
        log.info("deleting studentId=".concat(studentPK.getStudentId()));
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
            TenantValidator.validate(authHolder.getTenantId(), studentPK);    
        }//if
        return resultDao.deleteBySchoolIdStudentId(studentPK.getSchoolId(), studentPK.getStudentId());
    }

    @Transactional
    public List<Result> listAll() {
        log.info("listing All");
        if (authHolder!=null && authHolder.getAccessToken()!=null && authHolder.getAccessToken().getPreferredUsername()!=null) {
            log.info("by userId=".concat(authHolder.getAccessToken().getPreferredUsername()));
            return resultDao.findBySchooldId(authHolder.getTenantId());
        }//if
        List<Result> resultList = resultDao.listAll();
        return resultList;
    }

}
