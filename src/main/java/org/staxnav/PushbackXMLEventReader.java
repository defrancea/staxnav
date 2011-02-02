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

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PushbackXMLEventReader implements XMLEventReader
{

   /** . */
   private static final int PASS_THROUGH = 0;
   
   /** . */
   private static final int QUEUING = 1;

   /** . */
   private int status;
   
   /** . */
   private final LinkedList<XMLEvent> queue = new LinkedList<XMLEvent>();
   
   /** . */
   private final XMLEventReader reader;

   /** . */
   private Integer index;

   public PushbackXMLEventReader(XMLEventReader reader) throws NullPointerException
   {
      if (reader == null)
      {
         throw new NullPointerException();
      }
      this.reader = reader;
      this.status = PASS_THROUGH;
      this.index = null;
   }

   public void mark()
   {
      if (status != PASS_THROUGH)
      {
         throw new IllegalStateException();
      }
      status = QUEUING;
      index = 0;
   }

   public void rollback()
   {
      if (status != QUEUING)
      {
         throw new IllegalStateException();
      }
      status = PASS_THROUGH;
      index = null;
   }

   public void unmark()
   {
      if (status != QUEUING)
      {
         throw new IllegalStateException();
      }
      status = PASS_THROUGH;
      while (index > 0)
      {
         queue.removeFirst();
         index--;
      }
      index = null;
   }

   /**
    * Skip elements until a start element is reached.
    *
    * @return the start element if found, null otherwise
    * @throws XMLStreamException if there is an error with the underlying XML.
    */
   public StartElement skipToStart() throws XMLStreamException, NoSuchElementException
   {
      while (true)
      {
         XMLEvent event = peek();
         if (event == null || event.getEventType() == XMLStreamConstants.START_ELEMENT)
         {
            return (StartElement)event;
         }
         else
         {
            nextEvent();
         }
      }
   }

   public XMLEvent nextEvent() throws XMLStreamException
   {
      switch (status)
      {
         case PASS_THROUGH:
            if (queue.isEmpty())
            {
               return reader.nextEvent();
            }
            else
            {
               return queue.removeFirst();
            }
         case QUEUING:
            if (index < queue.size())
            {
               return queue.get(index++);
            }
            else
            {
               XMLEvent next = reader.nextEvent();
               queue.addLast(next);
               index++;
               return next;
            }
         default:
            throw new AssertionError();
      }
   }

   public XMLEvent peek() throws XMLStreamException
   {
      switch (status)
      {
         case PASS_THROUGH:
            if (queue.isEmpty())
            {
               return reader.peek();
            }
            else
            {
               return queue.peekFirst();
            }
         case QUEUING:
            if (index < queue.size())
            {
               return queue.get(index);
            }
            else
            {
               return reader.peek();
            }
         default:
            throw new AssertionError();
      }
   }

   public XMLEvent nextTag() throws XMLStreamException
   {
      throw new UnsupportedOperationException("to implement");
   }

   public Object getProperty(String name) throws IllegalArgumentException
   {
      return reader.getProperty(name);
   }

   public void close() throws XMLStreamException
   {
      reader.close();
   }

   public boolean hasNext()
   {
      switch (status)
      {
         case PASS_THROUGH:
            return queue.size() > 0 || reader.hasNext();
         case QUEUING:
            return index < queue.size() || reader.hasNext();
         default:
            throw new AssertionError();
      }
   }

   public Object next()
   {
      throw new UnsupportedOperationException("to implement");
   }

   public void remove()
   {
      throw new UnsupportedOperationException();
   }

   public String getElementText() throws XMLStreamException
   {
      // We don't want of this destructive operation
      throw new UnsupportedOperationException();
   }
}
