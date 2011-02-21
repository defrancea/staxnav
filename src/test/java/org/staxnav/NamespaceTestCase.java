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

package org.staxnav;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class NamespaceTestCase extends StaxNavigatorTestCase
{

   public void testA() throws Exception
   {
      StaxNavigator<String> navigator = navigator(new Naming.Local(), "namespace1.xml");
      assertEquals("foo", navigator.getName());
      assertEquals("http://www.w3.org/TR/html4/", navigator.getNamespaceByPrefix(""));
      assertEquals(null, navigator.getNamespaceByPrefix("ns"));
      assertEquals("bar", navigator.next());
      assertEquals("http://www.w3.org/TR/html4/", navigator.getNamespaceByPrefix(""));
      assertEquals(null, navigator.getNamespaceByPrefix("ns"));
   }

   public void testB() throws Exception
   {
      StaxNavigator<String> navigator = navigator(new Naming.Local(), "namespace2.xml");
      assertEquals("foo", navigator.getName());
      assertEquals("http://www.w3.org/TR/html4/", navigator.getNamespaceByPrefix("ns"));
      assertEquals("bar", navigator.next());
      assertEquals("http://www.w3.org/2000/svg", navigator.getNamespaceByPrefix("ns"));
      assertEquals("juu", navigator.next());
      assertEquals("http://www.w3.org/2000/svg", navigator.getNamespaceByPrefix("ns"));
   }
}
