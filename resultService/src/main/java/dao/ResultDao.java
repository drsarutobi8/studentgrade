package dao;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import domain.Result;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class ResultDao implements PanacheRepository<Result> {

    public Result findBySchoolIdStudentId(String schoolId, String studentId){
        return find("schoolId=?1 AND studentId=?2", schoolId, studentId).firstResult();
    }
    public long deleteBySchoolIdStudentId(String schoolId, String studentId) {
        return delete("schoolId=?1 AND studentId=?2", schoolId, studentId);
    }

    public List<Result> findBySchooldId(String schoolId) {
        return find("schoolId", schoolId).list();
    }
    public long deleteBySchoolId(String schoolId) {
        return delete("schoolId", schoolId);
    }

    public List<Result> findByStudentId(String studentId){
        return find("studentId", studentId).list();
    }
    public long deleteByStudentId(String studentId) {
        return delete("studentId", studentId);
    }
}
