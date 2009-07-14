package org.sakaiproject.component.kerberos.user;

import junit.framework.TestCase;

public class SimpleJassAuthenticateTest extends TestCase {
	
	private JassAuthenticate jass;

	public void setUp() throws Exception {
		super.setUp();
		jass = new JassAuthenticate("sakai@bit.oucs.ox.ac.uk", "servicePrincipal", "userPrincipal");
	}

	public void testGood() {
		assertTrue(jass.attemptAuthentication("username", "password"));
	}
	
	public void testBad() {
		assertFalse(jass.attemptAuthentication("username", "wrong password"));
	}
		
}
