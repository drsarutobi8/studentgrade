package com.students_information.result.event;

import com.students_information.common.event.EventType;
import com.students_information.result.domain.Result;

public class ResultCreated extends ResultEvent {

    public ResultCreated(Result result) {
        setEventType(EventType.CREATED);
        setResult(result);
    }

}
