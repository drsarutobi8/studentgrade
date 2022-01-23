package com.students_information.result.event;

import com.students_information.common.value.DeletedEntity;
import com.students_information.common.event.EventType;
import com.students_information.result.domain.ResultPK;

public class ResultDeleted extends ResultEvent {
    public ResultDeleted(DeletedEntity deletedEntity, ResultPK resultPK) {
        if (deletedEntity==null) {
            throw new IllegalArgumentException("DeletedEntity cannot be null.");
        }//if
        if (resultPK==null) {
            throw new IllegalArgumentException("ResulttPK cannot be null.");
        }//if
        setEventType(EventType.DELETED);
        setDeletedEntity(deletedEntity);
        setResultPK(resultPK);
    }
}
