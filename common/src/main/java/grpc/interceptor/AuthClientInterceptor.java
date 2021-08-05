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
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class AuthClientInterceptor implements ClientInterceptor, Prioritized {
    @Inject
    BearerAuthHolder authHolder;

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions, Channel next) {
        log.info("sending call service:" + method.getFullMethodName());
        
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                /* put custom header */
                if (authHolder!=null && authHolder.getBearerAuthKey()!=null &&  authHolder.getBearerAuthKey().trim().length()>0) {
                    log.info("client is forwarding BearerAuthKey");
                    headers.put(Constants.AUTHORIZATION_METADATA_KEY, authHolder.getBearerAuthKey());
                }//if
                super.start(
                        new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                            @Override
                            protected Listener<RespT> delegate() {
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

    @Override
    public int getPriority() {
        return 10;
    }

}
