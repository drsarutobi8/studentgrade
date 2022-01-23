package com.students_information.student.domain;

import com.students_information.common.tenant.ITenantValue;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StudentPK implements Serializable, ITenantValue{
    public String schoolId;
    public String studentId;

    @Override
    public String getTenantId() {
        return schoolId;
    }
}