package org.roussev.selenium4j;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

import com.thoughtworks.selenium.Selenium;

/**
 * 
 * <p>
 * 
 * @author Atanas Roussev (http://www.roussev.org)
 */
public abstract class MainTestCase extends TestCase {

	private static Logger logger = Logger.getLogger(MainTestCase.class);

	private static SeleniumThreadLocal<DriverSelenium> threadLocalSelenium = new SeleniumThreadLocal<DriverSelenium>();

	private final Map<String, String> suiteContext = new HashMap<String, String>();

	@Override
	public void setUp() throws Exception {
		logger.debug("Setting up " + this + " ...");

		// // A "base url", used by selenium to resolve relative URLs
		// String baseUrl = "http://www.google.com";

		// String packageName = this.getClass().getPackage().getName();
		// String masterPackage = packageName.substring(
		// 0,
		// packageName.indexOf('.'));
		// Class allSuiteClass = Class.forName(masterPackage + ".AllSuite");
		// Method m = allSuiteClass.getMethod("getContext", new Class[] {});
		// Map<String, String> resultMap = (Map<String, String>) m.invoke(
		// allSuiteClass.newInstance(),
		// new Object[] {});
		// suiteContext.putAll(resultMap);
		//
		// logger.info("Setup complete for " + this + " with context: " +
		// suiteContext);
	}

	public String get(String key) {
		return Utilities.getContextValue(key, suiteContext);
	}

	public static void main(String[] args) {
	}

	// @Override
	// public void tearDown() throws Exception {
	// logger.debug("tearDown " + this + " with context: " + suiteContext);
	// closeSeleniumSession();
	// logger.debug("tearDown complete for " + this);
	// }

	protected boolean dontCloseBrowserOnMessage(String text) {
		if (session().isTextPresent(text)) {
			threadLocalSelenium.setInError(true);
			return true;
		}
		return false;
	}

	protected void dontCloseBrowser() {
		threadLocalSelenium.setInError(true);
	}

	protected static void startSeleniumSession(String driver, String webSite) {
		logger.debug("starting SeleniumSession... ");

		WebDriver webDriver = null;

		if (FirefoxDriver.class.getSimpleName().equals(driver)) {
			webDriver = new FirefoxDriver();

		} else if (ChromeDriver.class.getSimpleName().equals(driver)) {
			webDriver = new ChromeDriver();

		} else if (HtmlUnitDriver.class.getSimpleName().equals(driver)) {
			webDriver = new HtmlUnitDriver();

		} else if (InternetExplorerDriver.class.getSimpleName().equals(driver)) {
			webDriver = new InternetExplorerDriver();

		} else {
			throw new UnsupportedOperationException("Driver '" + driver
					+ "' is not supported.");
		}

	//	WebDriverBackedSelenium selenium = new WebDriverBackedSelenium(	webDriver, webSite);

		DriverSelenium driverSelenium = new DriverSelenium();
		driverSelenium.setDriver(webDriver);
	//	driverSelenium.setSelenium(selenium);
		threadLocalSelenium.set(driverSelenium);

		logger.debug("SeleniumSession started.");
	}

	protected static void closeSeleniumSession() throws Exception {
		logger.debug("closing SeleniumSession... ");
		if (null != session()) {
			if (!threadLocalSelenium.isInError()) {
				session().stop();
				((DriverSelenium) threadLocalSelenium.get()).getDriver()
						.close();
			}
			resetSession();
		}
		logger.debug("SeleniumSession closed. ");
	}

	protected static Selenium session() {
		return ((DriverSelenium) threadLocalSelenium.get()).getSelenium();
	}

	protected static void resetSession() {
		logger.debug("resetting SeleniumSession... ");
		threadLocalSelenium.setInError(false);
		threadLocalSelenium.set(null);
		logger.debug("SeleniumSession reset ");
	}

	/** ********** Inner class ********* */
	private static class DriverSelenium {
		private Selenium selenium;
		private WebDriver driver;

		public Selenium getSelenium() {
			return selenium;
		}

		public void setSelenium(Selenium selenium) {
			this.selenium = selenium;
		}

		public WebDriver getDriver() {
			return driver;
		}

		public void setDriver(WebDriver driver) {
			this.driver = driver;
		}

	}


	private static class SeleniumThreadLocal<T> extends ThreadLocal<T> {
		private boolean isInError;

		public boolean isInError() {
			return isInError;
		}

		public void setInError(boolean isInError) {
			this.isInError = isInError;
		}
	}
	/** ********** Inner class ********* */
}
