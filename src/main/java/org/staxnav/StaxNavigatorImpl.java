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

package org.staxnav;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class StaxNavigatorImpl<N>
{

   /** . */
   private final Naming<N> naming;

   /** . */
   private XMLStreamReader stream;

   /** . */
   private Element current;

   public StaxNavigatorImpl(Naming<N> naming, XMLStreamReader stream) throws XMLStreamException
   {
      if (naming == null)
      {
         throw new NullPointerException();
      }
      if (stream == null)
      {
         throw new NullPointerException();
      }

      //
      this.naming = naming;
      this.stream = stream;

      // Find the tail
      Element tail = null;
      while (stream.hasNext())
      {
         int type = stream.getEventType();
         if (type == XMLStreamConstants.START_ELEMENT)
         {
            tail = new Element();
            break;
         }
         else
         {
            stream.next();
         }
      }

      //
      this.current = tail;
   }

   public N getName() throws XMLStreamException
   {
      return naming.getName(current.name);
   }

   public int getLevel() throws XMLStreamException
   {
      return current.depth;
   }

   public String getContent()
   {
      return current.content.toString();
   }

   public String getAttribute(String name)
   {
      return current.attributes.get(name);
   }

   public N next() throws XMLStreamException
   {
      return next(null, null);
   }

   public boolean next(N name) throws XMLStreamException
   {
      if (name == null)
      {
         throw new NullPointerException("No null name accepted");
      }
      return name.equals(next(naming.getURI(name), naming.getLocalPart(name)));
   }

   public N next(String namespaceURI, String localPart) throws XMLStreamException
   {
      Element next = current.next();
      if (next != null && next.hasName(namespaceURI, localPart))
      {
         current = next;
         return naming.getName(next.name);
      }
      else
      {
         return null;
      }
   }

   public N child() throws XMLStreamException
   {
      return child(null, null);
   }

   public boolean child(N name) throws NullPointerException, XMLStreamException
   {
      if (name == null)
      {
         throw new NullPointerException("No null name accepted");
      }
      return name.equals(child(naming.getURI(name), naming.getLocalPart(name)));
   }

   public N child(String namespaceURI, String localPart) throws XMLStreamException
   {
      Element element = current;
      while (true)
      {
         Element next = element.next();
         if (next != null && next.depth > current.depth)
         {
            if (next.depth == current.depth + 1 && next.hasName(namespaceURI, localPart))
            {
               current = next;
               return naming.getName(next.name);
            }
            else
            {
               element = next;
            }
         }
         else
         {
            break;
         }
      }
      return null;
   }

   public N sibling() throws XMLStreamException
   {
      return sibling(null, null);
   }

   public boolean sibling(N name) throws NullPointerException, XMLStreamException
   {
      return name.equals(sibling(naming.getURI(name), naming.getLocalPart(name)));
   }

   public N sibling(String namespaceURI, String localPart) throws XMLStreamException
   {
      Element element = current;
      while (true)
      {
         Element next = element.next();
         if (next != null && next.depth >= current.depth)
         {
            if (next.depth == current.depth && next.hasName(namespaceURI, localPart))
            {
               current = next;
               return naming.getName(next.name);
            }
            else
            {
               element = next;
            }
         }
         else
         {
            break;
         }
      }
      return null;
   }

   public int descendant(N name) throws NullPointerException, XMLStreamException
   {
      if (name == null)
      {
         throw new NullPointerException("No null name accepted");
      }
      return descendant(naming.getURI(name), naming.getLocalPart(name));
   }

   public int descendant(String namespaceURI, String localPart) throws XMLStreamException
   {
      Element element = current;
      while (true)
      {
         Element next = element.next();
         if (next != null && next.depth >= current.depth)
         {
            if (next.hasName(namespaceURI, localPart))
            {
               int diff = next.depth - current.depth;
               current = next;
               return diff;
            }
            else
            {
               element = next;
            }
         }
         else
         {
            break;
         }
      }
      return -1;
   }

   private class Element
   {

      /** . */
      private final Element parent;

      /** . */
      private final QName name;

      /** . */
      private final int depth;

      /** The content approxmimation. */
      private final StringBuilder content;

      /** . */
      private Element next;

      /** . */
      private Map<String, String> attributes;

      private Element() throws XMLStreamException
      {
         this(null);
      }

      private Element(Element parent) throws XMLStreamException
      {
         // We assume that the stream points to the start of the modelled element
         if (stream.getEventType() != XMLStreamConstants.START_ELEMENT)
         {
            throw new AssertionError();
         }

         //
         QName name = stream.getName();

         //
         Map<String, String> attributes;
         int attributeCount = stream.getAttributeCount();
         if (attributeCount > 0)
         {
            attributes = new HashMap<String, String>(attributeCount);
            for (int i = 0;i < attributeCount;i++)
            {
               String attributeName = stream.getAttributeLocalName(i);
               String attributeValue = stream.getAttributeValue(i);
               attributes.put(attributeName, attributeValue);
            }

         }
         else
         {
            attributes = Collections.emptyMap();
         }

         // When we leave we assume that we are positionned on the next element start or the document end
         StringBuilder sb = null;
         while (true)
         {
            stream.next();
            int type = stream.getEventType();
            if (type == XMLStreamConstants.START_ELEMENT || type == XMLStreamConstants.END_DOCUMENT || type == XMLStreamConstants.END_ELEMENT)
            {
               break;
            }
            else if (type == XMLStreamConstants.CHARACTERS)
            {
               if (sb == null)
               {
                  sb = new StringBuilder();
               }
               sb.append(stream.getText());
            }
         }

         //
         int depth = 1 + (parent != null ?  parent.depth : 0);

         //
         this.parent = parent;
         this.name = name;
         this.depth = depth;
         this.content = sb;
         this.attributes = attributes;
      }

      private boolean hasName(String namespaceURI, String localPart)
      {
         if (localPart == null)
         {
            return true;
         }
         else
         {
            return (namespaceURI == null || namespaceURI.equals(name.getNamespaceURI())) && localPart.equals(name.getLocalPart());
         }
      }

      protected Element next() throws XMLStreamException
      {
         if (next == null)
         {
            Element parent = this;
            while (true)
            {
               int type = stream.getEventType();
               if (type == XMLStreamConstants.START_ELEMENT)
               {
                  next = new Element(parent);
                  break;
               }
               else if (type == XMLStreamConstants.END_ELEMENT)
               {
                  parent = parent.parent;
                  stream.next();
               }
               else if (type == XMLStreamConstants.END_DOCUMENT)
               {
                  break;
               }
               else
               {
                  stream.next();
               }
            }
         }
         return next;
      }
   }
}
