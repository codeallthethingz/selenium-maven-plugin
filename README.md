Selenium Maven Plugin
=====================

This project builds upon the work of http://code.google.com/p/selenium4j 

Selenium4j is ant based, and we wanted a maven based approach to running our selenium tests.
Currently this is only tested on windows. (and in fact we use a specific windows tool - pskill - 
so we guarantee it won't work correctly on linux (fork away ;)) 

We use selenium IDE to record our tests.  We then saved the test cases into our project 
in the following fashion: (Note: currently the code from selenium4j only suports one level, so 
don't nest your folders)

	./src/test/selenium
	    |-signin
	        |-TestLoginGoodPasswordSmoke.html
	     	|-TestLoginBadPasswordSmoke.html
	     	|-selenium4j.properties
	     	
We didn't save the test suites as maven takes care of finding your tests.

The selenium4j.properties contains setup information about:

	# the web site being tested
	webSite=http://yourwebapp:8080
	
	# A comma separated values of the WebDrivers being used. Accepted drivers: 
	# HtmlUnitDriver, FirefoxDriver, ChromeDriver, InternetExplorerDriver
	driver=FirefoxDriver
	        
	# How many times we want to iterate and test
	loopCount=1

The selenium maven plugin, which is bound to the process-test-resources phase, then converts these
html files into junit 4 tests in your src/test/java folder.

So you end up with:

	./src/test/java
	    |-signin
	    	|-firefox
	            |-TestLoginGoodPasswordSmoke.java
	     	    |-TestLoginBadPasswordSmoke.java
	     	    

Setup
-----

Now the fun of integrating the needed xml into your pom.

1. Add the selenium-maven-plugin library to your pom.

		<dependency>
			<groupId>com.gbi.maven</groupId>
			<artifactId>selenium-maven-plugin</artifactId>
			<version>1.0.2</version>
			<scope>test</scope>
		</dependency>
	
	The jar isn't in a public repository so you can either use it with a 
	
		<systemPath>/path/to/jarfile.jar</systemPath>
		
	or download the source and install it into your local repository with the command
	
	    mvn install 
	    
	As github just removed file downloads, looks like its going to be the second option.

2. Make sure you have these browser drivers in your path if you intend to use chrome or IE - firefox just 
seems to work.
	
	http://code.google.com/p/selenium/wiki/InternetExplorerDriver

	http://code.google.com/p/selenium/wiki/ChromeDriver

3. To make sure the browsers get killed after each test make sure pskill is on your path

	http://live.sysinternals.com/pskill.exe

4. Bind the selenium plugin to the process-test-resources phase

		<build><plugins>...
		<plugin>
			<groupId>com.gbi.maven</groupId>
			<artifactId>selenium-maven-plugin</artifactId>
			<version>1.0.2</version>
			<executions>
				<execution>
					<phase>process-test-resources</phase>
					<goals>
						<goal>generate-selenium-tests</goal>
					</goals>
				</execution>
			</executions>
		</plugin>
		...</plugins></build>
		
You should now be able to run

	mvn clean compile test -Dsmoke
	
and have the plugin compile your selenium tests into junit tests and then have maven run them.

Separating your Smoke tests
---------------------------

Smoke tests are automatically separated out from your unit tests with the assume junit method.  

You can run

	mvn test -Dsmoke
	
and this will run your unit tests and smoke tests.

For those that are interested, this is separation is done with the setup method:

	@Before
	public void before() {
		org.junit.Assume.assumeTrue(null != System.getProperty("smoke"));
	}

Maven Configuration
-------------------

There are two things you can override in the configuration element of the plugin

1. Where the html files are read from.

		<configuration>
			<seleniumTestDir>${project.basedir}/src/somewhereElse</seleniumTestDir>
		</configuration>
		
2. Where the junit java files end up.

		<configuration>
			<testSourceDirectory>${project.basedir}/src/test-selenium</testSourceDirectory>
		</configuration>
