package grpcService;

import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.Prioritized;

import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class AuthServerInterceptor implements ServerInterceptor, Prioritized{
    static final Metadata.Key<String> CUSTOM_HEADER_KEY =    Metadata.Key.of("custom_server_header_key", Metadata.ASCII_STRING_MARSHALLER);
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
        ServerCall<ReqT, RespT> call,
        final Metadata requestHeaders,
        ServerCallHandler<ReqT, RespT> next) {
        log.info("header received from client:" + requestHeaders);
        log.info("calling service:"+call.getMethodDescriptor().getServiceName());
        
        return next.startCall(new SimpleForwardingServerCall<ReqT, RespT>(call) {
            @Override
            public void sendHeaders(Metadata responseHeaders) {
                responseHeaders.put(CUSTOM_HEADER_KEY, "customRespondValue");
                super.sendHeaders(responseHeaders);
            }
            }, requestHeaders);
    }

    @Override
    public int getPriority() {
        return 10;
    }

}
