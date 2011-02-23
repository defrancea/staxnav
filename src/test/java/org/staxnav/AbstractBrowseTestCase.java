package org.staxnav;/*
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

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public abstract class AbstractBrowseTestCase<N> extends AbstractXMLTestCase
{

   protected abstract Naming<N> getNaming();

   /** . */
   private StaxNavigator<N> navigator;

   /** . */
   private Naming<N> naming;

   @Override
   protected void setUp() throws Exception
   {
      Naming<N> naming = getNaming();
      InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("sample.xml");
      XMLInputFactory factory = XMLInputFactory.newInstance();
      XMLStreamReader stream = factory.createXMLStreamReader(is);

      //
      this.navigator = new StaxNavigatorImpl<N>(naming, stream);
      this.naming = naming;
   }

   protected final StaxNavigator<N> navigator(String document)
   {
      return navigator(naming, document);
   }

   protected N createName(String localPart)
   {
      return naming.getName(null, null, localPart);
   }

   public final void assertNameEquals(String expectedLocalPart, N name)
   {
      N expectedName = createName(expectedLocalPart);
      assertEquals(expectedName, name);
   }

   public void testInit() throws Exception
   {
      assertNameEquals("foo1", navigator.getName());
      assertEquals(1, navigator.getDepth());
   }

   public void testContent() throws Exception
   {
      assertNameEquals("foo1", navigator.getName());
      assertNameEquals("bar1", navigator.child());
      assertEquals("1", navigator.getContent());
      assertNameEquals("foo2", navigator.sibling());
      assertNameEquals("bar2", navigator.child());
      assertEquals("2", navigator.getContent());
      assertEquals(true, navigator.sibling(createName("bar3")));
      assertNameEquals("foo3", navigator.child());
      assertEquals("4", navigator.getContent());
      assertEquals(true, navigator.find(createName("foobar1")));
      assertEquals("3", navigator.getContent());
   }

   public void testChild() throws Exception
   {
      assertNameEquals("foo1", navigator.getName());
      assertNameEquals("bar1", navigator.child());
      assertNameEquals("bar1", navigator.getName());
      assertEquals(2, navigator.getDepth());
   }

   public void testChildWithName() throws Exception
   {
      assertNameEquals("foo1", navigator.getName());
      assertEquals(true, navigator.child(createName("foobar1")));
      assertNameEquals("foobar1", navigator.getName());
      assertEquals(2, navigator.getDepth());
   }

   public void testChildOver() throws Exception
   {
      assertNameEquals("foo1", navigator.getName());
      assertNameEquals("bar1", navigator.child());
      assertNull(navigator.child());
      assertEquals(true, navigator.sibling(createName("foobar1")));
      assertNameEquals("foobar2", navigator.sibling());
   }

   public void testChildWithNameOver() throws Exception
   {
      assertNameEquals("foo1", navigator.getName());
      assertEquals(true, navigator.child(createName("foo2")));
      assertFalse(navigator.child(createName("donotexist")));
      assertNameEquals("foo2", navigator.getName());
      assertEquals(2, navigator.getDepth());
      assertEquals(true, navigator.sibling(createName("foobar1")));
      assertNameEquals("foobar2", navigator.sibling());
   }

   public void testsibling() throws Exception
   {
      assertNameEquals("foo1", navigator.getName());
      assertNameEquals("bar1", navigator.child());
      assertNameEquals("foo2", navigator.sibling());
      assertNameEquals("foo2", navigator.getName());
      assertEquals(2, navigator.getDepth());
      assertNameEquals("foobar1", navigator.sibling());
      assertNameEquals("foobar1", navigator.getName());
      assertEquals(2, navigator.getDepth());
   }

   public void testsiblingWithName() throws Exception
   {
      assertNameEquals("foo1", navigator.getName());
      assertNameEquals("bar1", navigator.child());
      assertEquals(2, navigator.getDepth());
      assertEquals(true, navigator.sibling(createName("foobar1")));
      assertNameEquals("foobar1", navigator.getName());
      assertEquals(2, navigator.getDepth());
   }

   public void testsiblingOver() throws Exception
   {
      assertNameEquals("foo1", navigator.getName());
      assertEquals(true, navigator.child(createName("foo2")));
      assertEquals(true, navigator.child(createName("bar3")));
      assertEquals(true, navigator.child(createName("foo3")));
      assertNull(navigator.sibling());
   }

   public void testsiblingWithNameOver() throws Exception
   {
      assertNameEquals("foo1", navigator.getName());
      assertEquals(true, navigator.child(createName("foo2")));
      assertEquals(true, navigator.child(createName("bar2")));
   }

   public void testsiblingEOF() throws Exception
   {
      assertNameEquals("foo1", navigator.getName());
      assertEquals(true, navigator.child(createName("foo2")));
      assertEquals(true, navigator.child(createName("bar2")));
      assertEquals(true, navigator.find(createName("foobar2")));
      assertNull(navigator.sibling());
      assertNameEquals("foobar2", navigator.getName());
   }

   public void testsiblingWithNameEOF() throws Exception
   {
      assertNameEquals("foo1", navigator.getName());
      assertEquals(true, navigator.child(createName("foo2")));
      assertNameEquals("foo2", navigator.getName());
      assertFalse(navigator.sibling(createName("donotexist")));
      assertNameEquals("foo2", navigator.getName());
      assertEquals(2, navigator.getDepth());
      assertEquals(true, navigator.sibling(createName("foobar1")));
      assertNameEquals("foobar1", navigator.getName());
      assertEquals("3", navigator.getContent());
      assertEquals(2, navigator.getDepth());
      assertNameEquals("foobar2", navigator.sibling());
   }

   public void testAttribute() throws Exception
   {
      assertNameEquals("foo1", navigator.getName());
      navigator.child();
      navigator.sibling();
      navigator.child();
      assertNameEquals("bar2", navigator.getName());
      assertEquals("b", navigator.getAttribute("a"));
      assertEquals("c", navigator.getAttribute("b"));
      assertEquals(null, navigator.getAttribute("donotexists"));
   }

   public void testAttributeInPushback() throws Exception
   {
      assertNameEquals("foo1", navigator.getName());
      assertEquals(true, navigator.child(createName("foo2")));
      assertEquals(true, navigator.child(createName("bar2")));
      assertNameEquals("bar2", navigator.getName());
      assertFalse(navigator.sibling(createName("donotexist")));
      assertNameEquals("bar2", navigator.getName());
      assertEquals(3, navigator.getDepth());
      assertEquals("b", navigator.getAttribute("a"));
      assertEquals("c", navigator.getAttribute("b"));
      assertEquals(null, navigator.getAttribute("donotexists"));
   }

   public void testDescendant1() throws Exception
   {
      assertNameEquals("foo1", navigator.getName());
      assertEquals(-1, navigator.descendant(createName("foo1")));
   }

   public void testDescendant2() throws Exception
   {
      assertNameEquals("foo1", navigator.getName());
      assertEquals(1, navigator.descendant(createName("bar1")));
   }

   public void testDescendant3() throws Exception
   {
      assertNameEquals("foo1", navigator.getName());
      assertEquals(2, navigator.descendant(createName("bar2")));
   }

   public void testDescendant4() throws Exception
   {
      assertNameEquals("foo1", navigator.getName());
      assertEquals(-1, navigator.descendant(createName("blah")));
   }

   public void testNext1() throws Exception
   {
      assertNameEquals("foo1", navigator.getName());
      assertEquals(createName("bar1"), navigator.next());
      assertEquals(createName("foo2"), navigator.next());
      assertEquals(createName("bar2"), navigator.next());
      assertEquals(createName("bar3"), navigator.next());
      assertEquals(createName("foo3"), navigator.next());
      assertEquals(createName("foobar1"), navigator.next());
      assertEquals(createName("foobar2"), navigator.next());
   }

   public void testNext2() throws Exception
   {
      assertNameEquals("foo1", navigator.getName());
      assertEquals(false, navigator.next(createName("foo2")));
      assertEquals(createName("bar1"), navigator.next());
      assertEquals(true, navigator.next(createName("foo2")));
      assertEquals(true, navigator.next(createName("bar2")));
      assertEquals(true, navigator.next(createName("bar3")));
      assertEquals(true, navigator.next(createName("foo3")));
      assertEquals(createName("foobar1"), navigator.next());
      assertEquals(createName("foobar2"), navigator.next());
   }

   public void testfind1() throws Exception
   {
      assertNameEquals("foo1", navigator.getName());
      assertEquals(true, navigator.find(createName("bar2")));
      assertEquals(true, navigator.find(createName("foobar1")));
   }

   public void testEnd() throws Exception
   {
      assertNameEquals("foo1", navigator.getName());
      assertEquals(true, navigator.find(createName("foobar2")));
      assertEquals(null, navigator.next());
   }

   public void testNested() throws Exception
   {
      assertNameEquals("foo1", navigator.getName());
      assertEquals(true, navigator.find(createName("foo2")));
      StaxNavigator<N> bilto = navigator.fork();
      assertEquals(createName("foo2"), bilto.getName());
      assertEquals(createName("bar2"), bilto.next());
      assertEquals(createName("bar3"), bilto.next());
      assertEquals(createName("foo3"), bilto.next());
      assertEquals(null, bilto.next());
   }
}