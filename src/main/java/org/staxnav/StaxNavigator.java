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

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public interface StaxNavigator
{
   void init();
   String child();

   /**
    * Attempts to navigate to the first child with the specified name.
    * If the navigation occurs, the navigator now points to the first child with the specified name
    * and the method returns true. Otherwise no navigation occured and the method returns false.
    *
    * @param name the child name
    * @return true when the child is found
    * @throws NullPointerException if the name argument is null
    */
   boolean child(String name) throws NullPointerException;

   /**
    * Returns an attribute of the current element or null if such attribute does not exist.
    *
    * @param name the attribute name
    * @return the attribute value
    * @throws NullPointerException if the name argument is null
    * @throws IllegalStateException if no element is currently navigated
    */
   String getAttribute(String name) throws NullPointerException, IllegalStateException;

   String sibbling();
   String sibbling(String name);
   String getName();
   int getLevel();
   String getText();
}
