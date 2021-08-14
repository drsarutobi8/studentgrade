package com.students_information.student.kafka.serialization;

import com.students_information.student.domain.Result;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class ResultDeserializer extends ObjectMapperDeserializer<Result> {
    public ResultDeserializer() {
        super(Result.class);
    }
}