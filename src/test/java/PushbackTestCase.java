/*
* Copyright (C) 2003-2009 eXo Platform SAS.
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
import org.staxnav.wrapper.PushbackAttribute;
import org.staxnav.wrapper.PushbackXMLStreamReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class PushbackTestCase extends TestCase
{
   private InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("sample.xml");
   private XMLInputFactory factory = XMLInputFactory.newInstance();

   public void testRollbackNext() throws Exception
   {
      PushbackXMLStreamReader reader = new PushbackXMLStreamReader(factory.createXMLStreamReader(is));
      reader.nextTag();
      assertEquals("foo1", reader.getLocalName());
      reader.nextTag();
      assertEquals("bar1", reader.getLocalName());
      reader.next();
      reader.next();
      reader.next();
      reader.mark();
      reader.nextTag();
      assertEquals("foo2", reader.getLocalName());
      assertEquals(0, reader.getAttributeCount());
      reader.nextTag();
      assertEquals("bar2", reader.getLocalName());
      assertEquals(2, reader.getAttributeCount());
      assertEquals("a", reader.getAttributeLocalName(0));
      assertEquals("b", reader.getAttributeValue(0));
      assertEquals("b", reader.getAttributeLocalName(1));
      assertEquals("c", reader.getAttributeValue(1));
      reader.rollbackToMark();
      assertEquals("foo2", reader.getLocalName());
      assertEquals(0, reader.getAttributeCount());
      reader.nextTag();
      assertEquals("bar2", reader.getLocalName());
      assertEquals(2, reader.getAttributeCount());
      assertEquals("a", reader.getAttributeLocalName(0));
      assertEquals("b", reader.getAttributeValue(0));
      assertEquals("b", reader.getAttributeLocalName(1));
      assertEquals("c", reader.getAttributeValue(1));
      reader.next();
      assertEquals("2", reader.getText());
   }

   public void testFlushPushback() throws Exception
   {
      PushbackXMLStreamReader reader = new PushbackXMLStreamReader(factory.createXMLStreamReader(is));
      reader.nextTag();
      assertEquals("foo1", reader.getLocalName());
      reader.nextTag();
      assertEquals("bar1", reader.getLocalName());
      reader.next();
      reader.next();
      reader.next();
      reader.mark();
      reader.nextTag();
      assertEquals("foo2", reader.getLocalName());
      assertEquals(0, reader.getAttributeCount());
      reader.nextTag();
      assertEquals("bar2", reader.getLocalName());
      assertEquals("a", reader.getAttributeLocalName(0));
      assertEquals("b", reader.getAttributeValue(0));
      assertEquals("b", reader.getAttributeLocalName(1));
      assertEquals("c", reader.getAttributeValue(1));
      reader.flushPushback();
      assertEquals("bar2", reader.getLocalName());
      assertEquals("a", reader.getAttributeLocalName(0));
      assertEquals("b", reader.getAttributeValue(0));
      assertEquals("b", reader.getAttributeLocalName(1));
      assertEquals("c", reader.getAttributeValue(1));
      reader.next();
      assertEquals("2", reader.getText());
   }

   public void testDoubleMark() throws Exception
   {
      PushbackXMLStreamReader reader = new PushbackXMLStreamReader(factory.createXMLStreamReader(is));
      reader.mark();
      try
      {
         reader.mark();
         fail("Double mark must be throw an IllegalStageException");
      }
      catch (IllegalStateException e)
      {
         // ok
      }
   }
}
