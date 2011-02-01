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
public class StaxNavigatorImpl implements StaxNavigator
{
   private InputStream is;
   private PushbackXMLStreamReader reader;
   private State state;

   public StaxNavigatorImpl(final InputStream is)
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
         state.read(reader);
      }
      catch (XMLStreamException e)
      {
         e.printStackTrace();
      }
   }

   public String child()
   {
      checkinit();
      return _child(null);
   }

   public boolean child(final String name) throws NullPointerException
   {
      if (name == null)
      {
         throw new NullPointerException("No null name accepted");
      }
      return name.equals(_child(name));
   }

   public String getAttribute(String name) throws NullPointerException, IllegalStateException
   {
      if (name == null)
      {
         throw new NullPointerException("No null name accepted");
      }

      for (String key : state.currentAttributs.keySet())
      {
         if (name.equals(key))
         {
            return state.currentAttributs.get(key);
         }
      }

      return null;
   }

   private String _child(final String name)
   {
      checkinit();
      reader.wantMark();

      State backup = new State(state);

      int currentLevel = state.stack.size();
      boolean first = true;
      try
      {
         while (reader.hasNext())
         {
            switch ((first ? reader.getEventType() : reader.next()))
            {
               case XMLStreamReader.START_ELEMENT:
                  state.read(reader);
                  if (currentLevel + 1 == state.stack.size())
                  {
                     if (name == null || name.equals(state.stack.peek().name))
                     {
                        reader.flushPushback();
                        return state.stack.peek().name;
                     }
                  }
                  break;

               case XMLStreamReader.END_ELEMENT:
                  if (currentLevel == state.stack.size())
                  {
                     reader.rollbackToMark();
                     state = backup;
                     return null;
                  }
                  else
                  {
                     state.stack.pop();
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

   public String sibling()
   {
      checkinit();
      return _sibling(null);
   }

   public boolean sibling(final String name) throws NullPointerException
   {
      return name.equals(_sibling(name));
   }

   private String _sibling(final String name)
   {
      checkinit();
      reader.wantMark();
      State backup = new State(state);
      int currentLevel = state.stack.size();
      boolean first = true;
      try
      {
         while (reader.hasNext())
         {
            switch ((first ? reader.getEventType() : reader.next()))
            {
               case XMLStreamReader.START_ELEMENT:
                  state.read(reader);
                  if (currentLevel == state.stack.size())
                  {
                     if (name == null || name.equals(state.stack.peek().name))
                     {
                        return state.stack.peek().name;
                     }
                  }
                  break;

               case XMLStreamReader.END_ELEMENT:
                  if (reader.getLocalName().equals(state.stack.peek().name))
                  {
                     state.stack.pop();
                  }
                  if (currentLevel > state.stack.size() + 1)
                  {
                     while (reader.hasNext())
                     {
                        reader.next();
                        if (reader.isStartElement())
                        {
                           state.read(reader);
                           if (name == null || name.equals(state.stack.peek().name))
                           {
                              return state.stack.peek().name;
                           }
                        }
                        if (reader.isEndElement())
                        {
                           state.stack.pop();
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

   public String getName()
   {
      return state.stack.peek().name;
   }

   public int getLevel()
   {
      return state.stack.size();
   }

   public String getText()
   {
      return state.stack.peek().value;
   }

   private void checkinit()
   {
      if (reader == null)
      {
         throw new IllegalStateException("The navigator must be initialized");
      }
   }

   static class State
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

      private void read(PushbackXMLStreamReader reader)
      {
         currentAttributs.clear();
         for (int i = 0; i < reader.getAttributeCount(); ++i)
         {
            currentAttributs.put(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
         }

         //
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

         stack.push(new Pair(localName, content));
      }

   }

   static class Pair
   {
      private String name;
      private String value;

      public Pair(final String name, final String value)
      {
         this.name = name;
         this.value = value;
      }
   }
}