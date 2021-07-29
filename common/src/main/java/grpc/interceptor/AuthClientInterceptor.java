package grpc.interceptor;

import grpc.ref.Constants;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.Prioritized;

import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class AuthClientInterceptor implements ClientInterceptor, Prioritized {
    static final Metadata.Key<String> CUSTOM_HEADER_KEY = Metadata.Key.of("custom_client_header_key", Metadata.ASCII_STRING_MARSHALLER);
    private volatile long callTime;

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions, Channel next) {
        log.info("sending call service:" + method.getFullMethodName());
        
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                /* put custom header */
                headers.put(CUSTOM_HEADER_KEY, "customRequestValue");
                String bearerAuthKey = Constants.BEARER_AUTHORIZATION_CONTEXT_KEY.get();
                if (bearerAuthKey!=null && bearerAuthKey.trim().length()>0) {
                    headers.put(Constants.AUTHORIZATION_METADATA_KEY, bearerAuthKey);
                }//if
                super.start(
                        new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                            @Override
                            protected Listener<RespT> delegate() {
                                callTime = System.nanoTime();

                                log.info("headers sending from client:");
                                headers.keys().stream().map(key -> Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER))
                                        .collect(Collectors.toMap(Metadata.Key::name, headers::get))
                                        .forEach((key, value) -> log.info("   " + key + "=" + value));
                        

                                return super.delegate();
                            }
                        }, headers);
            }
        };
    }

    public long getLastCall() {
        return callTime;
    }

    @Override
    public int getPriority() {
        return 10;
    }

}
