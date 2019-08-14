package gofabian.vertx.web.mount.param;

import gofabian.vertx.web.mount.definition.ParamDefinition;
import io.vertx.core.Vertx;

public class VertxParamProviderFactory implements ParamProviderFactory {
    @Override
    public boolean supports(ParamDefinition paramDefinition) {
        return paramDefinition.getType() == Vertx.class;
    }

    @Override
    public ParamProvider createParamProvider(ParamDefinition paramDefinition) {
        return context -> context.vertx();
    }
}
