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
import org.staxnav.StaxNavigator;
import org.staxnav.StaxNavigatorImpl;

import java.io.InputStream;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public abstract class AbstractBrowseTestCase<N> extends TestCase
{
   private InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("sample.xml");
   private StaxNavigator<N> navigator = createNavigator(is);

   protected abstract StaxNavigator<N> createNavigator(InputStream is);

   protected abstract N createName(String localPart);

   public final void assertNameEquals(String expectedLocalPart, N name)
   {
      N expectedName = createName(expectedLocalPart);
      assertEquals(expectedName, name);
   }

   public void testInit() throws Exception
   {
      navigator.init();
      assertNameEquals("foo1", navigator.getName());
      assertEquals(1, navigator.getLevel());
   }

   public void testContent() throws Exception
   {
      navigator.init();
      assertNameEquals("bar1", navigator.child());
      assertEquals("1", navigator.getText());
      assertNameEquals("foo2", navigator.sibling());
      assertNameEquals("bar2", navigator.child());
      assertEquals("2", navigator.getText());
      assertEquals(true, navigator.sibling("bar3"));
      assertNameEquals("foo3", navigator.child());
      assertEquals("4", navigator.getText());
      assertEquals(true, navigator.sibling("foobar1"));
      assertEquals("3", navigator.getText());
   }

   public void testChild() throws Exception
   {
      navigator.init();
      assertNameEquals("bar1", navigator.child());
      assertNameEquals("bar1", navigator.getName());
      assertEquals(2, navigator.getLevel());
   }

   public void testChildWithName() throws Exception
   {
      navigator.init();
      assertEquals(true, navigator.child("foobar1"));
      assertNameEquals("foobar1", navigator.getName());
      assertEquals(2, navigator.getLevel());
   }

   public void testChildOver() throws Exception
   {
      navigator.init();
      assertNameEquals("bar1", navigator.child());
      assertNull(navigator.child());
      assertEquals(true, navigator.sibling("foobar1"));
      assertNameEquals("foobar2", navigator.sibling());
   }

   public void testChildWithNameOver() throws Exception
   {
      navigator.init();
      assertEquals(true, navigator.child("foo2"));
      assertFalse(navigator.child("donotexist"));
      assertNameEquals("foo2", navigator.getName());
      assertEquals(2, navigator.getLevel());
      assertEquals(true, navigator.sibling("foobar1"));
      assertNameEquals("foobar2", navigator.sibling());
   }

   public void testsibling() throws Exception
   {
      navigator.init();
      assertNameEquals("bar1", navigator.child());
      assertNameEquals("foo2", navigator.sibling());
      assertNameEquals("foo2", navigator.getName());
      assertEquals(2, navigator.getLevel());
      assertNameEquals("foobar1", navigator.sibling());
      assertNameEquals("foobar1", navigator.getName());
      assertEquals(2, navigator.getLevel());
   }

   public void testsiblingWithName() throws Exception
   {
      navigator.init();
      assertNameEquals("bar1", navigator.child());
      assertEquals(2, navigator.getLevel());
      assertEquals(true, navigator.sibling("foobar1"));
      assertNameEquals("foobar1", navigator.getName());
      assertEquals(2, navigator.getLevel());
   }

   public void testsiblingOver() throws Exception
   {
      navigator.init();
      assertEquals(true, navigator.child("foo2"));
      assertEquals(true, navigator.child("bar3"));
      assertEquals(true, navigator.child("foo3"));
      assertNameEquals("foobar1", navigator.sibling());
      assertNameEquals("foobar2", navigator.sibling());
   }

   public void testsiblingWithNameOver() throws Exception
   {
      navigator.init();
      assertEquals(true, navigator.child("foo2"));
      assertEquals(true, navigator.child("bar2"));
      assertEquals(true, navigator.sibling("foobar2"));
   }

   public void testsiblingEOF() throws Exception
   {
      navigator.init();
      assertEquals(true, navigator.child("foo2"));
      assertEquals(true, navigator.child("bar2"));
      assertEquals(true, navigator.sibling("foobar2"));
      assertNull(navigator.sibling());
      assertNameEquals("foobar2", navigator.getName());
   }

   public void testsiblingWithNameEOF() throws Exception
   {
      navigator.init();
      assertEquals(true, navigator.child("foo2"));
      assertNameEquals("foo2", navigator.getName());
      assertFalse(navigator.sibling("donotexist"));
      assertNameEquals("foo2", navigator.getName());
      assertEquals(2, navigator.getLevel());
      assertEquals(true, navigator.sibling("foobar1"));
      assertNameEquals("foobar1", navigator.getName());
      assertEquals("3", navigator.getText());
      assertEquals(2, navigator.getLevel());
      assertNameEquals("foobar2", navigator.sibling());
   }

   public void testAttribute() throws Exception
   {
      navigator.init();
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
      navigator.init();
      assertEquals(true, navigator.child("foo2"));
      assertEquals(true, navigator.child("bar2"));
      assertNameEquals("bar2", navigator.getName());
      assertFalse(navigator.sibling("donotexist"));
      assertNameEquals("bar2", navigator.getName());
      assertEquals(3, navigator.getLevel());
      assertEquals("b", navigator.getAttribute("a"));
      assertEquals("c", navigator.getAttribute("b"));
      assertEquals(null, navigator.getAttribute("donotexists"));
      assertEquals(true, navigator.sibling("foobar1"));
      assertEquals("bar", navigator.getAttribute("foo"));
   }
}