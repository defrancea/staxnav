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

import junit.framework.TestCase;
import org.staxnav.PushbackXMLEventReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PushbackXMLEventReaderTestCase extends TestCase
{

   private PushbackXMLEventReader reader;

   @Override
   protected void setUp() throws Exception
   {
      XMLInputFactory factory = XMLInputFactory.newInstance();
      InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("sample.xml");
      PushbackXMLEventReader reader = new PushbackXMLEventReader(factory.createXMLEventReader(is));
      this.reader = reader;
   }

   private void assertStartElement(String localName, boolean consume) throws Exception
   {
      assertTrue(reader.hasNext());
      XMLEvent event = consume ? reader.nextEvent() : reader.peek();
      assertEquals(XMLStreamConstants.START_ELEMENT, event.getEventType());
      assertStartElement(localName, (StartElement)event);
   }

   private void assertStartElement(String localName, StartElement start) throws Exception
   {
      assertEquals(localName, start.getName().getLocalPart());
   }

   private void assertNext() throws Exception
   {
      assertNext(null);
   }

   private XMLEvent assertNext(Integer type) throws Exception
   {
      assertTrue(reader.hasNext());
      XMLEvent next = reader.nextEvent();
      if (type != null)
      {
         assertEquals((int)type, next.getEventType());
      }
      return next;
   }

   private void assertChars() throws Exception
   {
      assertNext(XMLStreamConstants.CHARACTERS);
   }

   private void assertChars(String expectedChars) throws Exception
   {
      Characters chars = (Characters)assertNext(XMLStreamConstants.CHARACTERS);
      assertEquals(expectedChars, chars.getData());
   }

   public void testA() throws Exception
   {
      reader.skipToStart();
      reader.mark();
      assertStartElement("foo1", true);
      while (reader.hasNext())
      {
         reader.nextEvent();
      }
      reader.rollback();
      assertTrue(reader.hasNext());
      assertStartElement("foo1", false);
      reader.mark();
      assertTrue(reader.hasNext());
      assertStartElement("foo1", true);
      reader.unmark();
      assertChars();
      assertStartElement("bar1", true);
   }

   public void testRollbackNext() throws Exception
   {
      reader.skipToStart();
      assertStartElement("foo1", true);
      assertChars();
      assertStartElement("bar1", true);
      assertNext();
      assertNext();
      assertNext();
      reader.skipToStart();
      reader.mark();
      assertStartElement("foo2", true);
//      assertEquals(0, reader.getAttributeCount());
      reader.skipToStart();
      assertStartElement("bar2", true);
//      assertEquals(2, reader.getAttributeCount());
//      assertEquals("a", reader.getAttributeLocalName(0));
//      assertEquals("b", reader.getAttributeValue(0));
//      assertEquals("b", reader.getAttributeLocalName(1));
//      assertEquals("c", reader.getAttributeValue(1));

      reader.rollback();
      assertStartElement("foo2", true);
//      assertEquals(0, reader.getAttributeCount());
      reader.skipToStart();
      assertStartElement("bar2", true);
//      assertEquals(2, reader.getAttributeCount());
//      assertEquals("a", reader.getAttributeLocalName(0));
//      assertEquals("b", reader.getAttributeValue(0));
//      assertEquals("b", reader.getAttributeLocalName(1));
//      assertEquals("c", reader.getAttributeValue(1));
      assertChars("2");
   }
}
