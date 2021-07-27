package security.identity;

import java.security.Principal;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;

import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessToken.Access;

import grpc.ref.Constants;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
/**
 * CURRENTLY NOT WORKING, THIS CLASS NEVER BEEN CALLED
 * Maybe I should take a look further at 
 * https://nderwin.wordpress.com/2020/01/20/augment-your-quarkus-identity/
 */
public class RolesAugmentor implements SecurityIdentityAugmentor {

    @Override
    public int priority() {
        return 1;
        //return SecurityIdentityAugmentor.super.priority();
    }

    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context) {
        log.info("CALLING AUGMENT");
        //return context.runBlocking(build(identity));
        return Uni.createFrom().item(build(identity));

        // Do 'return context.runBlocking(build(identity));'
        // if a blocking call is required to customize the identity
    }

    private Supplier<SecurityIdentity> build(SecurityIdentity identity) {
        log.info("CALLING BUILD SECURITY IDENTITY");
        AccessToken token = Constants.ACCESS_TOKEN_CONTEXT_KEY.get();
        if (token!=null) {
            log.info(String.format("iss = %s%n", token.getIssuer()));
            log.info(String.format("sub = %s%n", token.getSubject()));
            log.info(String.format("typ = %s%n", token.getType()));
            if (token.getName()!=null) {
                log.info("name=".concat(token.getName()));
            }//if
            if (token.getPreferredUsername()!=null) {
                log.info("userName=".concat(token.getPreferredUsername()));
            }//if
            if (token.getRealmAccess()!=null && token.getRealmAccess().getRoles()!=null) {
                Set<String> roles = token.getRealmAccess().getRoles();
                log.info("roles=".concat(roles.toString()));
            }//if
        
            QuarkusSecurityIdentity.Builder builder =null;
            if(identity.isAnonymous()) {
                builder = QuarkusSecurityIdentity.builder()
                    .setPrincipal(new Principal() { 
                        @Override
                        public String getName() {
                            return token.getPreferredUsername();
                        }})
                    .setAnonymous(false);
            }//if
            else {
                // create a new builder and copy principal, attributes, credentials and roles from the original identity
                builder = QuarkusSecurityIdentity.builder(identity);
            }//else
            builder.addRoles(token.getRealmAccess().getRoles());
            Map<String,Access> mapAccess  = token.getResourceAccess();
            for (Map.Entry<String,Access> entry:mapAccess.entrySet()) {
                Access access   = entry.getValue();
                builder.addRoles(access.getRoles());
            }//for
            return builder::build;
        }//if

        return () -> identity;       
    }

}