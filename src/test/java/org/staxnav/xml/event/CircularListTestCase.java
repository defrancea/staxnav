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

   CircularList<String> l;

   @Override
   protected void setUp() throws Exception
   {
      l = new CircularList<String>(3);
   }

   private void assertFirst(String a)
   {
      assertEquals(a, l.peekFirst());
   }

   private void assertElement(String a, int index)
   {
      assertEquals(a, l.get(index));
   }

   private void addLast(String a)
   {
      l.addLast(a);
   }

   private void removeFirst(String a)
   {
      assertEquals(a, l.removeFirst());
   }

   private void assertSize(int i)
   {
      assertEquals(i, l.size());
   }

   private void assertOut(int i)
   {
      try
      {
         l.get(i);
         fail();
      }
      catch (IndexOutOfBoundsException ignore)
      {
      }
   }

   public void testResizeTailBeforeHead()
   {
      addLast("a");
      addLast("b");
      addLast("c");
      assertFirst("a");
      assertElement("a", 0);
      assertElement("b", 1);
      assertElement("c", 2);
      assertSize(3);
   }

   public void testResizeHeadBeforeTail()
   {
      addLast("a");
      addLast("b");
      removeFirst("a");
      addLast("c");
      removeFirst("b");
      addLast("d");
      removeFirst("c");
      addLast("e");
      removeFirst("d");
      addLast("f");
      removeFirst("e");
      addLast("g");

      //
      addLast("h");
      assertElement("f", 0);
      assertElement("g", 1);
      assertElement("h", 2);
      assertSize(3);
   }

   public void testRotation()
   {
      assertFirst(null);
      assertSize(0);
      assertOut(-1);
      assertOut(0);
      assertOut(1);

      //
      addLast("a");
      assertFirst("a");
      assertElement("a", 0);
      assertOut(-1);
      assertOut(1);
      assertSize(1);

      //
      addLast("b");
      assertFirst("a");
      assertElement("a", 0);
      assertElement("b", 1);
      assertOut(-1);
      assertOut(2);
      assertSize(2);

      //
      removeFirst("a");
      assertElement("b", 0);
      assertOut(-1);
      assertOut(1);
      assertSize(1);

      //
      addLast("c");
      assertFirst("b");
      assertElement("b", 0);
      assertElement("c", 1);
      assertOut(-1);
      assertOut(2);
      assertSize(2);

      //
      removeFirst("b");
      assertElement("c", 0);
      assertOut(-1);
      assertOut(1);
      assertSize(1);

      //
      addLast("d");
      assertFirst("c");
      assertElement("c", 0);
      assertElement("d", 1);
      assertOut(-1);
      assertOut(2);
      assertSize(2);

      //
      removeFirst("c");
      assertElement("d", 0);
      assertOut(-1);
      assertOut(1);
      assertSize(1);

      //
      addLast("e");
      assertFirst("d");
      assertElement("d", 0);
      assertElement("e", 1);
      assertOut(-1);
      assertOut(2);
      assertSize(2);
   }
}
