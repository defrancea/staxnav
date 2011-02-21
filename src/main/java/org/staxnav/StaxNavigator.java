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

import javax.xml.stream.XMLStreamException;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 * @param <N> the name type
 */
public interface StaxNavigator<N>
{

   /**
    * Returns the current navigated element name.
    *
    * @return the element name
    * @throws XMLStreamException any underlying XMLStreamException
    */
   N getName() throws XMLStreamException;

   /**
    * Returns the current navigated element depth.
    *
    * @return the element level
    * @throws XMLStreamException any underlying XMLStreamException
    */
   int getDepth() throws XMLStreamException;

   /**
    * Returns the current navigated element textual content. Note that this method is only valid when an element
    * content is not mixed, if an element has a mixed content then this method will not return an accurate result
    * it would likely return the first chunk of text found.
    *
    * @return the element text content
    * @throws XMLStreamException any underlying XMLStreamException
    */
   String getContent() throws XMLStreamException;

   /**
    * Attemps to navigate to an element following the current one when it has the specified name.
    * If the navigation occurs, the navigator now points to that element and the method returns true.
    * Otherwise no navigation happen and the method return false.
    *
    * @param name the element name to find
    * @return true if the desired element is reached
    * @throws XMLStreamException any underlying XMLStreamException
    */
   boolean find(N name) throws XMLStreamException;

   /**
    * Navigates to the next element and returns its name.
    *
    * @return the element name
    * @throws XMLStreamException any underlying XMLStreamException
    */
   N next() throws XMLStreamException;

   /**
    * Attempt to navigate to the next element when it has the specified name.
    * If the navigation occurs, the navigator now points to that element and the method returns true.
    * Otherwise no navigation happen and the method return false.
    *
    * @param name the desired element name
    * @return true if the desired element is reached
    * @throws XMLStreamException any underlying XMLStreamException
    */
   boolean next(N name) throws XMLStreamException;

   /**
    * Attempts to navigate to the first child found and return its name. If no such child exist then null
    * is returned.
    *
    * @return the child name
    * @throws XMLStreamException any underlying XMLStreamException
    */
   N child() throws XMLStreamException;

   /**
    * Attempts to navigate to the first child with the specified name.
    * If the navigation occurs, the navigator now points to that element and the method returns true.
    * Otherwise no navigation happen and the method return false.
    *
    * @param name the child name
    * @return true if the desired element is reached
    * @throws NullPointerException if the name argument is null
    * @throws XMLStreamException any underlying XMLStreamException
    */
   boolean child(N name) throws NullPointerException, XMLStreamException;

   /**
    * Returns an attribute of the current element or null if such attribute does not exist.
    *
    * @param name the attribute name
    * @return the attribute value
    * @throws NullPointerException if the name argument is null
    * @throws IllegalStateException if no element is currently navigated
    * @throws XMLStreamException any underlying XMLStreamException
    */
   String getAttribute(String name) throws NullPointerException, IllegalStateException, XMLStreamException;

   /**
    * Returns a namespace URI by its prefix or return null if it is not bound.
    *
    *
    * @param prefix the prefix
    * @return the corresponding namespace URI
    * @throws NullPointerException if the prefix is null
    * @throws XMLStreamException any underlying XMLStreamException
    */
   String getNamespaceByPrefix(String prefix) throws NullPointerException, XMLStreamException;

   /**
    * Attempt to navigate to the next sibling and return its name. If no such sibling exists
    * then null is returned.
    *
    * @return the next sibling name
    * @throws XMLStreamException any underlying XMLStreamException
    */
   N sibling() throws XMLStreamException;

   /**
    * Attempts to navigate to the next sibling with the specified name.
    * If the navigation occurs, the navigator now points to that element and the method returns true.
    * Otherwise no navigation happen and the method return false.
    *
    * @param name the next sibling name
    * @return true if the desired element is reached
    * @throws NullPointerException if the name argument is null
    * @throws XMLStreamException any underlying XMLStreamException
    */
   boolean sibling(N name) throws NullPointerException, XMLStreamException;

   /**
    * Attempts to navigate to the first descendant with the specified name. The returned value should be interpreted as:
    * <ul>
    * <li>a negative value means that no navigation occured</li>
    * <li>any other value is the difference of depth between the two elements</li>
    * </ul>
    *
    * @param name the descendant name
    * @return the
    * @throws NullPointerException if the name is null
    * @throws XMLStreamException any underlying XMLStreamException
    */
   int descendant(N name) throws NullPointerException, XMLStreamException;
}
