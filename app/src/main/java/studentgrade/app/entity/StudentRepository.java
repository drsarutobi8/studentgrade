package studentgrade.app.entity;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.List;

import org.hibernate.reactive.mutiny.Mutiny;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class StudentRepository {

    @Inject
    Mutiny.Session session;

    public Uni<List<Student>> findAll() {
        return session
                .createNamedQuery("Student.findAll", Student.class)
                .getResultList();
    }

    public Uni<Student> findById(String studentId){
        return session.find(Student.class, studentId);
    }
}
