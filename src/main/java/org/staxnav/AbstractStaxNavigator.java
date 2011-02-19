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

import org.staxnav.xml.AttributeContainer;
import org.staxnav.xml.XMLTokenType;
import org.staxnav.xml.XMLTokenizer;
import org.staxnav.xml.event.EventXMLTokenizer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public abstract class AbstractStaxNavigator<N> implements StaxNavigator<N>
{
   private InputStream is;
   private XMLTokenizer reader;
   private Stack state;

   public AbstractStaxNavigator(final InputStream is)
   {
      this.is = is;
      this.state = new Stack();
   }

   public N root() throws XMLStreamException
   {
      XMLInputFactory factory = XMLInputFactory.newInstance();
      this.reader = new EventXMLTokenizer(factory.createXMLEventReader(is));
      reader.skipTo(XMLTokenType.START_ELEMENT);
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
      Stack backup = new Stack(state);
      while (reader.hasNext())
      {
         XMLTokenType event = reader.peek();
         switch (event)
         {
            case START_ELEMENT:
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
            case END_ELEMENT:
               reader.next();
               state.pop();
               break;
            default:
               reader.next();
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
      return state.peek().attributes.get(name);
   }

   private N _child(final String namespaceURI, final String localPart) throws XMLStreamException
   {
      check();
      reader.mark();
      Stack backup = new Stack(state);
      int currentLevel = state.getLevel();
      while (reader.hasNext())
      {
         XMLTokenType event = reader.peek();
         switch (event)
         {
            case START_ELEMENT:
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

            case END_ELEMENT:
               reader.next();
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
               reader.next();
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
      Stack backup = new Stack(state);

      int currentLevel = state.getLevel();
      while (reader.hasNext())
      {
         if (state.matchName(namespaceURI, localPart))
         {
            return state.getLevel() - currentLevel;
         }
         XMLTokenType event = reader.peek();
         switch (event)
         {
            case START_ELEMENT:
               state.push(reader);
               break;
            case END_ELEMENT:
               reader.next();
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
               reader.next();
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
      Stack backup = new Stack(state);
      int currentLevel = state.getLevel();
      while (reader.hasNext())
      {
         XMLTokenType event = reader.peek();
         switch (event)
         {
            case START_ELEMENT:
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

            case END_ELEMENT:
               QName end = reader.getElementName();
               reader.next();
               if (state.matchName(end.getNamespaceURI(), end.getLocalPart()))
               {
                  state.pop();
               }
               if (currentLevel > state.getLevel() + 1)
               {
                  while (reader.hasNext())
                  {
                     XMLTokenType a = reader.peek();
                     if (a == XMLTokenType.START_ELEMENT)
                     {
                        state.push(reader);
                        if (state.matchName(namespaceURI, name))
                        {
                           reader.unmark();
                           return state.peekName();
                        }
                     }
                     else if (a == XMLTokenType.END_ELEMENT)
                     {
                        reader.next();
                        state.pop();
                     }
                     else
                     {
                        reader.next();
                     }
                  }
               }
               break;
            default:
               reader.next();
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

   class Stack
   {

      Element current;
      int depth;

      Stack()
      {
         this.current = null;
         this.depth = 0;
      }

      Stack(Stack that)
      {
         this.current = that.current;
         this.depth = that.depth;
      }

      private int getLevel()
      {
         return depth;
      }

      private boolean matchName(String namespaceURI, String localPart)
      {
         if (localPart == null)
         {
            return true;
         }
         else
         {
            Element p = peek();
            return (namespaceURI == null || namespaceURI.equals(getURI(p.name))) && localPart.equals(getLocalPart(p.name));
         }
      }

      private N peekName()
      {
         return peek().name;
      }

      private String peekURI()
      {
         return getURI(peek().name);
      }

      private String peekValue()
      {
         return peek().value;
      }

      private Element peek()
      {
         return current;
      }

      private Element push(XMLTokenizer reader) throws XMLStreamException
      {
         QName start = reader.getElementName();

         //
         String prefix = start.getPrefix();
         String uri = start.getNamespaceURI();
         String localPart = start.getLocalPart();
         N name = getName(uri, prefix, localPart);

         //
         Element element = new Element(current, name);

         //
         reader.fillAttributes(element);

         //
         reader.next();

         //
         StringBuilder content = new StringBuilder();
         try
         {
            while (reader.hasNext())
            {
               XMLTokenType next = reader.peek();
               if (next == XMLTokenType.CHARACTERS)
               {
                  String chars = reader.getCharacters();
                  content.append(chars);
               }
               else if (next == XMLTokenType.START_ELEMENT || next == XMLTokenType.END_ELEMENT)
               {
                  break;
               }
               reader.next();
            }
         }
         catch (XMLStreamException e)
         {
            e.printStackTrace();
         }

         //
         element.value = content.toString();

         //
         current = element;
         depth++;

         //
         return current;
      }

      private void pop()
      {
         depth--;
         current = current.parent;
      }
   }

   protected abstract String getLocalPart(N name);

   protected abstract String getURI(N name);

   protected abstract String getPrefix(N name);

   protected abstract N getName(String uri, String prefix, String localPart);

   class Element implements AttributeContainer
   {

      final Element parent;
      final N name;
      String value;
      private Map<String, String> attributes;

      protected Element(N name)
      {
         this(null, name);
      }

      protected Element(Element parent, N name)
      {
         this.parent = parent;
         this.name = name;
         this.value = null;
         this.attributes = Collections.emptyMap();
      }

      public void start()
      {
         attributes = new HashMap<String, String>();
      }

      public void add(QName name, String value)
      {
         attributes.put(name.getLocalPart(), value);
      }

      public void end()
      {
         // Nothing to do
      }
   }
}