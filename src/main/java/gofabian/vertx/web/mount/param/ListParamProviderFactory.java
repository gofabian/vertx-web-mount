package gofabian.vertx.web.mount.param;

import gofabian.vertx.web.mount.definition.ParamCategory;
import gofabian.vertx.web.mount.definition.ParamDefinition;
import gofabian.vertx.web.mount.request.StringReader;
import io.vertx.ext.web.RoutingContext;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ListParamProviderFactory implements ParamProviderFactory {

    private final StringReader stringReader = new StringReader();

    @Override
    public boolean supports(ParamDefinition paramDefinition) {
        ParamCategory paramCategory = paramDefinition.getCategory();
        if (paramCategory == ParamCategory.QUERY || paramCategory == ParamCategory.HEADER
                || paramCategory == ParamCategory.FORM) {
            Type type = paramDefinition.getType();

            if (type instanceof Class) {
                return true;
            }

            // check for List<String>
            if (type instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) type;
                return pType.getRawType() == List.class;
            }
        }
        return false;
    }

    @Override
    public ParamProvider createParamProvider(ParamDefinition paramDefinition) {
        Objects.requireNonNull(paramDefinition.getName(), "Missing name: " + paramDefinition);
        Objects.requireNonNull(paramDefinition.getType(), "Missing type: " + paramDefinition);

        if (!paramDefinition.isMandatory() && !(paramDefinition.getDefaultValue() instanceof List)) {
            throw new IllegalArgumentException("Default value must be List<String>: " + paramDefinition);
        }

        @SuppressWarnings("unchecked")
        List<String> defaultValues = (List<String>) paramDefinition.getDefaultValue();

        if (paramDefinition.getType() == List.class) {
            // list without parameter type -> return List<String>
            return context -> {
                return getHeaderValuesFrom(context, paramDefinition, defaultValues);
            };
        }

        if (paramDefinition.getType() instanceof Class) {
            // no generic type -> use first value
            return context -> {
                List<String> values = getHeaderValuesFrom(context, paramDefinition, defaultValues);
                if (values.isEmpty()) {
                    throw new IllegalArgumentException("Missing value or default value of parameter: " + paramDefinition);
                }
                String firstValue = values.get(0);
                return stringReader.read(firstValue, paramDefinition.getType());
            };
        }

        // list with parameter type: List<T>
        Type[] typeArgs = ((ParameterizedType) paramDefinition.getType()).getActualTypeArguments();
        Type listType = typeArgs[0];
        return context -> {
            List<String> values = getHeaderValuesFrom(context, paramDefinition, defaultValues);
            return values.stream()
                    .map(v -> stringReader.read(v, listType))
                    .collect(Collectors.toList());
        };
    }

    private List<String> getHeaderValuesFrom(RoutingContext context, ParamDefinition paramDefinition, List<String> defaultValues) {
        List<String> values;
        switch (paramDefinition.getCategory()) {
            case QUERY:
                values = context.queryParam(paramDefinition.getName());
                break;
            case HEADER:
                values = context.request().headers().getAll(paramDefinition.getName());
                break;
            case FORM:
                values = context.request().formAttributes().getAll(paramDefinition.getName());
                break;
            default:
                throw new IllegalArgumentException("Unexpected category: " + paramDefinition.getCategory());
        }

        if (values.isEmpty()) {
            if (paramDefinition.isMandatory()) {
                throw new IllegalArgumentException("Missing value: " + paramDefinition);
            }
            values.addAll(defaultValues);
        }
        return values;
    }

}
