package com.students_information.result.domain;

import com.students_information.common.tenant.ITenantValue;
import com.students_information.common.value.StudentPK;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "result")
@IdClass(StudentPK.class)
public class Result implements ITenantValue {

    private transient StudentPK pk;

    @Id
    String schoolId;
    @Id
    String studentId;

    String maths;
    String art;
    String chemistry;

    @Override
    public String getTenantId() {
        return schoolId;
    }

    public StudentPK getPK() {
        if (pk==null) {
            pk = new StudentPK(schoolId, studentId);
        }//if
        return pk;
    }

}
