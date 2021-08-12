package domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

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
@IdClass(StudentPK.class)
@Table(name = "student")
public class Student implements ITenantValue{

    private transient StudentPK pk;

    @Id
    String schoolId;

    @Id
    String studentId;

    @NotBlank(message="Name may not be blank")
    String name;
    
    @Min(message="Student must be over the minimum age.", value=1)
    Integer age;
    
    @NotBlank(message="Gender may not be blank")
    String gender;

    @Override
    public String getTenantId() {
        return schoolId;
    }

    public StudentPK getPK() {
        if (pk==null) {
            pk = new StudentPK(schoolId, studentId);
        }//if
        return pk;
    }
}
