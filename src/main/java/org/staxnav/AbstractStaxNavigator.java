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

import org.staxnav.wrapper.PushbackXMLStreamReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public abstract class AbstractStaxNavigator<N> implements StaxNavigator<N>
{
   private InputStream is;
   private PushbackXMLStreamReader reader;
   private State state;

   public AbstractStaxNavigator(final InputStream is)
   {
      this.is = is;
      this.state = new State();
   }

   public N init()
   {
      XMLInputFactory factory = XMLInputFactory.newInstance();
      try
      {
         this.reader = new PushbackXMLStreamReader(factory.createXMLStreamReader(is));
         reader.nextTag();
         return state.push(reader).name;
      }
      catch (XMLStreamException e)
      {
         e.printStackTrace();
         return null;
      }
   }

   public N child()
   {
      checkinit();
      return _child(null, null);
   }

   public boolean child(N name) throws NullPointerException
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

   private N _child(final String namespaceURI, final String localPart)
   {
      checkinit();
      reader.wantMark();

      State backup = new State(state);

      int currentLevel = state.getLevel();
      boolean first = true;
      try
      {
         while (reader.hasNext())
         {
            switch ((first ? reader.getEventType() : reader.next()))
            {
               case XMLStreamReader.START_ELEMENT:
                  state.push(reader);
                  if (currentLevel + 1 == state.getLevel())
                  {
                     if (state.matchName(namespaceURI, localPart))
                     {
                        reader.flushPushback();
                        return state.peekName();
                     }
                  }
                  break;

               case XMLStreamReader.END_ELEMENT:
                  if (currentLevel == state.getLevel())
                  {
                     reader.rollbackToMark();
                     state = backup;
                     return null;
                  }
                  else
                  {
                     state.pop();
                  }
                  break;
            }
            first = false;
         }
      }
      catch (XMLStreamException e)
      {
         e.printStackTrace();
      }
      return null;
   }

   public N sibling()
   {
      checkinit();
      return _sibling(null, null);
   }

   public boolean sibling(N name) throws NullPointerException
   {
      return name.equals(_sibling(getURI(name), getLocalPart(name)));
   }

   private N _sibling(final String namespaceURI, final String name)
   {
      checkinit();
      reader.wantMark();
      State backup = new State(state);
      int currentLevel = state.getLevel();
      boolean first = true;
      try
      {
         while (reader.hasNext())
         {
            switch ((first ? reader.getEventType() : reader.next()))
            {
               case XMLStreamReader.START_ELEMENT:
                  state.push(reader);
                  if (currentLevel == state.getLevel())
                  {
                     if (state.matchName(namespaceURI, name))
                     {
                        return state.peekName();
                     }
                  }
                  break;

               case XMLStreamReader.END_ELEMENT:
                  if (state.matchName(reader.getNamespaceURI(), reader.getLocalName()))
                  {
                     state.pop();
                  }
                  if (currentLevel > state.getLevel() + 1)
                  {
                     while (reader.hasNext())
                     {
                        reader.next();
                        if (reader.isStartElement())
                        {
                           state.push(reader);
                           if (state.matchName(namespaceURI, name))
                           {
                              return state.peekName();
                           }
                        }
                        if (reader.isEndElement())
                        {
                           state.pop();
                        }
                     }
                  }
                  break;
            }
            first = false;
         }
      }
      catch (XMLStreamException e)
      {
         e.printStackTrace();
      }
      reader.rollbackToMark();
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

   public String getText()
   {
      return state.peekValue();
   }

   private void checkinit()
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

      private Pair push(PushbackXMLStreamReader reader)
      {
         currentAttributs.clear();
         for (int i = 0; i < reader.getAttributeCount(); ++i)
         {
            currentAttributs.put(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
         }

         //
         String prefix = reader.getPrefix();
         String uri = reader.getNamespaceURI();
         String localPart = reader.getLocalName();

         String content = null;
         try
         {
            while (reader.hasNext())
            {
               reader.next();
               if (reader.isCharacters())
               {
                  content = reader.getText();
                  break;
               }
            }
         }
         catch (XMLStreamException e)
         {
            e.printStackTrace();
         }

         N name = getName(uri, prefix, localPart);
         Pair p = new Pair(name, content);
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