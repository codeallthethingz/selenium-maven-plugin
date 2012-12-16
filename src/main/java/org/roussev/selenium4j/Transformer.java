package org.roussev.selenium4j;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.roussev.selenium4j.VelocityBean.DriverBean;

/**
 * This is the main class iterating through Selenium HTML suite files,
 * discovering the HTML tests for each suite and transforming the tests and
 * suites into Java JUnti source files.
 * <p>
 * 
 * @author Atanas Roussev (http://www.roussev.org)
 * 
 */
public class Transformer {

	private static Logger logger = Logger.getLogger(Transformer.class);

	private static final String CONF_FILE = "selenium4j.properties";
	public static String TEST_DIR = "./test-html";
	public static String TEST_BUILD_DIR = "./test-java";
	private final static String SELENIUM_TEST_TEMPLATE = "SeleniumJava.vm";

	private final static String PROP_DRIVER = "driver";
	private final static String PROP_WEBSITE = "webSite";
	private final static String PROP_LOOPCOUNT = "loopCount";
	private final static String PROP_CONCURRENT_USERS = "cuncurrentUsers";

	public static void main(String[] args) throws Exception {
		new Transformer().execute();
	}

	public void execute() throws Exception {
		File buildDir = new File(TEST_BUILD_DIR);
		buildDir.mkdirs();
		read(new DefaultMethodReader());
	}

	private static String getFileNameNoSuffix(File f) {
		String fileName = f.getName();
		return fileName.substring(0, fileName.lastIndexOf('.'));

	}

	private final void read(MethodReader methodReader) throws Exception {
		File dir = new File(TEST_DIR);

		FileFilter dirFilter = new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory() && !file.getName().equals(".svn");
			}
		};
		for (File suiteDir : dir.listFiles(dirFilter)) {
			doTests(suiteDir, methodReader, null);
		}
	}

	private void doTests(File dir, MethodReader methodReader,
			VelocityBean confBean_) throws Exception {
		logger.debug("Reading " + dir + " tests...");
		VelocityBean velocityBean = new VelocityBean();
		if (confBean_ != null) {
			velocityBean.setSubstituteEntries(confBean_.getSubstituteEntries());
			velocityBean.setSuiteContext(confBean_.getSuiteContext());
		}

		loadTestProperties(dir, velocityBean);

		Collection<File> files = Arrays.asList(dir
				.listFiles(new FilenameFilter() {
					public boolean accept(File pArg0, String pArg1) {
						return pArg1.endsWith(".html");
					}
				}));

		String packName = dir.getName();

		for (File f : files) {
			logger.info("Processing: " + f.getName());
			StringBuilder sb = new StringBuilder();
			String className = getFileNameNoSuffix(f);
			Collection<Command> cmds = TestParser.parseHTML(f);
			for (Command c : cmds) {
				String cmdStr = CommandToMethodTranslator.discovery(c);
				cmdStr = getPopulatedCmd(className, cmdStr, velocityBean);
				sb.append("\n\t\t" + cmdStr);
			}

			ClassBean classBean = new ClassBean();
			classBean.setPackageName(packName);
			classBean.setClassName(clean(className));
			classBean.setMethodBody(sb.toString());
			classBean.setWebSite(velocityBean.getWebsite());
			writeTestFile(dir, methodReader, classBean, velocityBean);
		}

		// createAllTests(classBeans, velocityBean, packName, dir.getName());
		// doSetupTeardownTests(dir, methodReader, SETUP_DIR, velocityBean);
		// doSetupTeardownTests(dir, methodReader, TEARDOWN_DIR, velocityBean);
	}

	private String clean(String pClassName) {
		return StringUtils.capitalize(pClassName.replaceAll("[^a-z^A-Z^0-9]",
				""));
	}

	private String getPopulatedCmd(String className, String cmdStr,
			VelocityBean velocityBean) {
		Map<String, String> subEntries = velocityBean
				.getSubstituteEntries(className);
		for (String key : subEntries.keySet()) {
			if (cmdStr.contains("\"" + key + "\"")) {
				String ek = subEntries.get(key);
				return cmdStr.replace("\"" + key + "\"", "get(\"" + ek + "\")");
			}
		}
		return cmdStr;
	}

	private void loadTestProperties(File dir, VelocityBean velocityBean)
			throws Exception {
		File propFile = new File(dir, CONF_FILE);
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(propFile));
			String bp = properties.getProperty(PROP_DRIVER);
			String[] bpArr = bp.split(",");
			for (int i = 0; i < bpArr.length; i++) {
				if (bpArr[i] == null || bpArr[i].trim().equals("")) {
					continue;
				}
				DriverBean bProxy = new DriverBean(bpArr[i]);
				velocityBean.addDriverBean(bProxy);
			}
			velocityBean.setLoopCount(properties.getProperty(PROP_LOOPCOUNT));
			velocityBean.setCuncurrentUsers(properties
					.getProperty(PROP_CONCURRENT_USERS));
			velocityBean.setWebsite(properties.getProperty(PROP_WEBSITE));

		} catch (FileNotFoundException e1) {
			throw new RuntimeException("Missing \"" + CONF_FILE + "\" file at "
					+ dir + ".");
		}
	}

	private void writeTestFile(File dir, MethodReader methodReader,
			ClassBean classBean, VelocityBean velocityBean) throws Exception {
		for (DriverBean bp : velocityBean.getDriverBeans()) {
			String subPackage = filterSubPackage(bp);
			methodReader.read(dir, subPackage, classBean, bp);
		}
	}

	private static String filterSubPackage(DriverBean bp) {
		return bp.getDriver().substring(0, bp.getDriver().indexOf("Driver"))
				.toLowerCase();
	}

	// -----------
	static class DefaultMethodReader implements MethodReader {
		private static final String S = File.separator;

		public void read(File dir, String subPackage, ClassBean classBean,
				DriverBean driverBean) throws Exception {
			String dirName = new File(dir.getName() + S + subPackage).getName();
			File packageDir = new File(TEST_BUILD_DIR + S
					+ classBean.getPackageName() + S + dirName);
			packageDir.mkdirs();
			VelocityTestTranslator t = new VelocityTestTranslator(
					SELENIUM_TEST_TEMPLATE);

			t.doWrite(classBean, driverBean, dirName,
					packageDir.getAbsolutePath() + S + classBean.getClassName()
							+ ".java");
		}
	}

	interface MethodReader {
		void read(File dir, String subPackage, ClassBean classBean,
				DriverBean driverBean) throws Exception;
	}

}
