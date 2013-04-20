package org.roussev.selenium4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

public class WebTest {

	private static final Logger log = Logger.getLogger(WebTest.class);

	private WebDriverBackedSelenium selenium;
	private String driver = null;

	@Before
	public void before() {
		if (System.getProperty("smoke") == null) {
			log.info("skipping test, smoke not set");
		}
		org.junit.Assume.assumeTrue(null != System.getProperty("smoke"));
	}

	protected void startSelenium(String pDriver, String pWebsite) {
		WebDriver webDriver = null;
		driver = pDriver;
		if (FirefoxDriver.class.getSimpleName().equals(pDriver)) {
			webDriver = new FirefoxDriver();

		} else if (ChromeDriver.class.getSimpleName().equals(pDriver)) {
			webDriver = new ChromeDriver();

		} else if (HtmlUnitDriver.class.getSimpleName().equals(pDriver)) {
			webDriver = new HtmlUnitDriver();

		} else if (InternetExplorerDriver.class.getSimpleName().equals(pDriver)) {
			webDriver = new InternetExplorerDriver();

		} else {
			throw new UnsupportedOperationException("Driver '" + pDriver
					+ "' is not supported.");
		}

		selenium = new WebDriverBackedSelenium(webDriver, pWebsite);
	}

	public WebDriverBackedSelenium session() {
		return selenium;
	}

	public void verifyTrue(boolean b) {
		if (!b) {
			throw new IllegalStateException("test failed");
		}
	}

	public void verifyNotTrue(boolean b) {
		if (b) {
			throw new IllegalStateException("test failed");
		}
	}

	@After
	public void tearDown() {
		try {
			List<String> browserExes = getBrowserExe();
			if (browserExes != null) {
				for (String browserExe : browserExes) {
					Runtime.getRuntime().exec("pskill " + browserExe);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private List<String> getBrowserExe() {
		if (FirefoxDriver.class.getSimpleName().equals(driver)) {
			return Arrays.asList("firefox.exe");
		} else if (ChromeDriver.class.getSimpleName().equals(driver)) {
			return Arrays.asList("chromedriver", "chrome.exe");
		} else if (InternetExplorerDriver.class.getSimpleName().equals(driver)) {
			return Arrays.asList("IEDriverServer.exe", "iexplore.exe");
		}
		return null;
	}

}
