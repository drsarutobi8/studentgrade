package com.students_information.common.value;

import com.students_information.common.tenant.ITenantValue;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StudentPK implements Serializable, ITenantValue{
    String schoolId;
    String studentId;

    @Override
    public String getTenantId() {
        return schoolId;
    }
}