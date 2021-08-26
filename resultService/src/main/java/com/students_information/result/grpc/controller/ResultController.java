package com.students_information.result.grpc.controller;

import com.google.protobuf.util.Timestamps;

import com.students_information.common.event.EventType;
import com.students_information.common.tenant.InvalidTenantException;
import com.students_information.common.value.DeletedEntity;
import com.students_information.common.value.StudentPK;
import com.students_information.result.domain.Result;
import com.students_information.result.event.ResultCreated;
import com.students_information.result.event.ResultDeleted;
import com.students_information.result.event.ResultEvent;
import com.students_information.result.event.ResultUpdated;
import com.students_information.result.service.ResultService;
import com.students_information.result.stubs.Grade;
import com.students_information.result.stubs.ResultCreateRequest;
import com.students_information.result.stubs.ResultCreateResponse;
import com.students_information.result.stubs.ResultDeleteRequest;
import com.students_information.result.stubs.ResultDeleteResponse;
import com.students_information.result.stubs.ResultListAllRequest;
import com.students_information.result.stubs.ResultListResponse;
import com.students_information.result.stubs.ResultReadRequest;
import com.students_information.result.stubs.ResultReadResponse;
import com.students_information.result.stubs.ResultUpdateRequest;
import com.students_information.result.stubs.ResultUpdateResponse;

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
    @Inject @Channel("out-grades") MutinyEmitter<ResultEvent> resultEventEmitter;

    @Inject
    ResultService resultService;

    public ResultCreateResponse create(ResultCreateRequest req) throws InvalidTenantException {
        log.info("creating schoolId=".concat(req.getSchoolId()).concat(" studentId=").concat(req.getStudentId()));

        Result creating = new Result();
        creating.setArt(req.getArt().toString());
        creating.setChemistry(req.getChemistry().toString());
        creating.setMaths(req.getMaths().toString());
        creating.setSchoolId(req.getSchoolId());
        creating.setStudentId(req.getStudentId());

        ResultCreated createdEvent = new ResultCreated(resultService.create(creating));
        resultEventEmitter.send(Message.of(createdEvent));
        log.info("emitting to channel ...");
        return ResultCreateResponse.newBuilder()
            .setSchoolId(createdEvent.getResult().getSchoolId())
            .setStudentId(createdEvent.getResult().getStudentId())
            .setArt(Grade.valueOf(createdEvent.getResult().getArt()))
            .setChemistry(Grade.valueOf(createdEvent.getResult().getChemistry()))
            .setMaths(Grade.valueOf(createdEvent.getResult().getMaths()))
            .setCreateId(createdEvent.getResult().getCreateId())
            .setCreateTime(Timestamps.fromMillis(createdEvent.getResult().getCreateTime().getTime()))
            .build();
    }

    public ResultReadResponse read(ResultReadRequest req) throws InvalidTenantException {
        log.info("reading schoolId=".concat(req.getSchoolId()).concat(" studentId=").concat(req.getStudentId()));
        StudentPK studentPK = new StudentPK();
        studentPK.setSchoolId(req.getSchoolId());
        studentPK.setStudentId(req.getStudentId());

        Result result = resultService.read(studentPK);
        ResultReadResponse.Builder builder = ResultReadResponse.newBuilder();
        if (result!=null) {
            builder.setArt(Grade.valueOf(result.getArt()))
                .setChemistry(Grade.valueOf(result.getChemistry()))
                .setMaths(Grade.valueOf(result.getMaths()))
                .setSchoolId(result.getSchoolId())
                .setStudentId(result.getStudentId())
                .setCreateId(result.getCreateId())
                .setUpdateId(result.getUpdateId());
            if (result.getCreateTime()!=null) {
                builder.setCreateTime(Timestamps.fromMillis(result.getCreateTime().getTime()));
            }//if
            if (result.getUpdateTime()!=null) {
                builder.setUpdateTime(Timestamps.fromMillis(result.getUpdateTime().getTime()));
            }//if
        }//if    
        return builder.build();
    }

    public ResultUpdateResponse update(ResultUpdateRequest req) throws InvalidTenantException {
        log.info("updating schoolId=".concat(req.getSchoolId()).concat(" studentId=").concat(req.getStudentId()));

        Result updating = new Result();
        updating.setArt(req.getArt().toString());
        updating.setChemistry(req.getChemistry().toString());
        updating.setMaths(req.getMaths().toString());
        updating.setSchoolId(req.getSchoolId());
        updating.setStudentId(req.getStudentId());

        ResultUpdated updatedEvent = new ResultUpdated(resultService.update(updating));
        resultEventEmitter.send(Message.of(updatedEvent));
        return ResultUpdateResponse.newBuilder()
            .setSchoolId(updatedEvent.getResult().getSchoolId())
            .setStudentId(updatedEvent.getResult().getStudentId())
            .setArt(Grade.valueOf(updatedEvent.getResult().getArt()))
            .setChemistry(Grade.valueOf(updatedEvent.getResult().getChemistry()))
            .setMaths(Grade.valueOf(updatedEvent.getResult().getMaths()))
            .setUpdateId(updatedEvent.getResult().getUpdateId())
            .setUpdateTime(Timestamps.fromMillis(updatedEvent.getResult().getUpdateTime().getTime()))
            .build();
    }

    public ResultDeleteResponse delete(ResultDeleteRequest req) throws InvalidTenantException {
        log.info("deleting schoolId=".concat(req.getSchoolId()).concat(" studentId=").concat(req.getStudentId()));
        
        StudentPK studentPK = new StudentPK();
        studentPK.setSchoolId(req.getSchoolId());
        studentPK.setStudentId(req.getStudentId());

        ResultDeleted deletedEvent = new ResultDeleted(resultService.delete(studentPK), studentPK);
        resultEventEmitter.send(Message.of(deletedEvent));
        return ResultDeleteResponse.newBuilder()
            .setSchoolId(deletedEvent.getStudentPK().getSchoolId())
            .setStudentId(deletedEvent.getStudentPK().getStudentId())
            .setDeletedCount(deletedEvent.getDeletedEntity().getDeletedCount())
            .setDeleteId(deletedEvent.getDeletedEntity().getDeleteId())
            .setDeleteTime(Timestamps.fromMillis(deletedEvent.getDeletedEntity().getDeleteTime().getTime()))
            .build();
    }

    public ResultListResponse listAll(ResultListAllRequest req) {
        log.info("listing All");
        List<Result> results = resultService.listAll();
        ResultListResponse.Builder builder = ResultListResponse.newBuilder();
        results.stream()
            .forEach(result -> builder.addResults(
                                            ResultReadResponse.newBuilder()
                                                .setArt(Grade.valueOf(result.getArt()))
                                                .setChemistry(Grade.valueOf(result.getChemistry()))
                                                .setMaths(Grade.valueOf(result.getMaths()))
                                                .setSchoolId(result.getSchoolId())
                                                .setStudentId(result.getStudentId())
                                                .build()
                                        )
                                );
        return builder.build();
    }

 }
