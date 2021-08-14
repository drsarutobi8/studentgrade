package com.students_information.student.dao;

import com.students_information.student.domain.Student;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class StudentDao implements PanacheRepository<Student> {
    public Uni<Student> findBySchoolIdStudentId(String schoolId, String studentId){
        return find("schoolId=?1 AND studentId=?2", schoolId, studentId).firstResult();
    }
    public Uni<Long> deleteBySchoolIdStudentId(String schoolId, String studentId) {
        return delete("schoolId=?1 AND studentId=?2", schoolId, studentId);
    }

    public Uni<List<Student>> findBySchooldId(String schoolId) {
        return find("schoolId", schoolId).list();
    }
    public Uni<Long> deleteBySchoolId(String schoolId) {
        return delete("schoolId", schoolId);
    }

    public Uni<List<Student>> findByStudentId(String studentId){
        return find("studentId", studentId).list();
    }
    public Uni<Long> deleteByStudentId(String studentId) {
        return delete("studentId", studentId);
    }
}