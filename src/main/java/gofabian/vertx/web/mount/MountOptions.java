package gofabian.vertx.web.mount;

import gofabian.vertx.web.mount.definition.BlockingType;

import java.util.Objects;

public class MountOptions {

    private boolean isAuthenticationRequired;
    private BlockingType blockingType = BlockingType.NONE_BLOCKING;

    public boolean isAuthenticationRequired() {
        return isAuthenticationRequired;
    }

    public MountOptions setAuthenticationRequired(boolean isAuthenticationRequired) {
        this.isAuthenticationRequired = isAuthenticationRequired;
        return this;
    }

    public BlockingType getBlockingType() {
        return blockingType;
    }

    public MountOptions setBlockingType(BlockingType blockingType) {
        Objects.requireNonNull(blockingType);
        this.blockingType = blockingType;
        return this;
    }
}
