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
@Table(name = "result")
@NamedQuery(name = "Result.findAll", query = "SELECT o FROM Result o ORDER BY o.studentId")
public class Result {   
    @Id
    String studentId;

    String maths;
    String art;
    String chemistry;

}
