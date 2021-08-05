package security.identity;

import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import grpc.interceptor.BearerAuthHolder;
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

    @Inject
    BearerAuthHolder authHolder;

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
        if (authHolder!=null) {
            QuarkusSecurityIdentity.Builder builder =null;
            if(identity.isAnonymous()) {
                builder = QuarkusSecurityIdentity.builder()
                    .setPrincipal(authHolder.getPrincipal())
                    .setAnonymous(false);
            }//if
            else {
                // create a new builder and copy principal, attributes, credentials and roles from the original identity
                builder = QuarkusSecurityIdentity.builder(identity);
            }//else
            builder.addRoles(authHolder.getRoles());
            return builder::build;
        }//if

        return () -> identity;       
    }

}