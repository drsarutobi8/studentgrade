package com.students_information.result.event;

import com.students_information.common.event.EventType;
import com.students_information.common.value.DeletedEntity;
import com.students_information.result.domain.Result;
import com.students_information.result.domain.ResultPK;

import lombok.Data;

@Data
public class ResultEvent {

    private EventType eventType;
    private Result result;

    private ResultPK resultPK;
    private DeletedEntity deletedEntity;
}
