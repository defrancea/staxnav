/*
 * Copyright (C) 2010 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.staxnav.xml;

import junit.framework.TestCase;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class XMLTokenizerTestCase extends TestCase
{

   protected abstract TokenizerAsserter tokenizer(InputStream in) throws XMLStreamException;

   /** . */
   private TokenizerAsserter tokenizer;

   @Override
   protected void setUp() throws Exception
   {
      InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("sample.xml");
      this.tokenizer = tokenizer(is);
   }

   public final void testA() throws Exception
   {
      tokenizer.skipTo(XMLTokenType.START_ELEMENT);
      tokenizer.mark();
      tokenizer.assertStartElement("foo1", true);
      while (tokenizer.hasNext())
      {
         tokenizer.next();
      }
      tokenizer.rollback();
      assertTrue(tokenizer.hasNext());
      tokenizer.assertStartElement("foo1", false);
      tokenizer.mark();
      assertTrue(tokenizer.hasNext());
      tokenizer.assertStartElement("foo1", true);
      tokenizer.unmark();
      tokenizer.assertChars();
      tokenizer.assertStartElement("bar1", true);
   }

   public void testRollbackNext() throws Exception
   {
      tokenizer.skipTo(XMLTokenType.START_ELEMENT);
      tokenizer.assertStartElement("foo1", true);
      tokenizer.assertChars();
      tokenizer.assertStartElement("bar1", true);
      tokenizer.assertNext();
      tokenizer.assertNext();
      tokenizer.assertNext();
      tokenizer.skipTo(XMLTokenType.START_ELEMENT);
      tokenizer.mark();
      tokenizer.assertStartElement("foo2", true);
//      assertEquals(0, reader.getAttributeCount());
      tokenizer.skipTo(XMLTokenType.START_ELEMENT);
      tokenizer.assertStartElement("bar2", true);
//      assertEquals(2, reader.getAttributeCount());
//      assertEquals("a", reader.getAttributeLocalName(0));
//      assertEquals("b", reader.getAttributeValue(0));
//      assertEquals("b", reader.getAttributeLocalName(1));
//      assertEquals("c", reader.getAttributeValue(1));

      tokenizer.rollback();
      tokenizer.assertStartElement("foo2", true);
//      assertEquals(0, reader.getAttributeCount());
      tokenizer.skipTo(XMLTokenType.START_ELEMENT);
      tokenizer.assertStartElement("bar2", true);
//      assertEquals(2, reader.getAttributeCount());
//      assertEquals("a", reader.getAttributeLocalName(0));
//      assertEquals("b", reader.getAttributeValue(0));
//      assertEquals("b", reader.getAttributeLocalName(1));
//      assertEquals("c", reader.getAttributeValue(1));
      tokenizer.assertChars("2");
   }
}
