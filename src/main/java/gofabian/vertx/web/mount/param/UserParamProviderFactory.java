package gofabian.vertx.web.mount.param;

import gofabian.vertx.web.mount.definition.ParamDefinition;
import io.vertx.ext.auth.User;

public class UserParamProviderFactory implements ParamProviderFactory {
    @Override
    public boolean supports(ParamDefinition paramDefinition) {
        return paramDefinition.getType() == User.class;
    }

    @Override
    public ParamProvider createParamProvider(ParamDefinition paramDefinition) {
        return context -> context;
    }
}
