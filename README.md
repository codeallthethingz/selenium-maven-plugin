Selenium Maven Plugin
=====================

This project builds upon the work of http://code.google.com/p/selenium4j 

Selenium4j is ant based, and we wanted a maven based approach to running our selenium tests.

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
			<version>1.0</version>
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
			<version>1.0</version>
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

	mvn clean compile test 
	
and have the plugin compile your selenium tests into junit tests and then have maven run them.

Separating your Smoke tests
---------------------------

We named all our selenium HTML tests with a Smoke.html extension.  That way, we were
able to separate the selenium tests into a profile, to ensure surefire 
didn't run them with the rest of the unit tests.


Step 1, turn off surefire tests for matches against Smoke.java

	<build>
		...
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-surefire-plugin</artifactId>
				<configuration>
					<excludes>
						<exclude>**/*Smoke.java</exclude>
					</excludes>
				</configuration>
			</plugin>
		...
		</plugins>
		...
	</build>
	
	
Step 2, turn on tests for a smoke specific profile (we also turned off the unit tests)

	<profiles>
		<profile>
			<activation>
				<property>
					<name>smoke</name>
				</property>
			</activation>
			<build>
				<plugins>
					<plugin>
						<groupId>org.apache.maven.plugins</groupId>
						<artifactId>maven-surefire-plugin</artifactId>
						<configuration>
							<excludes>
								<exclude>**/*Test.java</exclude>
							</excludes>
							<includes>
								<include>**/*Smoke.java</include>
							</includes>
						</configuration>
					</plugin>
				</plugins>
			</build>
		</profile>
	</profiles>
	

Now you can run

	mvn test -Dsmoke
	
and you should only run your smoke tests.

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
