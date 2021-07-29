package dao;

import domain.Student;

import javax.enterprise.context.ApplicationScoped;

import io.smallrye.mutiny.Uni;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;

@ApplicationScoped
public class StudentDao implements PanacheRepository<Student> {
    public Uni<Student> findByStudentId(String studentId){
        return find("studentId", studentId).firstResult();
    }

    public Uni<Long> deleteByStudentId(String studentId) {
        return delete("studentId", studentId);
    }
}