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

import junit.framework.Assert;
import org.staxnav.xml.event.EventXMLTokenizer;

import static junit.framework.Assert.*;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TokenizerAsserter extends EventXMLTokenizer
{

   public TokenizerAsserter(XMLEventReader reader) throws NullPointerException
   {
      super(reader);
   }

   public void assertStartElement(String localName, boolean consume) throws Exception
   {
      Assert.assertTrue(hasNext());
      XMLTokenType type = peek();
      assertEquals(XMLTokenType.START_ELEMENT, type);
      assertEquals(localName, getElementName().getLocalPart());
      if (consume)
      {
         next();
      }
   }

   public void assertChars() throws Exception
   {
      assertChars(null);
   }

   public void assertChars(String expectedChars) throws Exception
   {
      assertTrue(hasNext());
      XMLTokenType type = peek();
      assertEquals(XMLTokenType.CHARACTERS, type);
      if (expectedChars != null)
      {
         assertEquals(expectedChars, getCharacters());
      }
      next();
   }

   public void assertNext() throws Exception
   {
      assertNext(null);
   }

   public void assertNext(XMLTokenType expectedType) throws Exception
   {
      assertTrue(hasNext());
      XMLTokenType type = next();
      if (expectedType != null)
      {
         assertEquals(expectedType, type);
      }
   }
}
