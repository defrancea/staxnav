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
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

   /** . */
   private boolean trimContent;

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
      this.current = new HeadElement(stream);
      this.depth = 0;
      this.trimContent = false;
   }

   private StaxNavigatorImpl(Naming<N> naming, Element current, boolean trimContent)
   {
      this.naming = naming;
      this.current = current;
      this.depth = current.getDepth();
      this.trimContent = trimContent;
   }

   public N getName() throws StaxNavException
   {
      return current.getName(naming);
   }

   public Location getLocation() throws StaxNavException
   {
      return current.getLocation();
   }

   public int getDepth() throws StaxNavException
   {
      return current.getDepth();
   }

   public void setTrimContent(boolean trimContent)
   {
      this.trimContent = trimContent;
   }

   public boolean getTrimContent()
   {
      return trimContent;
   }

   public String getContent() throws StaxNavException
   {
      return current.getContent(trimContent);
   }

   public <V> V parseContent(ValueType<V> valueType) throws IllegalStateException, NullPointerException, StaxNavException
   {
      if (valueType == null)
      {
         throw new NullPointerException();
      }
      Element element = current;
      String content = element.getContent(true);
      if (content == null)
      {
         throw new IllegalStateException("No content available for parsing");
      }
      try
      {
         return valueType.parse(content);
      }
      catch (Exception e)
      {
         if (e instanceof TypeConversionException)
         {
            throw (TypeConversionException)e;
         }
         else
         {
            throw new TypeConversionException(element.getLocation(), e, "Could not parse string value " + content);
         }
      }
   }

   public String getAttribute(String name) throws NullPointerException, IllegalStateException, StaxNavException
   {
      Map<String, String> attributes = current.getAttributes();
      if (attributes.isEmpty())
      {
         return null;
      }
      else
      {
         return attributes.get(name);
      }
   }

   public StaxNavigator<N> fork() throws StaxNavException
   {
      StaxNavigatorImpl<N> fork = new StaxNavigatorImpl<N>(naming, current, trimContent);
      sibling();
      return fork;
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
         Map<QName, String> qualifiedAttributes = current.getQualifiedAttributes();
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
      return current.getNamespaceByPrefix(prefix);
   }

   public N next() throws StaxNavException
   {
      Element next = current.next(depth);
      if (next != null)
      {
         current = next;
         return naming.getName(next.getName());
      }
      else
      {
         return null;
      }
   }

   public boolean next(N name) throws StaxNavException
   {
      if (name == null)
      {
         throw new NullPointerException("No null name accepted");
      }
      Element next = current.next(depth);
      if (next != null && next.hasName(naming.getURI(name), naming.getLocalPart(name)))
      {
         current = next;
         return true;
      }
      else
      {
         return false;
      }
   }

   public N next(Set<N> names) throws StaxNavException
   {
      if (names == null)
      {
         throw new NullPointerException();
      }
      Element next = current.next(depth);
      if (next != null)
      {
         N name = naming.getName(next.getName());
         if (names.contains(name))
         {
            current = next;
            return name;
         }
         else
         {
            throw new StaxNavException(next.getLocation(), "Was not expecting an element among " + names + " instead of " + name);
         }
      }
      else
      {
         throw new StaxNavException(current.getLocation());
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
      Element element = current;
      while (element != null)
      {
         if (element.hasName(namespaceURI, localPart))
         {
            current = element;
            return naming.getName(element.getName());
         }
         else
         {
            element = element.next();
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
      Element element = current;
      while (true)
      {
         Element next = element.next();
         if (next != null && next.getDepth() > current.getDepth())
         {
            if (next.getDepth() == current.getDepth() + 1 && next.hasName(namespaceURI, localPart))
            {
               current = next;
               return naming.getName(next.getName());
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
      Element element = current;
      while (true)
      {
         Element next = element.next();
         if (next != null && next.getDepth() >= current.getDepth())
         {
            if (next.getDepth() == current.getDepth() && next.hasName(namespaceURI, localPart))
            {
               current = next;
               return naming.getName(next.getName());
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
      Element element = current;
      while (true)
      {
         Element next = element.next();
         if (next != null && next.getDepth() >= current.getDepth())
         {
            if (next.hasName(namespaceURI, localPart))
            {
               int diff = next.getDepth() - current.getDepth();
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

   private static abstract class Element
   {

      protected abstract Element getParent();

      protected abstract <N> N getName(Naming<N> naming);

      protected abstract <N> boolean hasName(Naming<N> naming, N name);

      protected abstract String getContent(boolean trim);

      protected abstract boolean hasName(String namespaceURI, String localPart);

      protected abstract String getNamespaceByPrefix(String namespacePrefix);

      protected abstract Element next(int depth) throws StaxNavException;

      protected abstract Element next() throws StaxNavException;

      protected abstract QName getName();

      protected abstract int getDepth();

      protected abstract Location getLocation();

      protected abstract Map<String, String> getAttributes();

      protected abstract Map<QName, String> getQualifiedAttributes();

      protected abstract Map<String, String> getNamespaces();
   }

   private static class HeadElement extends Element
   {

      /** . */
      private final XMLStreamReader stream;

      /** . */
      private Element root;

      private HeadElement(XMLStreamReader stream)
      {
         this.stream = stream;
         this.root = null;
      }

      private Element get()
      {
         if (root == null)
         {
            try
            {
               while (stream.hasNext())
               {
                  int type = stream.getEventType();
                  if (type == XMLStreamConstants.START_ELEMENT)
                  {
                     root = new StreamElement(stream, null);
                     break;
                  }
                  else
                  {
                     stream.next();
                  }
               }
            }
            catch (XMLStreamException e)
            {
               throw new StaxNavException(stream.getLocation());
            }
         }
         if (root == null)
         {
            throw new StaxNavException(stream.getLocation(), "No head!!!!");
         }
         return root;
      }

      @Override
      protected Element getParent()
      {
         return null;
      }

      protected <N> N getName(Naming<N> naming)
      {
         return get().getName(naming);
      }

      protected <N> boolean hasName(Naming<N> naming, N name)
      {
         return get().hasName(naming, name);
      }

      protected String getContent(boolean trim)
      {
         return get().getContent(trim);
      }

      protected boolean hasName(String namespaceURI, String localPart)
      {
         return get().hasName(namespaceURI, localPart);
      }

      protected String getNamespaceByPrefix(String namespacePrefix)
      {
         return get().getNamespaceByPrefix(namespacePrefix);
      }

      protected Element next(int depth) throws StaxNavException
      {
         return get().next(depth);
      }

      protected Element next() throws StaxNavException
      {
         return get().next();
      }

      protected QName getName()
      {
         return get().getName();
      }

      protected int getDepth()
      {
         return get().getDepth();
      }

      protected Location getLocation()
      {
         return get().getLocation();
      }

      protected Map<String, String> getAttributes()
      {
         return get().getAttributes();
      }

      protected Map<QName, String> getQualifiedAttributes()
      {
         return get().getQualifiedAttributes();
      }

      protected Map<String, String> getNamespaces()
      {
         return get().getNamespaces();
      }
   }

   private static class StreamElement extends Element
   {

      /** . */
      private final XMLStreamReader stream;

      /** . */
      private final Element parent;

      /** . */
      private final QName name;

      /** . */
      private final int depth;

      /** The content is not null. */
      private final Object content;

      /** . */
      private final Location location;

      /** . */
      private final Map<String, String> attributes;

      /** . */
      private final Map<QName, String> qualifiedAttributes;

      /** . */
      private final Map<String, String> namespaces;

      /** . */
      private Element next;

      private StreamElement(XMLStreamReader stream, Element parent) throws XMLStreamException
      {
         // We assume that the stream points to the start of the modelled element
         if (stream.getEventType() != XMLStreamConstants.START_ELEMENT)
         {
            throw new AssertionError();
         }

         //
         QName name = stream.getName();
         Location location = stream.getLocation();

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
         String chunk = null;
         Object content = null;
         while (true)
         {
            stream.next();
            int type = stream.getEventType();
            if (type == XMLStreamConstants.END_DOCUMENT || type == XMLStreamConstants.START_ELEMENT)
            {
               break;
            }
            else if (type == XMLStreamConstants.CHARACTERS)
            {
               if (chunk == null)
               {
                  chunk = stream.getText();
               }
               else
               {
                  if (sb == null)
                  {
                     sb = new StringBuilder(chunk);
                  }
                  sb.append(stream.getText());
               }
            }
            else if (type == XMLStreamConstants.END_ELEMENT)
            {
               if (sb != null)
               {
                  content = sb;
               }
               else
               {
                  content = chunk;
               }
               break;
            }
         }

         //
         int depth = 1 + (parent != null ? parent.getDepth() : 0);

         //
         this.parent = parent;
         this.name = name;
         this.depth = depth;
         this.content = content;
         this.attributes = attributes;
         this.qualifiedAttributes = qualifiedAttributes;
         this.namespaces = namespaces;
         this.stream = stream;
         this.location = location;
      }

      @Override
      protected Element getParent()
      {
         return parent;
      }

      protected <N> N getName(Naming<N> naming)
      {
         return naming.getName(getName());
      }

      protected <N> boolean hasName(Naming<N> naming, N name)
      {
         return hasName(naming.getURI(name), naming.getLocalPart(name));
      }

      protected boolean hasName(String namespaceURI, String localPart)
      {
         if (localPart == null)
         {
            return true;
         }
         else
         {
            return (namespaceURI == null || namespaceURI.equals(getName().getNamespaceURI())) && localPart.equals(getName().getLocalPart());
         }
      }

      protected String getNamespaceByPrefix(String namespacePrefix)
      {
         for (Element current = this;current != null;current = current.getParent())
         {
            String namespaceURI = current.getNamespaces().get(namespacePrefix);
            if (namespaceURI != null)
            {
               return namespaceURI;
            }
         }
         return null;
      }

      protected Element next(int depth) throws StaxNavException
      {
         Element next = next();
         if (next != null && next.getDepth() > depth)
         {
            return next;
         }
         else
         {
            return null;
         }
      }

      protected Element next() throws StaxNavException
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
                     next = new StreamElement(stream, parent);
                     break;
                  }
                  else if (type == XMLStreamConstants.END_ELEMENT)
                  {
                     parent = parent.getParent();
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

      protected String getContent(boolean trim)
      {
         if (content != null)
         {
            String s = content.toString();
            if (trim)
            {
               s = s.trim();
            }
            return s;
         }
         else
         {
            return null;
         }
      }

      protected QName getName()
      {
         return name;
      }

      protected int getDepth()
      {
         return depth;
      }

      protected Location getLocation()
      {
         return location;
      }

      protected Map<String, String> getAttributes()
      {
         return attributes;
      }

      protected Map<QName, String> getQualifiedAttributes()
      {
         return qualifiedAttributes;
      }

      protected Map<String, String> getNamespaces()
      {
         return namespaces;
      }
   }
}
