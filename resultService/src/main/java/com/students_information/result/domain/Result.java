package com.students_information.result.domain;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.students_information.common.tenant.ITenantValue;
import com.students_information.common.value.StudentPK;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
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
public class Result extends PanacheEntityBase implements ITenantValue {

    private transient StudentPK pk;

    @Id
    String schoolId;
    @Id
    String studentId;

    String maths;
    String art;
    String chemistry;

    String createId;
    Timestamp createTime;

    String updateId;
    Timestamp updateTime;

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

    public static Result findBySchoolIdStudentId(String schoolId, String studentId){
        return find("schoolId=?1 AND studentId=?2", schoolId, studentId).firstResult();
    }
    public static long deleteBySchoolIdStudentId(String schoolId, String studentId) {
        return delete("schoolId=?1 AND studentId=?2", schoolId, studentId);
    }

    public static List<Result> findBySchooldId(String schoolId) {
        return find("schoolId", schoolId).list();
    }
    public static long deleteBySchoolId(String schoolId) {
        return delete("schoolId", schoolId);
    }

    public static List<Result> findByStudentId(String studentId){
        return find("studentId", studentId).list();
    }
    public static long deleteByStudentId(String studentId) {
        return delete("studentId", studentId);
    }
}
