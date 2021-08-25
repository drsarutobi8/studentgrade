package com.students_information.student.service;

import com.google.protobuf.util.Timestamps;

import com.students_information.common.event.EventType;
import com.students_information.common.tenant.InvalidTenantException;
import com.students_information.common.value.StudentPK;
import com.students_information.result.stubs.ResultCreateResponse;
import com.students_information.student.domain.Result;
import com.students_information.student.event.ResultEvent;

import java.util.NoSuchElementException;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
@Slf4j
public class ResultListener {
    // @Inject
    // ResultService resultService;

    @Incoming("in-grades")
    public Uni<Void> listen(ResultEvent event) {
        if (event==null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }//if
        if (event.getEventType()==null) {
            throw new IllegalArgumentException("EventType cannot be null.");
        }//if
        log.info("listening ResultEvent eventType=".concat(event.getEventType().name()));
        switch (event.getEventType()) {
            case CREATED:
                return create(event.getResult());
            case UPDATED:
                return update(event.getResult());
            case DELETED:
                return delete(event.getStudentPK());
            default:
                throw new UnsupportedOperationException("Unknown supported event ".concat(event.getEventType().name()));
        }//switch
    }

    private Uni<Void> create(@Valid Result result) {
        log.info("creating result=".concat(result.toString()));
        return Panache.withTransaction(result::persist)
                    .replaceWithVoid();
    }

    private Uni<Void> update(@Valid Result result) {
        log.info("updating result=".concat(result.toString()));
        return Panache.withTransaction(()-> Result.findById(result.getPK())
                                                .onItem()
                                                    .ifNotNull()
                                                        .invoke(updatingObj -> result.persist()
                                                                    //{
                                                                    // Result updating = (Result)updatingObj;
                                                                    // updating.setArt(result.getArt());
                                                                    // updating.setChemistry(result.getChemistry());
                                                                    // updating.setMaths(result.getMaths());
                                                                    // updating.setCreateId(result.getCreateId());
                                                                    // updating.setCreateTime(result.getCreateTime());
                                                                    // updating.setUpdateId(result.getUpdateId());
                                                                    // updating.setUpdateTime(result.getUpdateTime());
                                                                    // updating.persist();
                                                            //}
                                                            )
                                                .onItem()
                                                    .ifNull()
                                                        .failWith(
                                                            new NoSuchElementException("Unknown Student with studentPK=".concat(result.getPK().toString()))
                                                        )
                                        )
                                        .replaceWithVoid();
    }

    private Uni<Void> delete(StudentPK studentPK) {
        log.info("deleting result studentPK=".concat(studentPK.toString()));

        return Panache.withTransaction(()-> Result.deleteById(studentPK))
                                            .onItem()
                                                .ifNull()
                                                    .failWith(new NoSuchElementException("Unknown Student with studentId=".concat(studentPK.getStudentId())))
                                            .replaceWithVoid();
    }

}