package org.roussev.selenium4j;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;

/**
 * This class translates each Selenium HTML command to a Java JUnit methods.
 * 
 * Note. Don't auto format this code as it will reduce readability of the
 * generated HTML-to-Java methods.
 * 
 * <p>
 * 
 * @author Atanas Roussev (http://www.roussev.org)
 */
public class CommandToMethodTranslator {

	private final static String SESSION = "session()";
	private final static String TIMEOUT = "1000";
	private final static String LOOP = "60";
	private final static String WAIT_FOR_PAGE_TOLOAD = "30000";

	private final static Map<String, Method> methods = new HashMap<String, Method>();
	private static Logger logger = Logger.getLogger(CommandToMethodTranslator.class);

	static {
		init();
	}

	private static void init() {
		Class<WebDriver> selC = WebDriver.class;
		for (Method m : selC.getMethods()) {
			logger.info("method is " + m);
			Class<?>[] types = m.getParameterTypes();
			if (types.length == 0) {
				methods.put(m.getName(), m);
				continue;
			}
			for (Class<?> t : types) {
				if (!t.isAssignableFrom(String.class)) {
					continue;
				}
			}
			methods.put(m.getName(), m);
		}
	}

	public static String discovery(Command c) {

         return "cheese";
        //old code below
        /*
		Method m = methods.get(c.getName());
		if (m == null) {
			return discoveryCustom(c);
		}
		return _session_getMethod(m, c) + ";";
		*/
	}

	/**
	 * Returns String of the format [session().getMethod("target")]
	 */
	private static String _session_getMethod(Method m, Command c) {
		StringBuilder sb = new StringBuilder(SESSION);
		sb.append(".");
		sb.append(m.getName());
		sb.append("(");
		int i = 1;
		Class<?>[] pTypes = m.getParameterTypes();
		for (Class<?> cl : pTypes) {
			if (cl.isAssignableFrom(String.class)) {
				if (i == 1) {
					sb.append("\"" + filter(c.getTarget()) + "\"");
				} else if (i == 2) {
					sb.append(",");
					sb.append("\"" + filter(c.getValue()) + "\"");
				}
				i++;
			} else {
				throw new RuntimeException("Not a supported type.");
			}
		}
		sb.append(")");
		return sb.toString();
	}

	/**
	 * Returns String of the format ["name", session().getMethod("target")]
	 */
	private static String _compareLeftRight(Method m, Command c) {
		boolean noArgs = m.getParameterTypes().length == 0;
		String left = noArgs ? c.getTarget() : c.getValue();
		return isNumber(m) + filter(left) + isNumber(m) + ", "
				+ _session_getMethod(m, c);
	}

	private static String isNumber(Method m) {
		return m.getReturnType() == Number.class ? "" : "\"";
	}

	private static String _for_block(String condition, Command c) {
		String descr = c.getTarget();
		if (c.getValue() != null && !c.getValue().equals("")) {
			descr = c.getValue();
		}
		descr = c.getName() + ":" + descr;
		return "for (int second = 0;; second++) {" + "\n\t\t	if (second >= "
				+ LOOP + ") fail(\"timeout '" + filter(descr) + "' \");"
				+ "\n\t\t	try { " + condition
				+ " break; } catch (Exception e) {}" + "\n\t\t	Thread.sleep("
				+ TIMEOUT + ");" + "\n\t\t}";
	}

