package com.students_information.common.value;

import java.sql.Timestamp;
import lombok.Data;

@Data
public class DeletedEntity {
    private long deletedCount;
    private String deleteId;
    private Timestamp deleteTime;    
}
