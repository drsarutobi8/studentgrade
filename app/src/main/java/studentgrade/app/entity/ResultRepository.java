package studentgrade.app.entity;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.List;

import org.hibernate.reactive.mutiny.Mutiny;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class ResultRepository {

    @Inject
    Mutiny.Session session;

    public Uni<List<Result>> findAll() {
        return session
                .createNamedQuery("Result.findAll", Result.class)
                .getResultList();
    }

    public Uni<Result> findById(String studentId){
        return session.find(Result.class, studentId);
    }
}
