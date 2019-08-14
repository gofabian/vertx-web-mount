package gofabian.vertx.web.mount.param;

import gofabian.vertx.web.mount.definition.ParamDefinition;
import io.vertx.core.http.HttpServerRequest;

public class HttpRequestParamProviderFactory implements ParamProviderFactory {
    @Override
    public boolean supports(ParamDefinition paramDefinition) {
        return paramDefinition.getType() == HttpServerRequest.class;
    }

    @Override
    public ParamProvider createParamProvider(ParamDefinition paramDefinition) {
        return context -> context.request();
    }
}
