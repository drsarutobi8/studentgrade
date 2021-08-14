package tenant;

public class TenantValidator {
    public static void validate(String validTenantId, ITenantValue iTenantValue) throws InvalidTenantException {
        if (validTenantId!=null) {    
            if (iTenantValue==null) {
                throw new IllegalArgumentException("iTenantValue cannot be null.");
            }//if
            if (iTenantValue.getTenantId()==null || iTenantValue.getTenantId().trim().length()==0) {
                throw new InvalidTenantException(validTenantId, "<MISSING TENANT_ID>");
            }//if
            if (!validTenantId.equals(iTenantValue.getTenantId())) {
                throw new InvalidTenantException(validTenantId, iTenantValue.getTenantId());
            }//if
        }//if
    }
}
