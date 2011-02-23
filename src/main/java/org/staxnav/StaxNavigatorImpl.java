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

import javax.xml.XMLConstants;
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
public class StaxNavigatorImpl<N> implements StaxNavigator<N>
{

   /** . */
   private final Naming<N> naming;

   /** . */
   private Element current;

   /** . */
   private final int depth;

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
      this.current = new Element(stream);
      this.depth = 0;
   }

   public StaxNavigatorImpl(Naming<N> naming, Element current)
   {
      this.naming = naming;
      this.current = current;
      this.depth = current.depth;
   }

   private Element getCurrent() throws StaxNavException
   {
      return current;
   }

   private void setCurrent(Element current)
   {
      this.current = current;
   }

   public N getName() throws StaxNavException
   {
      return naming.getName(getCurrent().name);
   }

   public int getDepth() throws StaxNavException
   {
      return getCurrent().depth;
   }

   public String getContent() throws StaxNavException
   {
      return getCurrent().content.toString();
   }

   public <V> V parseContent(ValueType<V> valueType) throws NullPointerException, StaxNavException
   {
      if (valueType == null)
      {
         throw new NullPointerException();
      }
      String content = getContent().trim();
      return valueType.convert(content);
   }

   public String getAttribute(String name) throws NullPointerException, IllegalStateException, StaxNavException
   {
      Map<String, String> attributes = getCurrent().attributes;
      if (attributes.isEmpty())
      {
         return null;
      }
      else
      {
         return attributes.get(name);
      }
   }

   public StaxNavigator<N> create() throws StaxNavException
   {
      return new StaxNavigatorImpl<N>(naming, current);
   }

   public String getAttribute(QName name) throws NullPointerException, IllegalStateException, StaxNavException
   {
      if (name == null)
      {
         throw new NullPointerException("No null attribute name expected");
      }
      if (XMLConstants.NULL_NS_URI.equals(name.getNamespaceURI()))
      {
         return getAttribute(name.getLocalPart());
      }
      else
      {
         Map<QName, String> qualifiedAttributes = getCurrent().qualifiedAttributes;
         if (qualifiedAttributes.isEmpty())
         {
            return null;
         }
         else
         {
            return qualifiedAttributes.get(name);
         }
      }
   }

   public String getNamespaceByPrefix(String prefix) throws NullPointerException, StaxNavException
   {
      if (prefix == null)
      {
         throw new NullPointerException();
      }
      return getCurrent().getNamespaceByPrefix(prefix);
   }

   public N next() throws StaxNavException
   {
      return next(null, null);
   }

   public boolean next(N name) throws StaxNavException
   {
      if (name == null)
      {
         throw new NullPointerException("No null name accepted");
      }
      return name.equals(next(naming.getURI(name), naming.getLocalPart(name)));
   }

   public N next(String namespaceURI, String localPart) throws StaxNavException
   {
      Element next = getCurrent().next();
      if (next != null && next.depth > depth && next.hasName(namespaceURI, localPart))
      {
         setCurrent(next);
         return naming.getName(next.name);
      }
      else
      {
         return null;
      }
   }

   public boolean find(N name) throws StaxNavException
   {
      if (name == null)
      {
         throw new NullPointerException("No null name accepted");
      }
      return name.equals(find(naming.getURI(name), naming.getLocalPart(name)));
   }

   public N find(String namespaceURI, String localPart) throws StaxNavException
   {
      Element element = getCurrent();
      while (true)
      {
         Element next = element.next();
         if (next != null)
         {
            if (next.hasName(namespaceURI, localPart))
            {
               setCurrent(next);
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

   public N child() throws StaxNavException
   {
      return child(null, null);
   }

   public boolean child(N name) throws NullPointerException, StaxNavException
   {
      if (name == null)
      {
         throw new NullPointerException("No null name accepted");
      }
      return name.equals(child(naming.getURI(name), naming.getLocalPart(name)));
   }

   public N child(String namespaceURI, String localPart) throws StaxNavException
   {
      Element element = getCurrent();
      while (true)
      {
         Element next = element.next();
         if (next != null && next.depth > getCurrent().depth)
         {
            if (next.depth == getCurrent().depth + 1 && next.hasName(namespaceURI, localPart))
            {
               setCurrent(next);
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

   public N sibling() throws StaxNavException
   {
      return sibling(null, null);
   }

   public boolean sibling(N name) throws NullPointerException, StaxNavException
   {
      return name.equals(sibling(naming.getURI(name), naming.getLocalPart(name)));
   }

   public N sibling(String namespaceURI, String localPart) throws StaxNavException
   {
      Element element = getCurrent();
      while (true)
      {
         Element next = element.next();
         if (next != null && next.depth >= getCurrent().depth)
         {
            if (next.depth == getCurrent().depth && next.hasName(namespaceURI, localPart))
            {
               setCurrent(next);
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

   public int descendant(N name) throws NullPointerException, StaxNavException
   {
      if (name == null)
      {
         throw new NullPointerException("No null name accepted");
      }
      return descendant(naming.getURI(name), naming.getLocalPart(name));
   }

   public int descendant(String namespaceURI, String localPart) throws StaxNavException
   {
      Element element = getCurrent();
      while (true)
      {
         Element next = element.next();
         if (next != null && next.depth >= getCurrent().depth)
         {
            if (next.hasName(namespaceURI, localPart))
            {
               int diff = next.depth - getCurrent().depth;
               setCurrent(next);
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

   private static class Element
   {

      /** . */
      private final XMLStreamReader stream;

      /** . */
      private final Element parent;

      /** . */
      private final QName name;

      /** . */
      private final int depth;

      /** The content approxmimation. */
      private final StringBuilder content;

      /** . */
      private final Map<String, String> attributes;

      /** . */
      private final Map<QName, String> qualifiedAttributes;

      /** . */
      private final Map<String, String> namespaces;

      /** . */
      private Element next;

      private static XMLStreamReader seekFirstElement(XMLStreamReader stream) throws XMLStreamException
      {
         while (stream.hasNext())
         {
            int type = stream.getEventType();
            if (type == XMLStreamConstants.START_ELEMENT)
            {
               return stream;
            }
            else
            {
               stream.next();
            }
         }
         throw new StaxNavException("No head!!!!");
      }

      private Element(XMLStreamReader stream) throws XMLStreamException
      {
         this(seekFirstElement(stream), null);
      }

      private Element(XMLStreamReader stream, Element parent) throws XMLStreamException
      {
         // We assume that the stream points to the start of the modelled element
         if (stream.getEventType() != XMLStreamConstants.START_ELEMENT)
         {
            throw new AssertionError();
         }

         //
         QName name = stream.getName();

         //
         Map<String, String> attributes = Collections.emptyMap();
         Map<QName, String> qualifiedAttributes = Collections.emptyMap();
         int attributeCount = stream.getAttributeCount();
         for (int i = 0;i < attributeCount;i++)
         {
            String attributeValue = stream.getAttributeValue(i);
            QName attributeName = stream.getAttributeName(i);
            if (XMLConstants.NULL_NS_URI.equals(attributeName.getNamespaceURI()))
            {
               if (attributes.isEmpty())
               {
                  attributes = new HashMap<String, String>();
               }
               attributes.put(attributeName.getLocalPart(), attributeValue);
            }
            else
            {
               if (qualifiedAttributes.isEmpty())
               {
                  qualifiedAttributes = new HashMap<QName, String>();
               }
               qualifiedAttributes.put(attributeName, attributeValue);
            }
         }

         //
         Map<String, String> namespaces;
         int namespaceCount = stream.getNamespaceCount();
         if (namespaceCount > 0)
         {
            namespaces = new HashMap<String, String>();
            for (int i = 0;i < namespaceCount;i++)
            {
               String namespacePrefix = stream.getNamespacePrefix(i);
               if (namespacePrefix == null)
               {
                  namespacePrefix = "";
               }
               String namespaceURI = stream.getNamespaceURI(i);
               namespaces.put(namespacePrefix, namespaceURI);
            }
         }
         else
         {
            namespaces = Collections.emptyMap();
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
         this.qualifiedAttributes = qualifiedAttributes;
         this.namespaces = namespaces;
         this.stream = stream;
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

      private String getNamespaceByPrefix(String namespacePrefix)
      {
         for (Element current = this;current != null;current = current.parent)
         {
            String namespaceURI = current.namespaces.get(namespacePrefix);
            if (namespaceURI != null)
            {
               return namespaceURI;
            }
         }
         return null;
      }

      private Element next() throws StaxNavException
      {
         try
         {
            if (next == null)
            {
               Element parent = this;
               while (true)
               {
                  int type = stream.getEventType();
                  if (type == XMLStreamConstants.START_ELEMENT)
                  {
                     next = new Element(stream, parent);
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
         catch (XMLStreamException e)
         {
            throw new StaxNavException(e);
         }
      }
   }
}
