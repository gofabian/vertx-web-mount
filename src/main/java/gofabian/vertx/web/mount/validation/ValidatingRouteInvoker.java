package gofabian.vertx.web.mount.validation;

import gofabian.vertx.web.mount.invoker.RouteInvoker;
import gofabian.vertx.web.mount.invoker.RouteInvokerImpl;
import io.vertx.core.Future;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;
import java.lang.reflect.Method;
import java.util.Set;

public class ValidatingRouteInvoker implements RouteInvoker {

    private final RouteInvoker routeInvoker;
    private final ExecutableValidator validator;

    public ValidatingRouteInvoker(Validator validator) {
        this(new RouteInvokerImpl(), validator);
    }

    public ValidatingRouteInvoker(RouteInvoker routeInvoker, Validator validator) {
        this.routeInvoker = routeInvoker;
        this.validator = validator.forExecutables();
    }

    @Override
    public Future<?> invoke(Object apiDefinition, Method method, Object[] args) throws Exception {
        // validate arguments
        Set<ConstraintViolation<Object>> argViolations = validator.validateParameters(apiDefinition, method, args);
        if (argViolations.size() > 0) {
            throw new ConstraintViolationException(argViolations);
        }

        // invoke method
        Future<?> future = routeInvoker.invoke(apiDefinition, method, args);

        // validate result
        return future.map(result -> {
            Set<ConstraintViolation<Object>> resultViolations = validator.validateReturnValue(apiDefinition, method, result);
            if (resultViolations.size() > 0) {
                throw new ConstraintViolationException(resultViolations);
            }
            return result;
        });
    }
}
