package com.students_information.student.domain;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.students_information.common.tenant.ITenantValue;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@ToString
@Entity
@IdClass(StudentPK.class)
@Table(name = "result")
public class Result extends PanacheEntityBase implements ITenantValue {
    private transient StudentPK pk;

    @Id String schoolId;
    @Id String studentId;

    String maths;
    String art;
    String chemistry;
    String createId;
    Timestamp createTime;
    String updateId;
    Timestamp updateTime;

    @OneToOne(mappedBy = "result")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    Student student;

    public void copy(Result other) {
        setMaths(other.getMaths());
        setArt(other.getArt());
        setChemistry(other.getChemistry());
    }

    public void copyWithAuditTrail(Result other) {
        copy(other);
        setCreateId(other.getCreateId());
        setCreateTime(other.getCreateTime());
        setUpdateId(other.getUpdateId());
        setUpdateTime(other.getUpdateTime());
    }

    public StudentPK getPK() {
        if (pk == null) {
            pk = new StudentPK(schoolId, studentId);
        } // if
        return pk;
    }

    @Override
    public String getTenantId() {
        return schoolId;
    }

    public static Uni<List<Result>> findBySchoolId(String schoolId) {
        return find("schoolId", schoolId).list();
    }

    public static Uni<Long> deleteBySchoolId(String schoolId) {
        return delete("schoolId", schoolId);
    }
}