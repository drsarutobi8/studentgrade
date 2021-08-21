package com.students_information.student.domain;

import com.students_information.common.tenant.ITenantValue;
import com.students_information.common.value.StudentPK;

import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import io.smallrye.mutiny.Uni;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@IdClass(StudentPK.class)
@Table(name = "student")
public class Student extends PanacheEntityBase implements ITenantValue{

    private transient StudentPK pk;

    @Id
    String schoolId;

    @Id
    String studentId;

    @NotBlank(message="Name may not be blank")
    String name;
    
    @Min(message="Student must be over the minimum age.", value=1)
    Integer age;
    
    @NotBlank(message="Gender may not be blank")
    String gender;

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

    public static Uni<Student> findBySchoolIdStudentId(String schoolId, String studentId){
        return find("schoolId=?1 AND studentId=?2", schoolId, studentId).firstResult();
    }
    public static Uni<Long> deleteBySchoolIdStudentId(String schoolId, String studentId) {
        return delete("schoolId=?1 AND studentId=?2", schoolId, studentId);
    }

    public static Uni<List<Student>> findBySchooldId(String schoolId) {
        return find("schoolId", schoolId).list();
    }
    public static Uni<Long> deleteBySchoolId(String schoolId) {
        return delete("schoolId", schoolId);
    }

    public static Uni<List<Student>> findByStudentId(String studentId){
        return find("studentId", studentId).list();
    }
    public static Uni<Long> deleteByStudentId(String studentId) {
        return delete("studentId", studentId);
    }
}
