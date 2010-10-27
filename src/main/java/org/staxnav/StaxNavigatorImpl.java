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
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.Stack;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class StaxNavigatorImpl implements StaxNavigator
{
   private InputStream is;
   private XMLStreamReader reader;

   private Stack<Pair> stack = new Stack<Pair>();
   //private int rewindtype = 0;

   public StaxNavigatorImpl(final InputStream is)
   {
      this.is = is;
   }

   public void init()
   {
      XMLInputFactory factory = XMLInputFactory.newInstance();
      try
      {
         this.reader = factory.createXMLStreamReader(is);
      }
      catch (XMLStreamException e)
      {
         e.printStackTrace();
      }

      try
      {
         while (reader.hasNext())
         {
            int type = reader.next();
            if (type == XMLStreamReader.START_ELEMENT)
            {
               stack.push(new Pair(reader.getLocalName(), null));
               return;
            }
         }
      }
      catch (XMLStreamException e)
      {
         e.printStackTrace();
      }
   }

   public String child()
   {
      checkinit();
      return child(null);
   }

   public String child(final String name)
   {
      checkinit();
      int currentLevel = stack.size();
      try
      {
         while (reader.hasNext())
         {
            int type = reader.next();
            switch (type)
            {  
               case XMLStreamReader.START_ELEMENT:

                  stack.push(new Pair(reader.getLocalName(), null));
                  if (currentLevel + 1 == stack.size())
                  {
                     if (name == null || (name != null && name.equals(reader.getLocalName())))
                     {
                        return reader.getLocalName();
                     }
                  }
                  break;
                  
               case XMLStreamReader.END_ELEMENT:
                  stack.pop();
                  break;
            }
         }
      }
      catch (XMLStreamException e)
      {
         e.printStackTrace();
      }
      return null;      
   }

   public String sibbling()
   {
      checkinit();
      return sibbling(null);
   }

   public String sibbling(final String name)
   {
      checkinit();
      int currentLevel = stack.size();
      try
      {
         while (reader.hasNext())
         {
            int type = reader.next();
            switch (type)
            {
               case XMLStreamReader.START_ELEMENT:
                  stack.push(new Pair(reader.getLocalName(), null));
                  if (currentLevel == stack.size())
                  {
                     if (name == null || (name != null && name.equals(reader.getLocalName())))
                     {
                        return reader.getLocalName();
                     }
                  }
                  break;

               case XMLStreamReader.END_ELEMENT:
                  stack.pop();
                  break;
            }
         }
      }
      catch (XMLStreamException e)
      {
         e.printStackTrace();
      }
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

   /*private int next() throws XMLStreamException
   {
      return (rewindtype > 0 ? rewindtype : reader.next());
   }*/

   /*private void cancelRead(int type)
   {
      rewindtype = type;
   }

   private boolean isInRewind()
   {
      return rewindtype > 0;
   }*/

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
