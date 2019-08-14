package gofabian.vertx.web.mount.definition;

import java.lang.reflect.Type;

public class ParamDefinition {

    private String name;
    private ParamCategory category;
    private Type type;
    private boolean isMandatory = true;
    private Object defaultValue;

    public String getName() {
        return name;
    }

    public ParamDefinition setName(String name) {
        this.name = name;
        return this;
    }

    public ParamCategory getCategory() {
        return category;
    }

    public ParamDefinition setCategory(ParamCategory category) {
        this.category = category;
        return this;
    }

    public Type getType() {
        return type;
    }

    public ParamDefinition setType(Type type) {
        this.type = type;
        return this;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    public ParamDefinition setMandatory(boolean mandatory) {
        isMandatory = mandatory;
        return this;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public ParamDefinition setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    @Override
    public String toString() {
        return "ParamDefinition{" +
                "name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", type=" + type +
                ", isMandatory=" + isMandatory +
                ", defaultValue=" + defaultValue +
                '}';
    }
}
