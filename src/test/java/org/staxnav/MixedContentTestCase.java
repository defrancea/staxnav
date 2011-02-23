package org.staxnav;/*
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

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class MixedContentTestCase extends TestCase
{

   /** . */
   private StaxNavigatorImpl<String> navigator;

   @Override
   protected void setUp() throws Exception
   {
      InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("mixedcontent.xml");
      XMLInputFactory factory = XMLInputFactory.newInstance();
      XMLStreamReader stream = factory.createXMLStreamReader(is);

      //
      navigator = new StaxNavigatorImpl<String>(new Naming.Local(), stream);
   }

   public void testFoo() throws XMLStreamException
   {
      assertEquals("foo", navigator.getName());
      assertEquals(null, navigator.getContent());
   }
}
