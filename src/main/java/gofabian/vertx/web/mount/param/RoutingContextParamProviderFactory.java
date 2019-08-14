package gofabian.vertx.web.mount.param;

import gofabian.vertx.web.mount.definition.ParamDefinition;
import io.vertx.ext.web.RoutingContext;

public class RoutingContextParamProviderFactory implements ParamProviderFactory {
    @Override
    public boolean supports(ParamDefinition paramDefinition) {
        return paramDefinition.getType() == RoutingContext.class;
    }

    @Override
    public ParamProvider createParamProvider(ParamDefinition paramDefinition) {
        return context -> context;
    }
}
