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

import org.staxnav.xml.AttributeContainer;
import org.staxnav.xml.XMLTokenType;
import org.staxnav.xml.XMLTokenizer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class EventXMLTokenizer implements XMLTokenizer
{

   /** . */
   private static final int PASS_THROUGH = 0;
   
   /** . */
   private static final int QUEUING = 1;

   /** . */
   private int status;
   
   /** . */
   private final CircularList<XMLEvent> queue = new CircularList<XMLEvent>(10);
   
   /** . */
   private final XMLEventReader reader;

   /** . */
   private Integer index;

   public EventXMLTokenizer(XMLEventReader reader) throws NullPointerException
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

   public void skipTo(XMLTokenType wantedType) throws XMLStreamException
   {
      if (wantedType == XMLTokenType.START_ELEMENT)
      {
         while (hasNext())
         {
            XMLTokenType currentType = peek();
            if (currentType == XMLTokenType.START_ELEMENT)
            {
               break;
            }
            else
            {
               next();
            }
         }
      }
      else
      {
         throw new UnsupportedOperationException();
      }
   }

   public XMLTokenType next() throws XMLStreamException
   {
      XMLEvent event;
      switch (status)
      {
         case PASS_THROUGH:
            if (queue.isEmpty())
            {
               event = reader.nextEvent();
            }
            else
            {
               event = queue.removeFirst();
            }
            break;
         case QUEUING:
            if (index < queue.size())
            {
               event = queue.get(index++);
            }
            else
            {
               XMLEvent next = reader.nextEvent();
               queue.addLast(next);
               index++;
               event = next;
            }
            break;
         default:
            throw new AssertionError();
      }
      return unwrap(event);
   }

   public XMLTokenType peek() throws XMLStreamException
   {
      return unwrap(peekEvent());
   }

   private XMLEvent peekEvent() throws XMLStreamException
   {
      XMLEvent event;
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

   public QName getElementName() throws XMLStreamException
   {
      XMLEvent current = peekEvent();
      if (current instanceof StartElement)
      {
         StartElement start = (StartElement)current;
         return start.getName();
      }
      else if (current instanceof EndElement)
      {
         EndElement end = (EndElement)current;
         return end.getName();
      }
      else
      {
         throw new IllegalStateException();
      }
   }

   public void fillAttributes(AttributeContainer container) throws IllegalStateException, XMLStreamException
   {
      XMLEvent current = peekEvent();
      if (current instanceof StartElement)
      {
         StartElement start = (StartElement)current;
         Iterator<Attribute> i = start.getAttributes();
         if (i.hasNext())
         {
            container.start();
            while (i.hasNext())
            {
               Attribute attribute = i.next();
               container.add(attribute.getName(), attribute.getValue());
            }
            container.end();
         }
      }
      else
      {
         throw new IllegalStateException();
      }
   }

   public String getCharacters() throws IllegalStateException, XMLStreamException
   {
      XMLEvent current = peekEvent();
      if (current instanceof Characters)
      {
         Characters characters = (Characters)current;
         return characters.getData();
      }
      else
      {
         throw new IllegalStateException("Was not expecting event " + current);
      }
   }

   private XMLTokenType unwrap(XMLEvent event)
   {
      if (event != null)
      {
         switch (event.getEventType())
         {
            case XMLEvent.START_ELEMENT:
               return XMLTokenType.START_ELEMENT;
            case XMLEvent.END_ELEMENT:
               return XMLTokenType.END_ELEMENT;
            case XMLEvent.CHARACTERS:
               return XMLTokenType.CHARACTERS;
            default:
               return XMLTokenType.UNUSED;
         }
      }
      else
      {
         return null;
      }
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
}
