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
   protected Pair createPair(String uri, String localName, String content)
   {
      return new Pair2(uri, localName, content);
   }

   class Pair2 extends Pair
   {
      private String uri;
      private QName name;
      private String value;

      @Override
      protected String getURI()
      {
         return uri;
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

      public Pair2(final String uri, final String name, final String value)
      {
         this.uri = uri;
         this.name = new QName(uri, name);
         this.value = value;
      }
   }
}
