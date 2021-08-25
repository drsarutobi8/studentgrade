package com.students_information.student.event;

import com.students_information.common.event.EventType;
import com.students_information.common.value.DeletedEntity;
import com.students_information.common.value.StudentPK;
import com.students_information.student.domain.Result;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class ResultEvent {

    private EventType eventType;
    private Result result;    
    private StudentPK studentPK;
    private DeletedEntity deletedEntity;

    public Timestamp getEventTime() {
        if (eventType==null) {
            throw new IllegalStateException("EventType is null.");
        }//if
        switch (eventType) {
            case CREATED:
                if (result==null) {
                    throw new IllegalStateException("CREATED Event must have result.");
                }//if
                return result.getCreateTime();
            case UPDATED:
                if (result==null) {
                    throw new IllegalStateException("UPDATED Event must have result.");
                }//if
                return result.getUpdateTime();
            case DELETED:
                if (deletedEntity==null) {
                    throw new IllegalStateException("DELETED Event must have deletedEntity.");
                }//if
                return deletedEntity.getDeleteTime();
            default:
                throw new UnsupportedOperationException("Unknown eventType=".concat(eventType.name()));
        }//switch
    }
}