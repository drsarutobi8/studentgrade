package grpc.interceptor;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.Response.Status.OK;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.Prioritized;
import javax.inject.Inject;

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
import io.restassured.response.Response;
import io.restassured.path.json.JsonPath;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class AuthServerInterceptor implements ServerInterceptor, Prioritized {
    static final Metadata.Key<String> CUSTOM_HEADER_KEY = Metadata.Key.of("custom_server_header_key",
            Metadata.ASCII_STRING_MARSHALLER);
    @Inject
    BearerAuthHolder authHolder;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call,
            final Metadata requestHeaders, ServerCallHandler<ReqT, RespT> next) {
        log.info("receiving call service:" + call.getMethodDescriptor().getFullMethodName());
        log.info("header received from client:");
 
        requestHeaders.keys().stream().map(key -> Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER))
                .collect(Collectors.toMap(Metadata.Key::name, requestHeaders::get))
                .forEach((key, value) -> log.info("   " + key + "=" + value));
        Status status = Status.UNAUTHENTICATED.withDescription("Authorization token is missing");
        if (requestHeaders.containsKey(Constants.AUTHORIZATION_METADATA_KEY)) {
            log.info("getting header bearer auth key");
            String bearerAuthKey    = requestHeaders.get(Constants.AUTHORIZATION_METADATA_KEY);
            log.info("preparing auth key");
            String authKey = bearerAuthKey.substring(Constants.BEARER_TYPE.length()).trim();
            try {
                AccessToken accessToken = validateReceivedAuthorizationKey(authKey);
                if (accessToken!=null) {
                    if (accessToken.isExpired()) {
                        status = Status.DEADLINE_EXCEEDED.withDescription("token is already expired.");
                    }//if
                    else {
                        //AUTHENTICATED
                        
                        BearerAuthHolder _holder= new BearerAuthHolder();
                        _holder.setAccessToken(accessToken);
                        _holder.setBearerAuthKey(bearerAuthKey);

                        // check authorization here
                        boolean authorized = false;
                        String methodRolesAllowedConfigKey = call.getMethodDescriptor().getFullMethodName().concat(".rolesAllowed");
                        Optional<String> rolesAllowedOptional = ConfigProvider.getConfig().getOptionalValue(methodRolesAllowedConfigKey, String.class);
                        if (rolesAllowedOptional.isPresent()) {
                            Set<String> rolesAllowed = Arrays.asList(rolesAllowedOptional.get().split(",")).stream().collect(Collectors.toSet());
                            authorized = _holder.isRolesAllowed(rolesAllowed);
                            if (!authorized) {
                                status  = Status.PERMISSION_DENIED.withDescription("Required roles=".concat(rolesAllowed.toString()));
                            }//if
                        }//if
                        else {
                            log.warn("The rolesAllowed parameter ".concat(methodRolesAllowedConfigKey).concat(" is not defined."));
                            authorized =true;
                        }//else
                        if (authorized) {
                            //SET REQUEST SCOPED AUTH HOLDER
                            authHolder.copy(_holder);
                            log.info("User ".concat(authHolder.getAccessToken().getPreferredUsername().concat(" is authorized.")));
                            return Contexts.interceptCall(Context.current(), call, requestHeaders, next);   
                        }//if
                    }//else
                }//if
            }//try
            catch (AuthServerInterceptorException e) {
                log.warn("Unauthenticated User", e);
                status  = Status.UNAUTHENTICATED.withCause(e);
            }//catch
            catch (VerificationException e) {
                log.warn("Cannot Verify User", e);
                status  = Status.UNAUTHENTICATED.withCause(e);
            }//catch
        } //if
        else {
            log.warn("Cannot find Bearer Authorization in headers");
        }//else
        call.close(status, requestHeaders);
        return next.startCall(new SimpleForwardingServerCall<ReqT, RespT>(call) {
            @Override
            public void sendHeaders(Metadata responseHeaders) {
            responseHeaders.put(CUSTOM_HEADER_KEY, "customRespondValue");
            super.sendHeaders(responseHeaders);
            }
        }, requestHeaders);

    }

    /**
     * According to https://stackoverflow.com/questions/48274251/keycloak-access-token-validation-end-point
     * Access http://localhost:8180/auth/realms/studentgrade-realm/protocol/openid-connect/userinfo -H "Authorization: Bearer ${access_token}" 
     * @param authKey
     */
    private AccessToken validateReceivedAuthorizationKey(String authKey) throws VerificationException, AuthServerInterceptorException {
        log.info("start validateReceivedAuthorizationKey authKey=".concat(authKey));
        String serverUrl = ConfigProvider.getConfig().getOptionalValue("quarkus.oidc.auth-server-url", String.class).orElse("http://localhost:8180/auth/realms/studentgrade-realm");

        AccessToken token = TokenVerifier.create(authKey, AccessToken.class).getToken();
        if (token==null) {
            throw new AuthServerInterceptorException("AccessToken cannot be verified and is null.");
        }//if
        if (token.getIssuer()==null) {
            throw new AuthServerInterceptorException("AccessToken does not have Issuer.");
        }//if
        if (token.getIssuedFor()==null) {
            throw new AuthServerInterceptorException("AccessToken does not have IssuedFor.");
        }//if
        if (!token.getIssuedFor().equals("studentgrade-service")) {
            throw new AuthServerInterceptorException("AccessToken has invalid IssuedFor.");
        }//if

        int prefixRealmPos  = token.getIssuer().indexOf("studentgrade-");
        String realmName = token.getIssuer().substring(prefixRealmPos);
        log.debug("realmName=".concat(realmName));

        String userInfoPath = ConfigProvider.getConfig().getOptionalValue("quarkus.oidc.user-info-path", String.class).orElse("/protocol/openid-connect/userinfo");

        String authURL = serverUrl.substring(0,prefixRealmPos).concat(realmName).concat(userInfoPath);
        log.debug("authURL=".concat(authURL));
        Response response = given().auth().oauth2(authKey)
                                .when().get(authURL)
                                .then()
                                    .extract().response();
        int statusCode = response.getStatusCode();
        if (statusCode!=OK.getStatusCode()) {
            throw new AuthServerInterceptorException("Unauthenticated Status Code:".concat(String.valueOf(statusCode)));
        }//if
        JsonPath responseJson = response.jsonPath();
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

        log.info("Acr="+token.getAcr());
        log.info("Id="+token.getId());
        log.info("IssuedFor="+token.getIssuedFor());
        log.info("Issuer="+token.getIssuer());
        log.info("Nonce="+token.getNonce());
        log.info("Profile="+token.getProfile());
        log.info("Scope="+token.getScope());
        log.info("SessionState="+token.getSessionState());
        log.info("Subject="+token.getSubject());
        log.info("Type="+token.getType());

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
