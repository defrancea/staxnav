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

package org.staxnav;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public abstract class AbstractStaxNavigator<N> implements StaxNavigator<N>
{
   private InputStream is;
   private PushbackXMLEventReader reader;
   private State state;

   public AbstractStaxNavigator(final InputStream is)
   {
      this.is = is;
      this.state = new State();
   }

   public N root() throws XMLStreamException
   {
      XMLInputFactory factory = XMLInputFactory.newInstance();
      this.reader = new PushbackXMLEventReader(factory.createXMLEventReader(is));
      reader.skipToStart();
      return state.push(reader).name;
   }

   public N next() throws XMLStreamException
   {
      return _next(null, null);
   }

   public boolean next(N name) throws XMLStreamException
   {
      if (name == null)
      {
         throw new NullPointerException("No null name accepted");
      }
      return name.equals(_next(getURI(name), getLocalPart(name)));
   }

   private N _next(final String namespaceURI, final String localPart) throws XMLStreamException
   {
      check();
      reader.mark();
      State backup = new State(state);
      while (reader.hasNext())
      {
         XMLEvent event = reader.peek();
         switch (event.getEventType())
         {
            case XMLStreamReader.START_ELEMENT:
               state.push(reader);
               if (state.matchName(namespaceURI, localPart))
               {
                  reader.unmark();
                  return state.peekName();
               }
               else
               {
                  reader.rollback();
                  state = backup;
                  return null;
               }
            case XMLStreamReader.END_ELEMENT:
               reader.nextEvent();
               state.pop();
               break;
            default:
               reader.nextEvent();
         }
      }
      reader.rollback();
      state = backup;
      return null;
   }

   public N child() throws XMLStreamException
   {
      check();
      return _child(null, null);
   }

   public boolean child(N name) throws NullPointerException, XMLStreamException
   {
      if (name == null)
      {
         throw new NullPointerException("No null name accepted");
      }
      return name.equals(_child(getURI(name), getLocalPart(name)));
   }

   public String getAttribute(String name) throws NullPointerException, IllegalStateException
   {
      if (name == null)
      {
         throw new NullPointerException("No null name accepted");
      }

      //
      return state.currentAttributs.get(name);
   }

   private N _child(final String namespaceURI, final String localPart) throws XMLStreamException
   {
      check();
      reader.mark();
      State backup = new State(state);
      int currentLevel = state.getLevel();
      while (reader.hasNext())
      {
         XMLEvent event = reader.peek();
         switch (event.getEventType())
         {
            case XMLStreamReader.START_ELEMENT:
               state.push(reader);
               if (currentLevel + 1 == state.getLevel())
               {
                  if (state.matchName(namespaceURI, localPart))
                  {
                     reader.unmark();
                     return state.peekName();
                  }
               }
               break;

            case XMLStreamReader.END_ELEMENT:
               reader.nextEvent();
               if (currentLevel == state.getLevel())
               {
                  reader.rollback();
                  state = backup;
                  return null;
               }
               else
               {
                  state.pop();
               }
               break;
            default:
               reader.nextEvent();
         }
      }
      throw new AssertionError("This statement should not be reached");
   }

   public int descendant(N name) throws NullPointerException, XMLStreamException
   {
      if (name == null)
      {
         throw new NullPointerException("No null name accepted");
      }
      return _descendant(getURI(name), getLocalPart(name));
   }

   public int _descendant(final String namespaceURI, final String localPart) throws NullPointerException, XMLStreamException
   {
      check();
      reader.mark();
      State backup = new State(state);

      int currentLevel = state.getLevel();
      while (reader.hasNext())
      {
         if (state.matchName(namespaceURI, localPart))
         {
            return state.getLevel() - currentLevel;
         }
         XMLEvent event = reader.peek();
         switch (event.getEventType())
         {
            case XMLStreamReader.START_ELEMENT:
               state.push(reader);
               break;
            case XMLStreamReader.END_ELEMENT:
               reader.nextEvent();
               if (currentLevel == state.getLevel())
               {
                  reader.rollback();
                  state = backup;
                  return -1;
               }
               else
               {
                  state.pop();
               }
               break;
            default:
               reader.nextEvent();
         }
      }

      //
      return -1;
   }

   public N sibling() throws XMLStreamException
   {
      check();
      return _sibling(null, null);
   }

   public boolean sibling(N name) throws NullPointerException, XMLStreamException
   {
      return name.equals(_sibling(getURI(name), getLocalPart(name)));
   }

   private N _sibling(final String namespaceURI, final String name) throws XMLStreamException
   {
      check();
      reader.mark();
      State backup = new State(state);
      int currentLevel = state.getLevel();
      while (reader.hasNext())
      {
         XMLEvent event = reader.peek();
         switch (event.getEventType())
         {
            case XMLStreamReader.START_ELEMENT:
               state.push(reader);
               if (currentLevel == state.getLevel())
               {
                  if (state.matchName(namespaceURI, name))
                  {
                     reader.unmark();
                     return state.peekName();
                  }
               }
               break;

            case XMLStreamReader.END_ELEMENT:
               reader.nextEvent();
               EndElement end = event.asEndElement();
               if (state.matchName(end.getName().getNamespaceURI(), end.getName().getLocalPart()))
               {
                  state.pop();
               }
               if (currentLevel > state.getLevel() + 1)
               {
                  while (reader.hasNext())
                  {
                     XMLEvent a = reader.peek();
                     if (a.getEventType() == XMLStreamConstants.START_ELEMENT)
                     {
                        state.push(reader);
                        if (state.matchName(namespaceURI, name))
                        {
                           reader.unmark();
                           return state.peekName();
                        }
                     }
                     else if (a.getEventType() == XMLStreamConstants.END_ELEMENT)
                     {
                        reader.nextEvent();
                        state.pop();
                     }
                     else
                     {
                        reader.nextEvent();
                     }
                  }
               }
               break;
            default:
               reader.nextEvent();
         }
      }
      reader.rollback();
      state = backup;
      return null;
   }

   public N getName()
   {
      return state.peekName();
   }

   public int getLevel()
   {
      return state.getLevel();
   }

   public String getContent()
   {
      return state.peekValue();
   }

   public String getTrimmedContent()
   {
      String content = getContent();
      return content != null ? content.trim() : null;
   }

   public <V> V parseContent(ValueType<V> valueType) throws NullPointerException, IllegalStateException, TypeConversionException
   {
      if (valueType == null)
      {
         throw new NullPointerException("No null value type accepted");
      }
      if (state == null || state.peekValue() == null)
      {
         throw new IllegalStateException();
      }
      return valueType.convert(state.peekValue());
   }

   /**
    * Perform check prior most of the operations
    */
   private void check()
   {
      if (reader == null)
      {
         throw new IllegalStateException("The navigator must be initialized");
      }
   }

   class State
   {

      final Deque<Pair> stack;
      final Map<String, String> currentAttributs;

      State()
      {
         this.stack = new ArrayDeque<Pair>();
         this.currentAttributs = new HashMap<String, String>();
      }

      State(State that)
      {
         this.stack = new ArrayDeque<Pair>(that.stack);
         this.currentAttributs = new HashMap<String, String>(that.currentAttributs);
      }

      private int getLevel()
      {
         return stack.size();
      }

      private boolean matchName(String namespaceURI, String localPart)
      {
         if (localPart == null)
         {
            return true;
         }
         else
         {
            Pair p = stack.peek();
            return (namespaceURI == null || namespaceURI.equals(getURI(p.name))) && localPart.equals(getLocalPart(p.name));
         }
      }

      private N peekName()
      {
         return stack.peek().name;
      }

      private String peekURI()
      {
         return getURI(stack.peek().name);
      }

      private String peekValue()
      {
         return stack.peek().value;
      }

      private Pair push(PushbackXMLEventReader reader) throws XMLStreamException
      {
         StartElement start = reader.nextEvent().asStartElement();
         currentAttributs.clear();
         for (Iterator i = start.getAttributes(); i.hasNext();)
         {
            Attribute attr = (Attribute)i.next();
            currentAttributs.put(attr.getName().getLocalPart(), attr.getValue());
         }

         //
         String prefix = start.getName().getPrefix();
         String uri = start.getName().getNamespaceURI();
         String localPart = start.getName().getLocalPart();

         StringBuilder content = new StringBuilder();
         try
         {
            while (reader.hasNext())
            {
               XMLEvent next = reader.peek();
               int eventType = next.getEventType();
               if (eventType == XMLStreamConstants.CHARACTERS)
               {
                  Characters chars = (Characters)next;
                  content.append(chars.getData());
               }
               else if (eventType == XMLStreamConstants.START_ELEMENT || eventType == XMLStreamConstants.END_ELEMENT)
               {
                  break;
               }
               reader.nextEvent();
            }
         }
         catch (XMLStreamException e)
         {
            e.printStackTrace();
         }

         N name = getName(uri, prefix, localPart);
         Pair p = new Pair(name, content.toString());
         stack.push(p);
         return p;
      }

      private void pop()
      {
         stack.pop();
      }
   }

   protected abstract String getLocalPart(N name);

   protected abstract String getURI(N name);

   protected abstract String getPrefix(N name);

   protected abstract N getName(String uri, String prefix, String localPart);

   class Pair
   {

      final N name;
      final String value;

      protected Pair(N name, String value)
      {
         this.name = name;
         this.value = value;
      }
   }
}