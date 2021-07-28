package value;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class StudentInfo {

    String studentId;
    String name;
    Integer age;
    String gender;
    
    String maths;
    String art;
    String chemistry;
}
