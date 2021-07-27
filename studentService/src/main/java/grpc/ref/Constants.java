package grpc.ref;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

import io.grpc.Context;
import io.grpc.Metadata;

import org.keycloak.representations.AccessToken;

public interface Constants {
    public static final String BEARER_TYPE = "Bearer";

    public static final Metadata.Key<String> AUTHORIZATION_METADATA_KEY = Metadata.Key.of("Authorization", ASCII_STRING_MARSHALLER);
    public static final Context.Key<String> CLIENT_ID_CONTEXT_KEY = Context.key("clientId");
    public static final Context.Key<AccessToken> ACCESS_TOKEN_CONTEXT_KEY =  Context.key("accessToken");
}
