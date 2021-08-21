package com.students_information.result.service;

import com.students_information.common.tenant.InvalidTenantException;
import com.students_information.common.value.StudentPK;
import com.students_information.result.domain.Result;

import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.reactive.messaging.MutinyEmitter;

import lombok.extern.slf4j.Slf4j;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Message;

@ApplicationScoped
@Blocking
@Slf4j
public class ResultController {
    @Inject @Channel("out-grades") MutinyEmitter<Result> resultEmitter;

    @Inject
    ResultService resultService;

    public Result create(Result creating) throws InvalidTenantException {
        log.info("creating studentPK=".concat(creating.getPK().toString()));
        Result created = resultService.create(creating);
        resultEmitter.send(Message.of(created));
        log.info("emitting to channel ...");
        return created;
    }

    public Result read(StudentPK studentPK) throws InvalidTenantException {
        log.info("reading studentPK=".concat(studentPK.toString()));
        return resultService.read(studentPK);
    }

    public Result update(Result updating) throws InvalidTenantException {
        log.info("updating studentPK=".concat(updating.getPK().toString()));
        Result updated = resultService.update(updating);
        //resultEmitter.send(Message.of(updated));
        return updated;
    }

    public Long delete(StudentPK studentPK) throws InvalidTenantException {
        log.info("deleting studentId=".concat(studentPK.toString()));
        long deletedCount = resultService.delete(studentPK);
        //resultEmitter.send(Message.of(updated));
        return deletedCount;
    }

    public List<Result> listAll() {
        log.info("listing All");
        return resultService.listAll();
    }
 }
