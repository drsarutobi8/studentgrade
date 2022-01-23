// Generated from st4/pkClass.stg by ANTLR 4.9.3

package com.students_information.result.domain;

import com.students_information.common.tenant.ITenantValue;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResultPK implements Serializable, ITenantValue {
    String schoolId;
    String studentId;

    @Override
    public String getTenantId() {
        return schoolId;
    }
}