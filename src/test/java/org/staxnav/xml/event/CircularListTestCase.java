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

package org.staxnav.xml.event;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class CircularListTestCase extends TestCase
{

   public void testResizeTailBeforeHead()
   {
      CircularList<String> l = new CircularList<String>(3);
      l.addLast("a");
      l.addLast("b");
      l.addLast("c");
      assertEquals("a", l.peekFirst());
      assertEquals("a", l.get(0));
      assertEquals("b", l.get(1));
      assertEquals("c", l.get(2));
      assertEquals(3, l.size());
   }

   public void testResizeHeadBeforeTail()
   {
      CircularList<String> l = new CircularList<String>(3);
      l.addLast("a");
      l.addLast("b");
      assertEquals("a", l.removeFirst());
      l.addLast("c");
      assertEquals("b", l.removeFirst());
      l.addLast("d");
      assertEquals("c", l.removeFirst());
      l.addLast("e");
      assertEquals("d", l.removeFirst());
      l.addLast("f");
      assertEquals("e", l.removeFirst());
      l.addLast("g");

      //
      l.addLast("h");
      assertEquals("f", l.get(0));
      assertEquals("g", l.get(1));
      assertEquals("h", l.get(2));
      assertEquals(3, l.size());
   }

   public void testRotation()
   {

      CircularList<String> l = new CircularList<String>(3);

      //
      assertEquals(null, l.peekFirst());
      assertEquals(0, l.size());

      //
      l.addLast("a");
      assertEquals("a", l.peekFirst());
      assertEquals("a", l.get(0));
      assertEquals(1, l.size());

      //
      l.addLast("b");
      assertEquals("a", l.peekFirst());
      assertEquals("a", l.get(0));
      assertEquals("b", l.get(1));
      assertEquals(2, l.size());

      //
      assertEquals("a", l.removeFirst());
      assertEquals("b", l.get(0));
      assertEquals(1, l.size());

      //
      l.addLast("c");
      assertEquals("b", l.peekFirst());
      assertEquals("b", l.get(0));
      assertEquals("c", l.get(1));
      assertEquals(2, l.size());

      //
      assertEquals("b", l.removeFirst());
      assertEquals("c", l.get(0));
      assertEquals(1, l.size());

      //
      l.addLast("d");
      assertEquals("c", l.peekFirst());
      assertEquals("c", l.get(0));
      assertEquals("d", l.get(1));
      assertEquals(2, l.size());

      //
      assertEquals("c", l.removeFirst());
      assertEquals("d", l.get(0));
      assertEquals(1, l.size());

      //
      l.addLast("e");
      assertEquals("d", l.peekFirst());
      assertEquals("d", l.get(0));
      assertEquals("e", l.get(1));
      assertEquals(2, l.size());
   }
}
