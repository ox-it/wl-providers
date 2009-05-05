package org.sakaiproject.component.kerberos.user;
/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/


import java.security.PrivilegedAction;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

/*
 * JaasTest -- attempts to authenticate a user and reports success or an error message
 * Argument: LoginContext [optional, default is "JaasAuthentication"]
 *	(must exist in "login configuration file" specified in ${java.home}/lib/security/java.security)
 *
 * @author Matthew Buckett
 *
 */
public class JassAuthenticate {

	private final static Log log = LogFactory.getLog(JassAuthenticate.class);
	
	private GSSContext clientContext;
	private GSSContext serverContext;
	
	private byte[] acceptTokens = new byte[0];
	private byte[] initTokens = new byte[0];
	
	private String serverGSS;
	private int exchangeLimit = 50;

	private String servicePrincipal;

	private String userPrincipal;

	public JassAuthenticate(String serverGSS, String servicePrincipal, String userPrincipal) {
		this.serverGSS = serverGSS;
		this.servicePrincipal = servicePrincipal;
		this.userPrincipal = userPrincipal;
	}
	
	private class InitiatorAction implements PrivilegedAction<Void> {
		public Void run() {
			try {
				initTokens = clientContext.initSecContext(acceptTokens, 0, acceptTokens.length);
			} catch (GSSException e) {
				throw new RuntimeException("Failed to initiate.", e);
			}
			return null;
		}
	}
	
	private class AcceptorAction implements PrivilegedAction<Void> {
		public Void run() {
			try {
				acceptTokens = serverContext.acceptSecContext(initTokens, 0, initTokens.length);
			} catch (GSSException e) {
				throw new RuntimeException("Failed to accept.", e);
			}
			return null;
		}
	}

	public boolean attemptAuthentication(String username, String password) {
		LoginContext userLoginContext = null;
		LoginContext serverLoginContext = null;

		try {
			// This may well fail so run catch exceptions here.
			try {
				userLoginContext = new LoginContext(userPrincipal, new UsernamePasswordCallback(username, password));
				userLoginContext.login();
			} catch (LoginException le) {
				if (log.isDebugEnabled()) {
					log.debug("Failed to authenticate "+ username, le);
				}
				return false;
			}
			// Shouldn't ever fail
			serverLoginContext = new LoginContext(servicePrincipal, new NullCallbackHandler());
			serverLoginContext.login();


			GSSManager manager = GSSManager.getInstance();
			Oid kerberos = new Oid("1.2.840.113554.1.2.2");

			GSSName serverName = manager.createName(
					serverGSS, GSSName.NT_HOSTBASED_SERVICE);

			clientContext = manager.createContext(
					serverName, kerberos, null,
					GSSContext.DEFAULT_LIFETIME);

			serverContext = manager.createContext((GSSCredential)null);

			int exchanges = 0;
			while (!clientContext.isEstablished() && !serverContext.isEstablished() && !(initTokens == null && acceptTokens == null)) {
				Subject.doAs(userLoginContext.getSubject(), new InitiatorAction());
				Subject.doAs(serverLoginContext.getSubject(), new AcceptorAction());
				log.debug("Tokens exchanged.");
				if (++exchanges > exchangeLimit) {
					throw new RuntimeException("Too many tickets exchanged ("+ exchangeLimit+ ").");
				}
			}
			return true;
		} catch (GSSException gsse) {
			log.warn("Failed to verify ticket.", gsse);
		} catch (LoginException le) {
			log.warn("Failed to login with keytab.", le);
		} finally {
			try {
			if (clientContext != null) 
				clientContext.dispose();
			if (serverContext != null)
				serverContext.dispose();

			if (userLoginContext != null)
				userLoginContext.logout();
			if (serverLoginContext!= null)
				serverLoginContext.logout();
			} catch (Exception e) {
				log.error("Failed to tidy up after attempting authentication.", e);
			}
		}
		return false;
	}
}