	public static String discoveryCustom(Command c) {

		String result = null;

		if ("".equals(c.getName()) || c.getName() == null) {
			return "";// empty step

		} else if (c.getName().startsWith("waitForNot")) {
			result = doWaitFor(c, "Not", false);

		} else if (c.getName().startsWith("waitFor")
				&& c.getName().endsWith("NotPresent")) {
			result = doWaitFor(c, "", true);

		} else if (c.getName().startsWith("waitFor")) {
			result = doWaitFor(c, "", false);

		} else if (c.getName().startsWith("verifyNot")) {
			result = doVerify(c, "Not", false);

		} else if (c.getName().startsWith("verify")
				&& c.getName().endsWith("NotPresent")) {
			result = doVerify(c, "", true);

		} else if (c.getName().startsWith("verify")) {
			result = doVerify(c, "", false);

		} else if (c.getName().startsWith("assertNot")) {
			result = doAssert(c, "Not", false);

		} else if (c.getName().startsWith("assert")
				&& c.getName().endsWith("NotPresent")) {
			result = doAssert(c, "", true);

		} else if (c.getName().startsWith("assert")) {
			result = doAssert(c, "", false);

		} else if (c.getName().endsWith("AndWait")) {
			result = doAndWait(c);
		} else if (c.getName().startsWith("pause")) {
			result = "try {\n" + "\tThread.sleep(" + c.getValue() + ");\n"
					+ "} catch(Exception e){\n" + "\t// do nothing\n" + "}\n";
		}

		if (result == null) {
			return "ERROR: Method \"" + c.getName() + "\" not supported yet.";
		}

		return result;
	}

	private static String doAssert(Command c, String Not, boolean methodNotPresent) {
		String mName = c.getName().substring(("assert" + Not).length());
		Method m = methods.get("is" + mName);
		if (m != null) {
			return "assert" + Not + "True(" + _session_getMethod(m, c) + ");";
		}
		m = methods.get("get" + mName);
		if (m != null) {
			return "assert" + Not + "Equals(" + _compareLeftRight(m, c) + ");";
		}
		if (methodNotPresent) {
			mName = mName.replace("Not", "");
			m = methods.get("is" + mName);
			return "verifyFalse(" + _session_getMethod(m, c) + ");";
		}
		return null;
	}

	private static String doAndWait(Command c) {
		String mName = c.getName().substring(0,
				c.getName().length() - "AndWait".length());
		Method m = methods.get(mName);
		if (m != null) {
			return doAndWait(m, c);
		}
		return null;
	}

	private static String doVerify(Command c, String Not,
			boolean methodNotPresent) {
		String mName = c.getName().substring(("verify" + Not).length());
		Method m = methods.get("is" + mName);
		if (m != null) {
			return "verify" + Not + "True(" + _session_getMethod(m, c) + ");";
		}
		m = methods.get("get" + mName);
		if (m != null) {
			return "verify" + Not + "Equals(" + _compareLeftRight(m, c) + ");";
		}
		if (methodNotPresent) {
			mName = mName.replace("Not", "");
			m = methods.get("is" + mName);
			return "verifyFalse(" + _session_getMethod(m, c) + ");";
		}
		return null;
	}

	private static String doWaitFor(Command c, String Not,
			boolean methodNotPresent) {
		String mName = c.getName().substring(("waitFor" + Not).length());
		String pipe = "";
		if (methodNotPresent) {
			mName = mName.replace("Not", "");
			pipe = "!";

		} else if (Not.equals("Not")) {
			pipe = "!";
		}
		Method m = methods.get("is" + mName);
		if (m != null) {
			return _for_block("if (" + pipe + SESSION + "." + m.getName()
					+ "(\"" + filter(c.getTarget()) + "\"))", c);
		}
		m = methods.get("get" + mName);
		if (m != null) {
			boolean noArgs = m.getParameterTypes().length == 0;
			if (noArgs) {
				return _for_block("if (" + pipe + "\"" + filter(c.getTarget())
						+ "\".equals(" + SESSION + "." + m.getName() + "()))",
						c);
			} else {
				return _for_block("if (" + pipe + "\"" + filter(c.getValue())
						+ "\".equals(" + SESSION + "." + m.getName() + "(\""
						+ filter(c.getTarget()) + "\")))", c);
			}
		}
		return null;
	}

	private static String doAndWait(Method m, Command c) {
		String s = _session_getMethod(m, c);
		return s + ";\t\t" + SESSION + ".waitForPageToLoad(\""
				+ WAIT_FOR_PAGE_TOLOAD + "\");";
	}

	private static String filter(String s) {
		if (s != null) {
			s = s.replace("\\", "\\\\");
			s = s.replace("\"", "\\\"");
		}
		return s;
	}
}