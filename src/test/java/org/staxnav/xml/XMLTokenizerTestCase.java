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

   protected abstract XMLTokenizer tokenizer(InputStream in) throws XMLStreamException;

   /** . */
   private XMLTokenizer tokenizer;

   @Override
   protected void setUp() throws Exception
   {
      InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("sample.xml");
      this.tokenizer = tokenizer(is);
   }

   private void assertStartElement(String localName, boolean consume) throws Exception
   {
      assertTrue(tokenizer.hasNext());
      XMLTokenType type = tokenizer.peek();
      assertEquals(XMLTokenType.START_ELEMENT, type);
      assertEquals(localName, tokenizer.getElementName());
      if (consume)
      {
         tokenizer.next();
      }
   }

   private void assertChars() throws Exception
   {
      assertChars(null);
   }

   private void assertChars(String expectedChars) throws Exception
   {
      assertTrue(tokenizer.hasNext());
      XMLTokenType type = tokenizer.peek();
      assertEquals(XMLTokenType.CHARACTERS, type);
      if (expectedChars != null)
      {
         assertEquals(expectedChars, tokenizer.getCharacters());
      }
      tokenizer.next();
   }

   private void assertNext() throws Exception
   {
      assertNext(null);
   }

   private void assertNext(XMLTokenType expectedType) throws Exception
   {
      assertTrue(tokenizer.hasNext());
      XMLTokenType type = tokenizer.next();
      if (expectedType != null)
      {
         assertEquals(expectedType, type);
      }
   }

   public final void testA() throws Exception
   {
      tokenizer.skipTo(XMLTokenType.START_ELEMENT);
      tokenizer.mark();
      assertStartElement("foo1", true);
      while (tokenizer.hasNext())
      {
         tokenizer.next();
      }
      tokenizer.rollback();
      assertTrue(tokenizer.hasNext());
      assertStartElement("foo1", false);
      tokenizer.mark();
      assertTrue(tokenizer.hasNext());
      assertStartElement("foo1", true);
      tokenizer.unmark();
      assertChars();
      assertStartElement("bar1", true);
   }

   public void testRollbackNext() throws Exception
   {
      tokenizer.skipTo(XMLTokenType.START_ELEMENT);
      assertStartElement("foo1", true);
      assertChars();
      assertStartElement("bar1", true);
      assertNext();
      assertNext();
      assertNext();
      tokenizer.skipTo(XMLTokenType.START_ELEMENT);
      tokenizer.mark();
      assertStartElement("foo2", true);
//      assertEquals(0, reader.getAttributeCount());
      tokenizer.skipTo(XMLTokenType.START_ELEMENT);
      assertStartElement("bar2", true);
//      assertEquals(2, reader.getAttributeCount());
//      assertEquals("a", reader.getAttributeLocalName(0));
//      assertEquals("b", reader.getAttributeValue(0));
//      assertEquals("b", reader.getAttributeLocalName(1));
//      assertEquals("c", reader.getAttributeValue(1));

      tokenizer.rollback();
      assertStartElement("foo2", true);
//      assertEquals(0, reader.getAttributeCount());
      tokenizer.skipTo(XMLTokenType.START_ELEMENT);
      assertStartElement("bar2", true);
//      assertEquals(2, reader.getAttributeCount());
//      assertEquals("a", reader.getAttributeLocalName(0));
//      assertEquals("b", reader.getAttributeValue(0));
//      assertEquals("b", reader.getAttributeLocalName(1));
//      assertEquals("c", reader.getAttributeValue(1));
      assertChars("2");
   }
}
