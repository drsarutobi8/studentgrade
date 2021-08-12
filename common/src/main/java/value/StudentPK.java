package value;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tenant.ITenantValue;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StudentPK implements Serializable, ITenantValue{
    String schoolId;
    String studentId;

    @Override
    public String getTenantId() {
        return schoolId;
    }
}