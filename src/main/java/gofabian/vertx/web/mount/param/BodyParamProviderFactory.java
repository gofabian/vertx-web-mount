package gofabian.vertx.web.mount.param;

import gofabian.vertx.web.mount.request.RequestReader;
import gofabian.vertx.web.mount.definition.ParamCategory;
import gofabian.vertx.web.mount.definition.ParamDefinition;

public class BodyParamProviderFactory implements ParamProviderFactory {

    private final RequestReader requestReader;

    public BodyParamProviderFactory(RequestReader requestReader) {
        this.requestReader = requestReader;
    }

    @Override
    public boolean supports(ParamDefinition paramDefinition) {
        return paramDefinition.getCategory() == ParamCategory.BODY;
    }

    @Override
    public ParamProvider createParamProvider(ParamDefinition paramDefinition) {
        return context -> requestReader.read(context, paramDefinition.getType(), requestReader);
    }

}
