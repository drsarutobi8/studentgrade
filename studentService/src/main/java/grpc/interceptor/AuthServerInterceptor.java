package grpc.interceptor;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.Response.Status.OK;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.Prioritized;

import org.eclipse.microprofile.config.ConfigProvider;
import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessToken.Access;

import grpc.ref.Constants;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import io.restassured.path.json.JsonPath;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class AuthServerInterceptor implements ServerInterceptor, Prioritized {
    static final Metadata.Key<String> CUSTOM_HEADER_KEY = Metadata.Key.of("custom_server_header_key",
            Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call,
            final Metadata requestHeaders, ServerCallHandler<ReqT, RespT> next) {
        log.info("calling service:" + call.getMethodDescriptor().getFullMethodName());
        log.info("header received from client:");
        requestHeaders.keys().stream().map(key -> Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER))
                .collect(Collectors.toMap(Metadata.Key::name, requestHeaders::get))
                .forEach((key, value) -> log.info(key + "=" + value));
        Status status = Status.UNAUTHENTICATED.withDescription("Authorization token is missing");
        if (requestHeaders.containsKey(Constants.AUTHORIZATION_METADATA_KEY)) {
            String authKey = requestHeaders.get(Constants.AUTHORIZATION_METADATA_KEY).substring(Constants.BEARER_TYPE.length()).trim();
            try {
                AccessToken token = this.validateReceivedAuthorizationKey(authKey);
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

                    //authorization here
                    boolean authorized = false;
                    String methodRolesAllowedConfigKey = call.getMethodDescriptor().getFullMethodName().concat(".rolesAllowed");
                    Optional<String> rolesAllowedOptional = ConfigProvider.getConfig().getOptionalValue(methodRolesAllowedConfigKey, String.class);
                    if (rolesAllowedOptional.isPresent()) {
                        Set<String> rolesAllowed = Arrays.asList(rolesAllowedOptional.get().split(",")).stream().collect(Collectors.toSet());

                        Set<String> roles = new HashSet<String>();
                        if (token.getRealmAccess()!=null && token.getRealmAccess().getRoles()!=null) {
                            roles.addAll(token.getRealmAccess().getRoles());
                        }//if
                        Map<String,Access> mapAccess  = token.getResourceAccess();
                        for (Map.Entry<String,Access> entry:mapAccess.entrySet()) {
                            Access access   = entry.getValue();
                            roles.addAll(access.getRoles());
                        }//for
                        log.info("roles=".concat(roles.toString()));

                        for (String role:roles) {
                            authorized = authorized || rolesAllowed.contains(role);
                        }//for
                        if (!authorized) {
                            status  = Status.PERMISSION_DENIED.withDescription("Required roles=".concat(rolesAllowed.toString()));
                        }//if
                    }//if
                    else {
                        log.warn("The rolesAllowed parameter ".concat(methodRolesAllowedConfigKey).concat(" is not defined."));
                        authorized =true;
                    }//else
                    //SET CONTEXT HERE FROM VALIDATED TOKEN
                    if (authorized) {
                        Context ctx = Context.current()
                                        .withValue(Constants.CLIENT_ID_CONTEXT_KEY, token.getSubject())
                                        .withValue(Constants.ACCESS_TOKEN_CONTEXT_KEY, token);

                        return Contexts.interceptCall(ctx, call, requestHeaders, next);   
                    }//if
                }//if         
            }//try
            catch (AuthServerInterceptorException e) {
                status  = Status.UNAUTHENTICATED.withCause(e);
            }//catch
            catch (VerificationException e) {
                status  = Status.UNAUTHENTICATED.withCause(e);
            }//catch
        } // else

        call.close(status, requestHeaders);
        return next.startCall(new SimpleForwardingServerCall<ReqT, RespT>(call) {
            // @Override
            // public void sendHeaders(Metadata responseHeaders) {
            // responseHeaders.put(CUSTOM_HEADER_KEY, "customRespondValue");
            // super.sendHeaders(responseHeaders);
            // }
        }, requestHeaders);

    }

    /**
     * According to https://stackoverflow.com/questions/48274251/keycloak-access-token-validation-end-point
     * Access http://localhost:8180/auth/realms/studentgrade-realm/protocol/openid-connect/userinfo -H "Authorization: Bearer ${access_token}" 
     * @param authKey
     */
    private AccessToken validateReceivedAuthorizationKey(String authKey) throws VerificationException, AuthServerInterceptorException {
        log.info("authKey=".concat(authKey));
        String authURL = "http://localhost:8180/auth/realms/studentgrade-realm/protocol/openid-connect/userinfo";
        JsonPath responseJson = given().auth().oauth2(authKey)
                                .when().get(authURL)
                                .then()
                                    .statusCode(OK.getStatusCode())
                                    .extract().response().jsonPath();
        String resp_preferredUserName = responseJson.getString("preferred_username");
        if (resp_preferredUserName==null) {
            throw new AuthServerInterceptorException("Response preferredUserName is null");
        }//if
        log.info(String.format("response preferredUserName = %s%n", resp_preferredUserName));
        
        String resp_subject = responseJson.getString("sub");
        if (resp_subject==null) {
            throw new AuthServerInterceptorException("Response subject is null");
        }//if
        log.info(String.format("response subject = %s%n", resp_subject));

        String resp_name = responseJson.getString("name");
        if (resp_name==null) {
            throw new AuthServerInterceptorException("Response name is null");
        }//if
        log.info(String.format("response name = %s%n", resp_name));

        Boolean resp_emailVerified = responseJson.get("email_verified");
        if (resp_emailVerified==null) {
            throw new AuthServerInterceptorException("Response emailVerified is null");
        }//if
        log.info(String.format("response emailVerified = %s%n", String.valueOf(resp_emailVerified)));

        AccessToken token = TokenVerifier.create(authKey, AccessToken.class).getToken();
        if (token==null) {
            throw new AuthServerInterceptorException("AccessToken cannot be verified and is null.");
        }//if

        if (token.getSubject()==null) {
            throw new AuthServerInterceptorException("AccessToken subject is null.");
        }//if

        if (token.getPreferredUsername()==null) {
            throw new AuthServerInterceptorException("AccessToken preferredUserName is null.");
        }//if

        if (!resp_subject.equals(token.getSubject()))  {
            throw new AuthServerInterceptorException("AccessToken subject and Response subject are not the same.");
        }//if

        if (!resp_preferredUserName.equals(token.getPreferredUsername()))  {
            throw new AuthServerInterceptorException("AccessToken preferredUserName and Response preferredUserName are not the same.");
        }//if

        if (token.getName()==null) {
            throw new AuthServerInterceptorException("AccessToken name is null.");
        }//if

        if (!resp_name.equals(token.getName()))  {
            throw new AuthServerInterceptorException("AccessToken name and Response name are not the same.");
        }//if

        if (token.getEmailVerified()==null) {
            throw new AuthServerInterceptorException("AccessToken emailVerified is null.");
        }//if

        if (!resp_emailVerified.equals(token.getEmailVerified()))  {
            throw new AuthServerInterceptorException("AccessToken emailVerified and Response emailVerified are not the same.");
        }//if

        return token;
    }

    @Override
    public int getPriority() {
        return 10;
    }

    public static class AuthServerInterceptorException extends Exception {
        private AuthServerInterceptorException(String errorMsg) {
            super(errorMsg);
        }
    }

    //https://dzone.com/articles/how-to-handle-checked-exception-in-lambda-expressi
    @FunctionalInterface
    public interface ThrowingFunction<T, R, E extends Throwable> {
        R apply(T t) throws E;
    
        static <T, R, E extends Throwable> Function<T, R> unchecked(ThrowingFunction<T, R, E> f) {
            return t -> {
                try {
                    return f.apply(t);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            };
        }
    }
}
