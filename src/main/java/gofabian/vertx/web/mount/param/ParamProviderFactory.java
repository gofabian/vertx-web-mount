package gofabian.vertx.web.mount.param;

import gofabian.vertx.web.mount.definition.ParamDefinition;

public interface ParamProviderFactory {

    boolean supports(ParamDefinition paramDefinition);

    ParamProvider createParamProvider(ParamDefinition paramDefinition);

}
