package gofabian.vertx.web.mount.security;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.ext.auth.User;

import java.util.ArrayList;
import java.util.List;

public class AuthoritiesHelper {

    public static Future<Boolean> hasUserAnyAuthority(User user, List<String> authorities) {
        if (user == null) {
            return Future.failedFuture("Unauthorized");
        }

        return isAuthorized(user, authorities).map(compositeFuture -> {
            for (int i = 0; i < compositeFuture.size(); i++) {
                Boolean isAuthorized = compositeFuture.resultAt(i);
                if (isAuthorized) {
                    return true;
                }
            }
            return false;
        });
    }

    public static Future<Boolean> hasUserAllAuthorities(User user, List<String> authorities) {
        if (user == null) {
            return Future.failedFuture("Unauthorized");
        }

        return isAuthorized(user, authorities).map(compositeFuture -> {
            for (int i = 0; i < compositeFuture.size(); i++) {
                Boolean isAuthorized = compositeFuture.resultAt(i);
                if (!isAuthorized) {
                    return false;
                }
            }
            return true;
        });
    }

    private static CompositeFuture isAuthorized(User user, List<String> authorities) {
        List<Future> futures = new ArrayList<>();
        for (String authority : authorities) {
            Future<Boolean> future = Future.future();
            user.isAuthorized(authority, future);
            futures.add(future);
        }
        return CompositeFuture.all(futures);
    }

}
