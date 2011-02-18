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
    */
   N getName();

   /**
    * Returns the current navigated element level.
    *
    * @return the element level
    */
   int getLevel();

   /**
    * Returns the current navigated element textual content. Note that this method is only valid when an element
    * content is not mixed, if an element has a mixed content then this method will not return an accurate result
    * it would likely return the first chunk of text found.
    *
    * @return the element text content
    */
   String getContent();

   String getTrimmedContent();

   /**
    * Parse the current content to the specified value type.
    *
    * @param valueType the value type to parse to
    * @param <V> the value generic type
    * @return the parsed value
    * @throws NullPointerException if the value type is null
    * @throws IllegalStateException if the navigator is not navigated to a content container
    * @throws TypeConversionException anything that would prevent type conversion to happen
    */
   <V> V parseContent(ValueType<V> valueType) throws NullPointerException, IllegalStateException, TypeConversionException;

   /**
    * Initialize the parsing and returns the root name found.
    *
    * @return the root name
    * @throws XMLStreamException any underlying XMLStreamException
    */
   N root() throws XMLStreamException;

   /**
    * Navigates to the next element and returns its name.
    *
    * @return the element name
    * @throws XMLStreamException any underlying XMLStreamException
    */
   N next() throws XMLStreamException;

   /**
    * Attempt to navigate to the next element having the specified name.
    * If the navigation occurs, the navigator now points to the next element with the specified name
    * and the method returns true. Otherwise no navigation happens and the method returns false.
    *
    * @param name the desired element name
    * @return true if the element is reached
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
    * If the navigation occurs, the navigator now points to the first child with the specified name
    * and the method returns true. Otherwise no navigation happens and the method returns false.
    *
    * @param name the child name
    * @return true when the child is found
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
    */
   String getAttribute(String name) throws NullPointerException, IllegalStateException;

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
    * If the navigation occurs, the navigator now points to the next sibling with the specified name
    * and the method returns true. Otherwise no navigation happens and the method returns false.
    *
    * @param name the next sibling name
    * @return true when the next sibling is found
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
