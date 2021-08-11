package domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Min;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "student")
public class Student {

    @Id
    String studentId;
    
    @NotBlank(message="Name may not be blank")
    String name;
    
    @Min(message="Student must be over the minimum age.", value=1)
    Integer age;
    
    @NotBlank(message="Gender may not be blank")
    String gender;

}
