package gofabian.vertx.web.mount;

public class MountOptions {

    private boolean isAuthenticationRequired;

    public boolean isAuthenticationRequired() {
        return isAuthenticationRequired;
    }

    public MountOptions setAuthenticationRequired(boolean authenticationRequired) {
        isAuthenticationRequired = authenticationRequired;
        return this;
    }
}
