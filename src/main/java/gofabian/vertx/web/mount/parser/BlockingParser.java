package gofabian.vertx.web.mount.parser;

import gofabian.vertx.web.mount.MountOptions;
import gofabian.vertx.web.mount.definition.BlockingType;
import gofabian.vertx.web.mount.definition.ParamDefinition;
import gofabian.vertx.web.mount.definition.RouteDefinition;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class BlockingParser implements RouteParser {
    @Override
    public void visitClass(Class<?> clazz, RouteDefinition routeDefinition, MountOptions options) {
        visitAnnotatedElement(clazz, routeDefinition);
    }

    @Override
    public void visitMethod(Method method, RouteDefinition routeDefinition, MountOptions options) {
        visitAnnotatedElement(method, routeDefinition);
    }

    private void visitAnnotatedElement(AnnotatedElement element, RouteDefinition routeDefinition) {
        Blocking annotation = element.getAnnotation(Blocking.class);
        if (annotation != null) {
            if (annotation.value()) {
                if (annotation.ordered()) {
                    routeDefinition.setBlockingType(BlockingType.BLOCKING_ORDERED);
                } else {
                    routeDefinition.setBlockingType(BlockingType.BLOCKING_UNORDERED);
                }
            } else {
                routeDefinition.setBlockingType(BlockingType.NONE_BLOCKING);
            }
        }
    }

    @Override
    public void merge(RouteDefinition parent, RouteDefinition child, RouteDefinition result, MountOptions options) {
        BlockingType blockingType = mergeBlockingType(parent, child);
        if (blockingType == null) {
            blockingType = options.getBlockingType();
        }
        result.setBlockingType(blockingType);
    }

    private BlockingType mergeBlockingType(RouteDefinition parent, RouteDefinition child) {
        if (child.getBlockingType() != null) {
            return child.getBlockingType();
        }
        return parent.getBlockingType();
    }

    @Override
    public void visitParameter(Parameter parameter, ParamDefinition paramDefinition, MountOptions options) {
    }
}
