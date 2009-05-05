package org.sakaiproject.component.kerberos.user;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Properties;
import java.util.Random;

import junit.framework.TestCase;

public class ThreadedJaasAuthenticateTest extends TestCase {

	private int loopLimit = 1000;
	private int threadCount = 10;
	
	private String goodUser;
	private String goodPass;
	
	private String badUser;
	private String badPass;
	
	public ThreadedJaasAuthenticateTest() {
		Properties props = new Properties();
		try {
			props.load(getClass().getResourceAsStream("/users.properties"));
		} catch (IOException e) {
			throw new IllegalStateException("Can't load users file.", e);
		}
		goodUser = props.getProperty("good.user");
		goodPass = props.getProperty("good.pass");
		badUser = props.getProperty("bad.user");
		badPass = props.getProperty("bad.pass");
	}
	
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testThreads() throws InterruptedException {
		UncaughtExceptionHandler handler = Thread.currentThread().getUncaughtExceptionHandler();
		Thread[] threads = new Thread[threadCount];
		Random rnd = new Random();
		for (int i = 0; i < threadCount ; i++) {
			String name;
			if (rnd.nextBoolean()) {
				name = "Thread-"+ i+ "-good";
				threads[i] = new Thread(new Authenticate(goodUser, goodPass, true), name);
			} else {
				name = "Thread-"+ i+ "-bad";
				threads[i] = new Thread(new Authenticate(badUser, badPass, false), name);
			}
			threads[i].setUncaughtExceptionHandler(handler);
			threads[i].start();
			System.out.println("Started "+ name);
		}
		for (Thread thread: threads) {
			thread.join();
		}
	}
	
	private class Authenticate implements Runnable {

		String username;
		String password;
		boolean good;
		
		private Authenticate(String username, String password, boolean good) {
			this.username = username;
			this.password = password;
			this.good = good;
		}
		
		public void run() {
			for(int i = 0; i< loopLimit; i++) {
				JassAuthenticate jass = new JassAuthenticate("sakai@bit.oucs.ox.ac.uk", "servicePrincipal", "userPrincipal");
				assertEquals(good,jass.attemptAuthentication(username, password));
				//System.out.println(Thread.currentThread().getName());
			}
		}
		
	}

}
