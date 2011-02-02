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
import java.io.InputStream;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class QualifedStaxNavigator extends AbstractStaxNavigator<QName>
{

   public QualifedStaxNavigator(InputStream is)
   {
      super(is);
   }

   @Override
   protected String getURI(QName name)
   {
      return name != null ? name.getNamespaceURI() : null;
   }

   @Override
   protected String getLocalPart(QName name)
   {
      return name != null ? name.getLocalPart() : null;
   }

   @Override
   protected Pair createPair(String uri, String prefix, String localPart, String content)
   {
      return new Pair2(uri, prefix, localPart, content);
   }

   class Pair2 extends Pair
   {
      private QName name;
      private String value;

      @Override
      protected String getURI()
      {
         return name.getNamespaceURI();
      }

      @Override
      protected String getPrefix()
      {
         return name.getPrefix();
      }

      @Override
      protected String getLocalPart()
      {
         return name.getLocalPart();
      }

      @Override
      protected QName getName()
      {
         return name;
      }

      @Override
      protected String getValue()
      {
         return value;
      }

      public Pair2(String uri, String prefix, String localPart, String value)
      {
         this.name = prefix != null ? new QName(uri, localPart, prefix) : new QName(uri, localPart);
         this.value = value;
      }
   }
}
