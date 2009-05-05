package org.sakaiproject.component.kerberos.user;
import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * Simple callback handler that supplies a username and password.
 * @author buckett
 */
public class UsernamePasswordCallback implements CallbackHandler {

	private final String username;
	private final String password;
	
	public UsernamePasswordCallback(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public void handle(Callback[] callbacks) throws IOException,
			UnsupportedCallbackException {
		for (Callback callback: callbacks) {
			if (callback instanceof NameCallback) {
				NameCallback nameCallback = (NameCallback)callback;
				nameCallback.setName(username);
			} else if (callback instanceof PasswordCallback) {
				PasswordCallback passwordCallback = (PasswordCallback)callback;
				passwordCallback.setPassword(password.toCharArray());
			} else {
				throw new UnsupportedCallbackException(callback, "Only username and password supported.");
			}
		}

	}

}
