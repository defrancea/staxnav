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

package org.staxnav.wrapper;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class PushbackXMLStreamReader implements XMLStreamReader
{
   private XMLStreamReader streamReader;
   private Queue<PushbackData> pushbackStream = new LinkedList<PushbackData>();
   private boolean marked;
   private boolean needMark;
   private boolean inPushback;
   private PushbackData currentData;

   public PushbackXMLStreamReader(final XMLStreamReader streamReader)
   {
      this.streamReader = streamReader;
   }

   public Object getProperty(final String name)
           throws IllegalArgumentException
   {
      return streamReader.getProperty(name);
   }

   public int next()
           throws XMLStreamException
   {
      if (inPushback && !pushbackStream.isEmpty())
      {
         currentData = pushbackStream.remove();
         if (pushbackStream.isEmpty())
         {
            inPushback = false;
            if (needMark)
            {
               mark();
            }
         }
         return currentData.getType();
      }
      else if (marked)
      {
         int type = streamReader.next();
         pushbackStream.offer(
                 new PushbackData(
                         streamReader.getEventType(),
                         (streamReader.hasName() ? streamReader.getLocalName() : null),
                         (streamReader.hasText() ? streamReader.getText() : null)
                 )
         );
         if (streamReader.isStartElement())
         {
            for (int i = 0; i < streamReader.getAttributeCount(); ++i)
            {
               pushbackStream.element().getAttributes().add(
                       new PushbackAttribute(streamReader.getAttributeLocalName(i), streamReader.getAttributeValue(i))
               );
            }
         }
         return type;
      }
      return streamReader.next();
   }

   public void require(final int type, final String namespaceURI, final String localName)
           throws XMLStreamException
   {
      streamReader.require(type, namespaceURI, localName);
   }

   public String getElementText()
           throws XMLStreamException
   {
      return streamReader.getElementText();
   }

   public int nextTag()
           throws XMLStreamException
   {
      if (inPushback && !pushbackStream.isEmpty())
      {
         currentData = pushbackStream.remove();
         if (pushbackStream.isEmpty())
         {
            inPushback = false;
            if (needMark)
            {
               mark();
            }
         }
         return currentData.getType();
      }
      else if (marked)
      {
         int type = streamReader.nextTag();
         pushbackStream.offer(
                 new PushbackData(
                         streamReader.getEventType(),
                         (streamReader.hasName() ? streamReader.getLocalName() : null),
                         (streamReader.hasText() ? streamReader.getText() : null)
                 )
         );
         if (streamReader.isStartElement())
         {
            for (int i = 0; i < streamReader.getAttributeCount(); ++i)
            {
               pushbackStream.element().getAttributes().add(
                       new PushbackAttribute(streamReader.getAttributeLocalName(i), streamReader.getAttributeValue(i))
               );
            }
         }
         return type;
      }
      return streamReader.nextTag();
   }

   public boolean hasNext()
           throws XMLStreamException
   {
      return (inPushback && !pushbackStream.isEmpty() ? true : streamReader.hasNext());
   }

   public void close()
           throws XMLStreamException
   {
      streamReader.close();
   }

   public String getNamespaceURI(final String prefix)
   {
      return streamReader.getNamespaceURI(prefix);
   }

   public boolean isStartElement()
   {
      return (inPushback && currentData != null ? currentData.getType() == XMLStreamReader.START_ELEMENT : streamReader.isStartElement());
   }

   public boolean isEndElement()
   {
      return (inPushback && currentData != null ? currentData.getType() == XMLStreamReader.END_ELEMENT : streamReader.isEndElement());
   }

   public boolean isCharacters()
   {
      return (inPushback && currentData != null ? currentData.getType() == XMLStreamReader.CHARACTERS : streamReader.isCharacters());
   }

   public boolean isWhiteSpace()
   {
      return (inPushback && currentData != null ? currentData.getType() == XMLStreamReader.SPACE : streamReader.isWhiteSpace());
   }

   public String getAttributeValue(final String namespaceURI, final String localName)
   {
      return streamReader.getAttributeValue(namespaceURI, localName);
   }

   public int getAttributeCount()
   {
      return (inPushback && currentData != null ? currentData.getAttributes().size() : streamReader.getAttributeCount());
   }

   public QName getAttributeName(final int index)
   {
      return streamReader.getAttributeName(index);
   }

   public String getAttributeNamespace(final int index)
   {
      return streamReader.getAttributeNamespace(index);
   }

   public String getAttributeLocalName(final int index)
   {
      return (inPushback && currentData != null ? currentData.getAttributes().get(index).getKey() : streamReader.getAttributeLocalName(index));
   }

   public String getAttributePrefix(final int index)
   {
      return streamReader.getAttributePrefix(index);
   }

   public String getAttributeType(final int index)
   {
      return streamReader.getAttributeType(index);
   }

   public String getAttributeValue(final int index)
   {
      return (inPushback && currentData != null ? currentData.getAttributes().get(index).getValue() : streamReader.getAttributeValue(index));
   }

   public boolean isAttributeSpecified(final int index)
   {
      return streamReader.isAttributeSpecified(index);
   }

   public int getNamespaceCount()
   {
      return streamReader.getNamespaceCount();
   }

   public String getNamespacePrefix(final int index)
   {
      return streamReader.getNamespacePrefix(index);
   }

   public String getNamespaceURI(final int index)
   {
      return streamReader.getNamespaceURI(index);
   }

   public NamespaceContext getNamespaceContext()
   {
      return streamReader.getNamespaceContext();
   }

   public int getEventType()
   {
      return (inPushback && currentData != null ? currentData.getType() : streamReader.getEventType());
   }

   public String getText()
   {
      return (inPushback && currentData != null ? currentData.getText() : streamReader.getText());
   }

   public char[] getTextCharacters()
   {
      return streamReader.getTextCharacters();
   }

   public int getTextCharacters(final int sourceStart, final char[] target, final int targetStart, final int length)
           throws XMLStreamException
   {
      return streamReader.getTextCharacters(sourceStart, target, targetStart, length);
   }

   public int getTextStart()
   {
      return streamReader.getTextStart();
   }

   public int getTextLength()
   {
      return streamReader.getTextLength();
   }

   public String getEncoding()
   {
      return streamReader.getEncoding();
   }

   public boolean hasText()
   {
      return streamReader.hasText();
   }

   public Location getLocation()
   {
      return streamReader.getLocation();
   }

   public QName getName()
   {
      return streamReader.getName();
   }

   public String getLocalName()
   {
      return (inPushback && currentData != null ? currentData.getName() : streamReader.getLocalName());
   }

   public boolean hasName()
   {
      return streamReader.hasName();
   }

   public String getNamespaceURI()
   {
      return streamReader.getNamespaceURI();
   }

   public String getPrefix()
   {
      return streamReader.getPrefix();
   }

   public String getVersion()
   {
      return streamReader.getVersion();
   }

   public boolean isStandalone()
   {
      return streamReader.isStandalone();
   }

   public boolean standaloneSet()
   {
      return streamReader.standaloneSet();
   }

   public String getCharacterEncodingScheme()
   {
      return streamReader.getCharacterEncodingScheme();
   }

   public String getPITarget()
   {
      return streamReader.getPITarget();
   }

   public String getPIData()
   {
      return streamReader.getPIData();
   }

   public void mark()
   {
      if (marked)
      {
         throw new IllegalStateException("The navigator is already marked. Call flushPushback to mark again.");
      }

      if (inPushback)
      {
         throw new IllegalStateException("The navigator cannot mark because pushback is used. Call flushPushback to mark again.");
      }
      flushPushback();
      marked = true;
   }

   public boolean isMarked()
   {
      return marked;
   }

   public void wantMark()
   {
      if (!marked && !inPushback)
      {
         mark();
      }
      else
      {
         needMark = true;
      }
   }

   public void flushPushback()
   {
      pushbackStream.clear();
      marked = false;
      needMark = false;
      inPushback = false;
   }

   public void rollbackToMark()
   {
      currentData = pushbackStream.remove();
      if (!pushbackStream.isEmpty())
      {
         marked = false;
         needMark = false;
         inPushback = true;
      }
      else
      {
         currentData = null;
      }
   }

   public boolean haveCurrent()
   {
      return currentData != null;
   }

}
