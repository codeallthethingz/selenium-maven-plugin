package com.wonderant.maven.selenium;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.roussev.selenium4j.Transformer;

/**
 * 
 * @goal generate-selenium-tests
 * 
 * @phase process-test-resources
 */
public class SeleniumMojo extends AbstractMojo {

	/**
	 * Where the selenium html tests are stored
	 * @parameter default-value="${project.basedir}/src/test/selenium";
	 */
	private String seleniumTestDir;

	/**
	 * Where the generated selenium Java tests will be saved
	 * @parameter default-value="${project.build.testSourceDirectory}"
	 */
	private String testSourceDirectory;

	public void execute() throws MojoExecutionException {
		getLog().info("Reading Selenium tests from: " + seleniumTestDir);
		getLog().info("Outputting unit tests to: " + testSourceDirectory);
		if (System.getProperty("smoke") == null){
			getLog().warn("smoke system property not set so these tests will not run, use -Dsmoke");
		}
		Transformer.TEST_BUILD_DIR = testSourceDirectory;
		Transformer.TEST_DIR = seleniumTestDir;
		try {
			new Transformer().execute();
		} catch (Exception e) {
			throw new MojoExecutionException("Couldn't run Transformer", e);
		}

	}
}
