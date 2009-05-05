package org.sakaiproject.component.kerberos.user;
import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;


/**
 * Callback handler that doesn't support anything. This is used when the login is
 * done using a keychain.
 * @author buckett
 *
 */
public class NullCallbackHandler implements CallbackHandler {

	public void handle(Callback[] callbacks) throws IOException,
			UnsupportedCallbackException {
		for (Callback callback: callbacks) {
			throw new UnsupportedCallbackException(callback, "Can't handle any callbacks");
		}

	}

}
