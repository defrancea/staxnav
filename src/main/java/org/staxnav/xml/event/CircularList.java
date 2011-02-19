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

import java.util.NoSuchElementException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class CircularList<E>
{

   /** . */
   private Object[] elements;

   /** . */
   private int head;

   /** . */
   private int tail;

   CircularList(int capacity)
   {
      this.elements = new Object[capacity];
      this.head = 0;
      this.tail = 0;
   }

   E peekFirst()
   {
      if (head != tail)
      {
         @SuppressWarnings("unchecked")
         E first = (E)elements[head % elements.length];
         return first;
      }
      else
      {
         return null;
      }
   }

   E removeFirst()
   {
      if (head == tail)
      {
         throw new NoSuchElementException("Cannot remove from empty");
      }
      int index = head % elements.length;
      @SuppressWarnings("unchecked")
      E first = (E)elements[index];
      elements[index] = null;
      head = (head + 1) % elements.length;
      return first;
   }

   void addLast(E element)
   {
      // Ensure capacity
      if ((tail + 1) % elements.length == head)
      {
         Object[] tmp = new Object[(elements.length * 3) / 2 + 1];
         if (head < tail)
         {
            System.arraycopy(elements, head, tmp, head, tail - head);
            elements = tmp;
         }
         else
         {
            System.arraycopy(elements, 0, tmp, 0, tail);
            System.arraycopy(elements, head, tmp, tmp.length - (elements.length - head), elements.length - head);
            head += tmp.length - elements.length;
            elements = tmp;
         }
      }
      elements[tail] = element;
      tail = (tail + 1) % elements.length;
   }

   boolean isEmpty()
   {
      return head == tail;
   }

   int size()
   {
      return (tail - head + elements.length) % elements.length;
   }

   E get(int index)
   {
      if (index > tail && index < head)
      {
         throw new IndexOutOfBoundsException();
      }
      @SuppressWarnings("unchecked")
      E element = (E)elements[(head + index) % elements.length];
      return element;
   }
}
