package com.students_information.student.dao;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import com.students_information.student.domain.Result;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class ResultDao implements PanacheRepository<Result> {

    public Uni<Result> findBySchoolIdStudentId(String schoolId, String studentId){
        return find("schoolId=?1 AND studentId=?2", schoolId, studentId).firstResult();
    }
    public Uni<Long> deleteBySchoolIdStudentId(String schoolId, String studentId) {
        return delete("schoolId=?1 AND studentId=?2", schoolId, studentId);
    }

    public Uni<List<Result>> findBySchooldId(String schoolId) {
        return find("schoolId", schoolId).list();
    }
    public Uni<Long> deleteBySchoolId(String schoolId) {
        return delete("schoolId", schoolId);
    }

    public Uni<List<Result>> findByStudentId(String studentId){
        return find("studentId", studentId).list();
    }
    public Uni<Long> deleteByStudentId(String studentId) {
        return delete("studentId", studentId);
    }
}
