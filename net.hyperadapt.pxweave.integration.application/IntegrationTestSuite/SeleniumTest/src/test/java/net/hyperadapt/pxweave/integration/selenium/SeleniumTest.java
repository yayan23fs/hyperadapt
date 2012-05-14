package net.hyperadapt.pxweave.integration.selenium;

import org.testng.annotations.Test;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

/**
 * This class creates a penetration test suite with the selenium framework to
 * simulate different parallel user interactions.
 * 
 * @author Martin Lehmann
 * 
 */
public class SeleniumTest {

	private boolean doPenetrationTest = false;

	private static final int timeToWait = 500;
	private static final String waitForPageLoad = "100000";
	private static final String URL = "http://localhost:8080/jsfpxweave/hello.html";

	private void doIt() {
		if (doPenetrationTest) {
			Selenium selenium = new DefaultSelenium("localhost", 4444,
					"*firefox", URL);
			selenium.start();
			selenium.open(URL);
			selenium.waitForPageToLoad(waitForPageLoad);
			try {
				Thread.sleep(timeToWait);
			} catch (Exception e) {
			}
			selenium.open(URL);
			selenium.waitForPageToLoad(waitForPageLoad);
			try {
				Thread.sleep(timeToWait);
			} catch (Exception e) {
			}
			selenium.open(URL);
			selenium.waitForPageToLoad(waitForPageLoad);
			try {
				Thread.sleep(timeToWait);
			} catch (Exception e) {
			}
			selenium.open(URL);
			selenium.waitForPageToLoad(waitForPageLoad);
			try {
				Thread.sleep(timeToWait);
			} catch (Exception e) {
			}
			selenium.open(URL);
			selenium.waitForPageToLoad(waitForPageLoad);
			try {
				Thread.sleep(timeToWait);
			} catch (Exception e) {
			}
			selenium.open(URL);
			selenium.waitForPageToLoad(waitForPageLoad);
			try {
				Thread.sleep(timeToWait);
			} catch (Exception e) {
			}
			selenium.open(URL);
			selenium.waitForPageToLoad(waitForPageLoad);
			try {
				Thread.sleep(timeToWait);
			} catch (Exception e) {
			}
			selenium.open(URL);
			selenium.waitForPageToLoad(waitForPageLoad);
			try {
				Thread.sleep(timeToWait);
			} catch (Exception e) {
			}
			selenium.open(URL);
			selenium.waitForPageToLoad(waitForPageLoad);
			try {
				Thread.sleep(timeToWait);
			} catch (Exception e) {
			}
			selenium.open(URL);
			selenium.waitForPageToLoad(waitForPageLoad);
			try {
				Thread.sleep(timeToWait);
			} catch (Exception e) {
			}
			selenium.open(URL);
			selenium.waitForPageToLoad(waitForPageLoad);
			selenium.stop();
		}
	}

	@Test
	public void testUser1() {
		doIt();
	}

	@Test
	public void testUser2() {
		try {
			Thread.sleep(100);
		} catch (Exception e) {
		}
		doIt();
	}

	@Test
	public void testUser3() {
		try {
			Thread.sleep(300);
		} catch (Exception e) {
		}
		doIt();
	}

	@Test
	public void testUser4() {
		try {
			Thread.sleep(500);
		} catch (Exception e) {
		}
		doIt();
	}

	@Test
	public void testUser5() {
		try {
			Thread.sleep(700);
		} catch (Exception e) {
		}
		doIt();
	}

}
