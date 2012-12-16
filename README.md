Selenium Maven Plugin
=====================

This project builds upon the work of http://code.google.com/p/selenium4j 

Selenium4j is ant based, and we wanted a maven based approach to running our selenium tests.

We use selenium IDE to record our tests.  We then save the test cases into our project 
in the following fashion: (Note: currently the code from selenium4j only suports one level, so 
don't nest your folders)

	./src/test/selenium
	    |-signin
	        |-TestLoginGoodPasswordSmoke.html
	     	|-TestLoginBadPasswordSmoke.html
	     	|-selenium4j.properties
	     	
We didn't save the test suites.  They're not needed as maven takes care of finding your tests.

The selenium maven plugin which is bound to the process-test-resources, then converts these
html files into junit 4 tests in your src/test/java folder.

Nifty.

Setup
-----

1. Add the selenium-maven-plugin library to your pom.

		<dependency>
			<groupId>com.gbi.maven</groupId>
			<artifactId>selenium-maven-plugin</artifactId>
			<version>1.0</version>
			<scope>test</scope>
		</dependency>
	
	The jar isn't in a repository anywhere so you can either use it with a 
	
		<systemPath>../path/to/jarfile.jar</systemPath>
		
	or download the source and install it into your local repository with the command
	
	    mvn install 

2. Make sure you have these in your path if you intend to use chrome or IE - firefox just 
seems to work.
	
	http://code.google.com/p/selenium/wiki/InternetExplorerDriver
	http://code.google.com/p/selenium/wiki/ChromeDriver

3. Bind the selenium plugin to the process-test-resources phase

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

Separating your Smoke tests
---------------------------

We named all our selenium HTML tests with a Smoke.html extension.  That way, we were
able to separate the selenium tests into a profile run of surefire so 
they didn't run with the rest of the unit tests.


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
