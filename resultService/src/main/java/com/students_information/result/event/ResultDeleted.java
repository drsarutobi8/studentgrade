package com.students_information.result.event;

import com.students_information.common.value.DeletedEntity;
import com.students_information.common.value.StudentPK;
import com.students_information.common.event.EventType;

public class ResultDeleted extends ResultEvent {
    public ResultDeleted(DeletedEntity deletedEntity, StudentPK studentPK) {
        if (deletedEntity==null) {
            throw new IllegalArgumentException("DeletedEntity cannot be null.");
        }//if
        if (studentPK==null) {
            throw new IllegalArgumentException("StudentPK cannot be null.");
        }//if
        setEventType(EventType.DELETED);
        setDeletedEntity(deletedEntity);
        setStudentPK(studentPK);
    }
}
