package com.students_information.student.domain;

import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.students_information.common.tenant.ITenantValue;
import com.students_information.common.value.StudentPK;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
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

    @Transient
    private StudentPK pk;

    @Id
    String schoolId;
    @Id
    String studentId;

    String maths;
    String art;
    String chemistry;

    @OneToOne
    @JoinColumns({
        @JoinColumn(name = "schoolId", referencedColumnName = "schoolId", nullable = false, insertable = false, updatable = false),
        @JoinColumn(name = "studentId", referencedColumnName = "studentId", nullable = false, insertable = false, updatable = false)
    })
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
    @JsonIgnore
    Student student;

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

    public static Uni<Result> findBySchoolIdStudentId(String schoolId, String studentId){
        return find("schoolId=?1 AND studentId=?2", schoolId, studentId).firstResult();
    }
    public static Uni<Long> deleteBySchoolIdStudentId(String schoolId, String studentId) {
        return delete("schoolId=?1 AND studentId=?2", schoolId, studentId);
    }

    public static Uni<List<Result>> findBySchooldId(String schoolId) {
        return find("schoolId", schoolId).list();
    }
    public static Uni<Long> deleteBySchoolId(String schoolId) {
        return delete("schoolId", schoolId);
    }

    public static Uni<List<Result>> findByStudentId(String studentId){
        return find("studentId", studentId).list();
    }
    public static Uni<Long> deleteByStudentId(String studentId) {
        return delete("studentId", studentId);
    }
}
