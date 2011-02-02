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

   public void init()
   {
      XMLInputFactory factory = XMLInputFactory.newInstance();
      try
      {
         this.reader = new PushbackXMLStreamReader(factory.createXMLStreamReader(is));
         reader.nextTag();
         state.push(reader);
      }
      catch (XMLStreamException e)
      {
         e.printStackTrace();
      }
   }

   public N child()
   {
      checkinit();
      return _child(null, null);
   }

   public boolean child(final String name) throws NullPointerException
   {
      return child(null, name);
   }

   public boolean child(String namespaceURI, String name) throws NullPointerException
   {
      if (name == null)
      {
         throw new NullPointerException("No null name accepted");
      }
      return name.equals(_child(namespaceURI, name));
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

   private N _child(final String namespaceURI, final String localName)
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
                     if (state.matchName(namespaceURI, localName))
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
      return _sibling(null);
   }

   public boolean sibling(final String name) throws NullPointerException
   {
      return name.equals(_sibling(name));
   }

   private N _sibling(final String name)
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
                     if (name == null || name.equals(state.peekName()))
                     {
                        return state.peekName();
                     }
                  }
                  break;

               case XMLStreamReader.END_ELEMENT:
                  if (reader.getLocalName().equals(state.peekName()))
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
                           if (name == null || name.equals(state.peekName()))
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

      private boolean matchName(String namespaceURI, String localName)
      {
         if (localName == null)
         {
            return true;
         }
         else
         {
            Pair p = stack.peek();
            return (namespaceURI == null || namespaceURI.equals(p.getURI())) && localName.equals(p.getName());
         }
      }

      private N peekName()
      {
         return stack.peek().getName();
      }

      private String peekURI()
      {
         return stack.peek().getURI();
      }

      private String peekValue()
      {
         return stack.peek().getValue();
      }

      private void push(PushbackXMLStreamReader reader)
      {
         currentAttributs.clear();
         for (int i = 0; i < reader.getAttributeCount(); ++i)
         {
            currentAttributs.put(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
         }

         //
         String prefix = reader.getPrefix();
         String uri = reader.getNamespaceURI();
         String localName = reader.getLocalName();

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

         stack.push(createPair(uri, prefix, localName, content));
      }

      private void pop()
      {
         stack.pop();
      }
   }

   protected abstract Pair createPair(String uri, String prefix, String localPart, String content);

   abstract class Pair
   {
      protected abstract String getURI();
      protected abstract N getName();
      protected abstract String getPrefix();
      protected abstract String getLocalPart();
      protected abstract String getValue();
   }
}