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

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class BrowseTestCase extends TestCase
{
   private StaxNavigator navigator = null;
   /*
      <foo>
         <bar>1</bar>
         <foo>
            <bar>2</bar>
         <foo>
         <foobar>3</foobar>
      </foo>
    */
   public void testBrowseNotRecursive() throws Exception {
      navigator.next("foo");
      navigator.child("bar");
      try
      {
         navigator.child("bar"); // Exception
         fail();
      }
      catch (Exception e)
      {

      }
   }

   public void testRecursive() throws Exception {
      navigator.next("foo");
      navigator.child("bar");
      navigator.next("bar", true); // Ok
   }

   public void testOutOfScope() throws Exception {
      navigator.next("foo");
      navigator.child("foo");
      try
      {
         navigator.child("foobar"); // Exception : not child
         fail();
      }
      catch (Exception e)
      {

      }
   }

   public void testName() throws Exception {
      navigator.next("foo");
      assertEquals("foo", navigator.getName());
      navigator.child("foo", true);
      assertEquals("foo", navigator.getName());
      navigator.child("bar");
      assertEquals("foo", navigator.getName());
   }

   public void testValue() throws Exception {
      navigator.next("foo");
      navigator.child("bar");
      assertEquals("1", navigator.getText());
      navigator.next("bar", true);
      assertEquals("2", navigator.getText());
      navigator.next("foobar");
      assertEquals("3", navigator.getText());
   }
}
