package grpc.interceptor;

import java.security.Principal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.RequestScoped;

import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessToken.Access;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import tenant.ITenantValue;

@Getter
@RequestScoped
public class BearerAuthHolder {
    @Setter
    private String bearerAuthKey;

    @Setter
    private String tenantId;

    private AccessToken accessToken;
    private Set<String> roles;
    private Principal principal;

    public void setAccessToken(AccessToken accessToken) {
        this.accessToken    = accessToken;
        this.principal  = new AuthPrincipal(accessToken.getPreferredUsername());
        
        Set<String> roles = new HashSet<String>();
        if (accessToken.getRealmAccess()!=null && accessToken.getRealmAccess().getRoles()!=null) {
            roles.addAll(accessToken.getRealmAccess().getRoles());
        }//if
        Map<String,Access> mapAccess  = accessToken.getResourceAccess();
        for (Map.Entry<String,Access> entry:mapAccess.entrySet()) {
            Access access   = entry.getValue();
            roles.addAll(access.getRoles());
        }//for
        this.roles = roles;
    }

    public void copy(BearerAuthHolder _holder) {
        this.bearerAuthKey  = _holder.getBearerAuthKey();
        this.accessToken    = _holder.getAccessToken();
        this.roles  = _holder.getRoles();
        this.principal = _holder.getPrincipal();
        this.tenantId = _holder.getTenantId();
    }

    public boolean isRolesAllowed(Set<String> rolesAllowed) {
        boolean authorized = false;
        for (Iterator<String> rolesIter = getRoles().iterator();(!authorized)&&rolesIter.hasNext();) {
            authorized = authorized || rolesAllowed.contains(rolesIter.next());
        }//for
        return authorized;
    }

    @Data
    private static class AuthPrincipal implements Principal {
        private AuthPrincipal(String name) {
            this.name   = name;
        }
        private String name;
    }

    public boolean isValidTenant(ITenantValue iTenantValue) {
        return tenantId.equals(iTenantValue.getTenantId());
    }

}
