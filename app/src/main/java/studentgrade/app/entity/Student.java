package studentgrade.app.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString

@Entity
@Table(name = "student")
@NamedQuery(name = "Student.findAll", query = "SELECT o FROM Student o ORDER BY o.name")
public class Student {

    @Id
    String studentId;
    String name;
    Integer age;
    String gender;

}
