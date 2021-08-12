package domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tenant.ITenantValue;
import value.StudentPK;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "result")
@IdClass(StudentPK.class)
public class Result implements ITenantValue {

    @Id
    String schoolId;
    @Id
    String studentId;

    String maths;
    String art;
    String chemistry;

    @Override
    public String getTenantId() {
        return schoolId;
    }
}
