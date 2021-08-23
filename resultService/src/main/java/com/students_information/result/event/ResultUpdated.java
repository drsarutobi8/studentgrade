package com.students_information.result.event;

import com.students_information.common.event.EventType;
import com.students_information.result.domain.Result;

public class ResultUpdated extends ResultEvent {

    public ResultUpdated(Result result) {
        setEventType(EventType.UPDATED);
        setResult(result);
    }

}
