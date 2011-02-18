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

import java.io.InputStream;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class EnumStaxNavigator<E extends Enum<E>> extends AbstractStaxNavigator<E>
{

   /** . */
   private final Class<E> enumType;

   public EnumStaxNavigator(InputStream is, Class<E> enumType)
   {
      super(is);
      this.enumType = enumType;
   }

   @Override
   protected String getLocalPart(E name)
   {
      String s = name.name();
      if (s.indexOf('_') >= 0)
      {
         s = s.replace('_', '-');
      }
      return s;
   }

   @Override
   protected String getURI(E name)
   {
      return name == null ? null : "";
   }

   @Override
   protected String getPrefix(E name)
   {
      return name == null ? null : "";
   }

   @Override
   protected E getName(String uri, String prefix, String localPart)
   {
      if (localPart.indexOf('-') != -1)
      {
         localPart = localPart.replace('-', '_');
      }
      return Enum.valueOf(enumType, localPart);
   }
}
