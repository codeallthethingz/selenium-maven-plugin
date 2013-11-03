package org.roussev.selenium4j;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CommandToMethodTranslatorTest {

	@Test
	public void testAssertEquals() throws Exception {
		Command c = new Command();
		c.setName("assertText");
		c.setTarget("aoeu");
		c.setValue("aoeu");
		String discoveryCustom = CommandToMethodTranslator.discovery(c);
		assertEquals("assertEquals(\"aoeu\", session().getText(\"aoeu\"));",
				discoveryCustom);
	}

	@Test
	public void testAssertXpathCount() throws Exception {
		Command c = new Command();
		c.setName("assertXpathCount");
		c.setTarget("//li[@class='filterItem clearfix']");
		c.setValue("2");
		String discoveryCustom = CommandToMethodTranslator.discovery(c);
		assertEquals(
				"assertEquals(2, session().getXpathCount(\"//li[@class='filterItem clearfix']\"));",
				discoveryCustom);
	}

	@Test
	public void testSendKeys() throws Exception {
		Command c = new Command();
		c.setName("typeKeys");
		c.setTarget("//li[@class='filterItem clearfix']");
		c.setValue("aoeu");
		String discoveryCustom = CommandToMethodTranslator.discovery(c);
		assertEquals(
				"session().typeKeys(\"//li[@class='filterItem clearfix']\",\"aoeu\");",
				discoveryCustom);
	}

	@Test
	public void testPause() throws Exception {
		Command c = new Command();
		c.setName("pause");
		c.setTarget("//li[@class='filterItem clearfix']");
		c.setValue("aoeu");
		String discoveryCustom = CommandToMethodTranslator.discovery(c);
		assertEquals("try {\n" + "	Thread.sleep(aoeu);\n"
				+ "} catch(Exception e){\n" + "	// do nothing\n" + "}\n" + "",
				discoveryCustom);
	}
}
