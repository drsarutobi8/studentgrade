package com.students_information.student.kafka.serialization;

import com.students_information.student.event.ResultEvent;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class ResultEventDeserializer extends ObjectMapperDeserializer<ResultEvent> {
    public ResultEventDeserializer() {
        super(ResultEvent.class);
    }
}