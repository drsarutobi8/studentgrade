package com.students_information.student.service;

import com.google.protobuf.util.Timestamps;

import com.students_information.common.event.EventType;
import com.students_information.common.tenant.InvalidTenantException;
import com.students_information.result.stubs.ResultCreateResponse;
import com.students_information.student.domain.Result;
import com.students_information.student.event.ResultEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
@Slf4j
public class ResultListener {
    @Inject
    ResultService resultService;

    @Incoming("in-grades")
    public void listen(ResultEvent event) {
        if (event==null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }//if
        if (event.getEventType()==null) {
            throw new IllegalArgumentException("EventType cannot be null.");
        }//if
        log.info("listening ResultEvent eventType=".concat(event.getEventType().name()));
        switch (event.getEventType()) {
            case CREATED:
                resultService.create(event.getResult());
                break;
            case UPDATED:
                resultService.update(event.getResult());
                break;
            case DELETED:
                resultService.delete(event.getStudentPK());
                break;
            default:
                throw new UnsupportedOperationException("Unknown supported event ".concat(event.getEventType().name()));
        }//switch
    }
}
