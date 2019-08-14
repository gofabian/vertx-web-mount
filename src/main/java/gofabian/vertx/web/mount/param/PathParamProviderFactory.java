package gofabian.vertx.web.mount.param;

import gofabian.vertx.web.mount.definition.ParamCategory;
import gofabian.vertx.web.mount.definition.ParamDefinition;
import gofabian.vertx.web.mount.request.StringReader;

import java.util.Objects;

public class PathParamProviderFactory implements ParamProviderFactory {

    private final StringReader stringReader = new StringReader();

    @Override
    public boolean supports(ParamDefinition paramDefinition) {
        return paramDefinition.getCategory() == ParamCategory.PATH;
    }

    @Override
    public ParamProvider createParamProvider(ParamDefinition paramDefinition) {
        Objects.requireNonNull(paramDefinition.getName(), "Missing name: " + paramDefinition);
        Objects.requireNonNull(paramDefinition.getType(), "Missing type: " + paramDefinition);

        if (!paramDefinition.isMandatory() && !(paramDefinition.getDefaultValue() instanceof String)) {
            throw new IllegalArgumentException("Default value must be String: " + paramDefinition);
        }

        return context -> {
            String value = context.pathParam(paramDefinition.getName());
            if (value == null) {
                if (paramDefinition.isMandatory()) {
                    throw new IllegalArgumentException("Parameter is mandatory: " + paramDefinition);
                }
                value = (String) paramDefinition.getDefaultValue();
            }
            return stringReader.read(value, paramDefinition.getType());
        };
    }

}
