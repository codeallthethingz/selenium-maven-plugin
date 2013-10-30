package org.roussev.selenium4j;

import static org.junit.Assert.*;

import org.junit.Test;

public class CommandToMethodTranslatorTest {
	

	@Test
	public void testAssertEquals() throws Exception {
		Command c = new Command();
		c.setName("assertText");
		c.setTarget("aoeu");
		c.setValue("aoeu");
		String discoveryCustom = CommandToMethodTranslator.discovery(c);
		assertEquals("assertEquals(\"aoeu\", session().getText(\"aoeu\"));", discoveryCustom);
	}
	@Test
	public void testAssertXpathCount() throws Exception {
		Command c = new Command();
		c.setName("assertXpathCount");
		c.setTarget("//li[@class='filterItem clearfix']");
		c.setValue("2");
		String discoveryCustom = CommandToMethodTranslator.discovery(c);
		assertEquals("assertEquals(2, session().getXpathCount(\"//li[@class='filterItem clearfix']\"));", discoveryCustom);
	}
}
