package com.students_information.common.tenant;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class InvalidTenantException extends Exception {
    private String validTenantId;
    private String invalidTenantId;

    public InvalidTenantException(String validTenantId, String invalidTenantId) {
        this.validTenantId = validTenantId;
        this.invalidTenantId = invalidTenantId;
    }
}