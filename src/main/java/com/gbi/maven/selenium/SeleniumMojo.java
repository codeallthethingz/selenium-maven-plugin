package com.gbi.maven.selenium;

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
	 * @parameter default-value="${project.basedir}/src/test/selenium";
	 */
	private String seleniumTestDir;

	/**
	 * @parameter default-value="${project.build.testSourceDirectory}"
	 */
	private String testSourceDirectory;

	public void execute() throws MojoExecutionException {
		getLog().info("reading selenium tests from: " + seleniumTestDir);
		getLog().info("outputting unit test to: " + testSourceDirectory);

		Transformer.TEST_BUILD_DIR = testSourceDirectory;
		Transformer.TEST_DIR = seleniumTestDir;
		try {
			new Transformer().execute();
		} catch (Exception e) {
			throw new MojoExecutionException("Couldn't run Transformer", e);
		}

	}
}
