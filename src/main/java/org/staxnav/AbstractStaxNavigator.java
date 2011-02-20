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

import org.staxnav.xml.ElementVisitor;
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
class AbstractStaxNavigator<N> implements StaxNavigator<N>
{

   private final Naming<N> naming;
   private InputStream is;
   private XMLTokenizer tokenizer;
   private Stack state;

   AbstractStaxNavigator(Naming<N> naming, final InputStream is)
   {
      this.naming = naming;
      this.is = is;
      this.state = new Stack();
   }

   public N root() throws XMLStreamException
   {
      XMLInputFactory factory = XMLInputFactory.newInstance();
      this.tokenizer = new EventXMLTokenizer(factory.createXMLEventReader(is));
      tokenizer.skipTo(XMLTokenType.START_ELEMENT);
      return state.push(tokenizer).name;
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
      return name.equals(_next(naming.getURI(name), naming.getLocalPart(name)));
   }

   private N _next(final String namespaceURI, final String localPart) throws XMLStreamException
   {
      check();
      tokenizer.mark();
      Stack backup = new Stack(state);
      while (tokenizer.hasNext())
      {
         XMLTokenType type = tokenizer.peek();
         switch (type)
         {
            case START_ELEMENT:
               state.push(tokenizer);
               if (state.matchName(namespaceURI, localPart))
               {
                  tokenizer.unmark();
                  return state.peekName();
               }
               else
               {
                  tokenizer.rollback();
                  state = backup;
                  return null;
               }
            case END_ELEMENT:
               tokenizer.next();
               state.pop();
               break;
            default:
               tokenizer.next();
         }
      }
      tokenizer.rollback();
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
      return name.equals(_child(naming.getURI(name), naming.getLocalPart(name)));
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
      tokenizer.mark();
      Stack backup = new Stack(state);
      int currentLevel = state.getLevel();
      while (tokenizer.hasNext())
      {
         XMLTokenType type = tokenizer.peek();
         switch (type)
         {
            case START_ELEMENT:
               state.push(tokenizer);
               if (currentLevel + 1 == state.getLevel())
               {
                  if (state.matchName(namespaceURI, localPart))
                  {
                     tokenizer.unmark();
                     return state.peekName();
                  }
               }
               break;

            case END_ELEMENT:
               tokenizer.next();
               if (currentLevel == state.getLevel())
               {
                  tokenizer.rollback();
                  state = backup;
                  return null;
               }
               else
               {
                  state.pop();
               }
               break;
            default:
               tokenizer.next();
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
      return _descendant(naming.getURI(name), naming.getLocalPart(name));
   }

   public int _descendant(final String namespaceURI, final String localPart) throws NullPointerException, XMLStreamException
   {
      check();
      tokenizer.mark();
      Stack backup = new Stack(state);

      int currentLevel = state.getLevel();
      while (tokenizer.hasNext())
      {
         if (state.matchName(namespaceURI, localPart))
         {
            return state.getLevel() - currentLevel;
         }
         XMLTokenType type = tokenizer.peek();
         switch (type)
         {
            case START_ELEMENT:
               state.push(tokenizer);
               break;
            case END_ELEMENT:
               tokenizer.next();
               if (currentLevel == state.getLevel())
               {
                  tokenizer.rollback();
                  state = backup;
                  return -1;
               }
               else
               {
                  state.pop();
               }
               break;
            default:
               tokenizer.next();
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
      return name.equals(_sibling(naming.getURI(name), naming.getLocalPart(name)));
   }

   private N _sibling(final String namespaceURI, final String name) throws XMLStreamException
   {
      check();
      tokenizer.mark();
      Stack backup = new Stack(state);
      int currentLevel = state.getLevel();
      while (tokenizer.hasNext())
      {
         XMLTokenType type = tokenizer.peek();
         switch (type)
         {
            case START_ELEMENT:
               state.push(tokenizer);
               if (currentLevel == state.getLevel())
               {
                  if (state.matchName(namespaceURI, name))
                  {
                     tokenizer.unmark();
                     return state.peekName();
                  }
               }
               break;

            case END_ELEMENT:
               QName end = tokenizer.getElementName();
               tokenizer.next();
               if (state.matchName(end.getNamespaceURI(), end.getLocalPart()))
               {
                  state.pop();
               }
               if (currentLevel > state.getLevel() + 1)
               {
                  while (tokenizer.hasNext())
                  {
                     XMLTokenType a = tokenizer.peek();
                     if (a == XMLTokenType.START_ELEMENT)
                     {
                        state.push(tokenizer);
                        if (state.matchName(namespaceURI, name))
                        {
                           tokenizer.unmark();
                           return state.peekName();
                        }
                     }
                     else if (a == XMLTokenType.END_ELEMENT)
                     {
                        tokenizer.next();
                        state.pop();
                     }
                     else
                     {
                        tokenizer.next();
                     }
                  }
               }
               break;
            default:
               tokenizer.next();
         }
      }
      tokenizer.rollback();
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
      if (tokenizer == null)
      {
         throw new IllegalStateException("The navigator must be initialized");
      }
   }

   class Stack
   {

      Element current;

      Stack()
      {
         this.current = null;
      }

      Stack(Stack that)
      {
         this.current = that.current;
      }

      private int getLevel()
      {
         return current != null ? current.depth : 0;
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
            return (namespaceURI == null || namespaceURI.equals(naming.getURI(p.name))) && localPart.equals(naming.getLocalPart(p.name));
         }
      }

      private N peekName()
      {
         return peek().name;
      }

      private String peekURI()
      {
         return naming.getURI(peek().name);
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
         N name = naming.getName(uri, prefix, localPart);

         //
         Element element = new Element(current, name);

         //
         reader.visitElement(element);

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

         //
         return current;
      }

      private void pop()
      {
         current = current.parent;
      }
   }

   class Element implements ElementVisitor
   {

      /** The element parent. */
      final Element parent;

      /** The element name. */
      final N name;

      /** The current element depth. */
      final int depth;

      /** An approximation of the element content. */
      String value;

      /** The attribute map. */
      private Map<String, String> attributes;

      /** The namespace map. */
      private Map<String, String> namespaces;

      /** The next element if not null, this element was already computed. */
      private Element next;

      protected Element(N name)
      {
         this(null, name);
      }

      protected Element(Element parent, N name)
      {
         this.depth = parent != null ? (1 + parent.depth) : 1;
         this.parent = parent;
         this.name = name;
         this.value = null;
         this.attributes = Collections.emptyMap();
         this.namespaces = Collections.emptyMap();
      }

      public void startAttributes()
      {
         attributes = new HashMap<String, String>();
      }

      public void addAttribute(QName name, String value)
      {
         attributes.put(name.getLocalPart(), value);
      }

      public void endAttributes()
      {
         // Nothing to do
      }

      public void startNamespaces()
      {
         namespaces = new HashMap<String, String>();
      }

      public void addNamespace(String prefix, String namespaceURI)
      {
         namespaces.put(prefix,  namespaceURI);
      }

      public void endNamespaces()
      {
         // Nothing to do
      }
   }
}