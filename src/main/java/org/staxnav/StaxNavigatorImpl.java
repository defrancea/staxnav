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
   private Deque<Pair> stack = new ArrayDeque<Pair>();
   private Map<String, String> currentAttributs = new HashMap<String, String>();

   public StaxNavigatorImpl(final InputStream is)
   {
      this.is = is;
   }

   public void init()
   {
      XMLInputFactory factory = XMLInputFactory.newInstance();
      try
      {
         this.reader = new PushbackXMLStreamReader(factory.createXMLStreamReader(is));
         reader.nextTag();
         readCurrentAttributs();
         stack.push(new Pair(reader.getLocalName(), readContent()));
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

      for (String key : currentAttributs.keySet())
      {
         if (name.equals(key))
         {
            return currentAttributs.get(key);
         }
      }

      return null;
   }

   private String _child(final String name)
   {
      checkinit();
      reader.wantMark();
      Deque<Pair> backupStack = new ArrayDeque<Pair>(stack);
      Map<String, String> backupAttributs = new HashMap<String, String>(currentAttributs);
      int currentLevel = stack.size();
      boolean first = true;
      try
      {
         while (reader.hasNext())
         {
            switch ((first ? reader.getEventType() : reader.next()))
            {
               case XMLStreamReader.START_ELEMENT:
                  readCurrentAttributs();
                  stack.push(new Pair(reader.getLocalName(), readContent()));
                  if (currentLevel + 1 == stack.size())
                  {
                     if (name == null || name.equals(stack.peek().name))
                     {
                        reader.flushPushback();
                        backupStack = null;
                        backupAttributs = null;
                        return stack.peek().name;
                     }
                  }
                  break;

               case XMLStreamReader.END_ELEMENT:
                  if (currentLevel == stack.size())
                  {
                     reader.rollbackToMark();
                     stack = backupStack;
                     currentAttributs = backupAttributs;
                     backupStack = null;
                     backupAttributs = null;
                     return null;
                  }
                  else
                  {
                     stack.pop();
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
      backupStack = null;
      backupAttributs = null;
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
      Deque<Pair> backupStack = new ArrayDeque<Pair>(stack);
      Map<String, String> backupAttributs = new HashMap<String, String>(currentAttributs);
      int currentLevel = stack.size();
      boolean first = true;
      try
      {
         while (reader.hasNext())
         {
            switch ((first ? reader.getEventType() : reader.next()))
            {
               case XMLStreamReader.START_ELEMENT:
                  readCurrentAttributs();
                  stack.push(new Pair(reader.getLocalName(), readContent()));
                  if (currentLevel == stack.size())
                  {
                     if (name == null || (name != null && name.equals(stack.peek().name)))
                     {
                        backupStack = null;
                        backupAttributs = null;
                        return stack.peek().name;
                     }
                  }
                  break;

               case XMLStreamReader.END_ELEMENT:
                  if (reader.getLocalName().equals(stack.peek().name)) stack.pop();
                  if (currentLevel > stack.size() + 1)
                  {
                     while (reader.hasNext())
                     {
                        reader.next();
                        if (reader.isStartElement())
                        {
                           readCurrentAttributs();
                           stack.push(new Pair(reader.getLocalName(), readContent()));
                           if (name == null || (name != null && name.equals(stack.peek().name)))
                           {
                              backupStack = null;
                              backupAttributs = null;
                              return stack.peek().name;
                           }
                        }
                        if (reader.isEndElement())
                        {
                           stack.pop();
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
      stack = backupStack;
      currentAttributs = backupAttributs;
      backupStack = null;
      backupAttributs = null;
      return null;
   }

   public String getName()
   {
      return stack.peek().name;
   }

   public int getLevel()
   {
      return stack.size();
   }

   public String getText()
   {
      return stack.peek().value;
   }

   private void checkinit()
   {
      if (reader == null)
      {
         throw new IllegalStateException("The navigator must be initialized");
      }
   }

   private String readContent()
   {
      try
      {
         while (reader.hasNext())
         {
            reader.next();
            if (reader.isCharacters())
            {
               return reader.getText();
            }
         }
      }
      catch (XMLStreamException e)
      {
         e.printStackTrace();
      }
      return null;
   }

   private void readCurrentAttributs()
   {
      currentAttributs.clear();
      for (int i = 0; i < reader.getAttributeCount(); ++i)
      {
         currentAttributs.put(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
      }
   }

   class Pair
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