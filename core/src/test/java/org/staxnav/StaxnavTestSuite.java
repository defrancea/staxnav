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


import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class StaxnavTestSuite
{
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(ContentTestCase.class));
      suite.addTest(new TestSuite(EncodedNamingTestCase.class));
      suite.addTest(new TestSuite(GateInTestCase.class));
      suite.addTest(new TestSuite(LocalBrowseTestCase.class));
      suite.addTest(new TestSuite(MappedEnumBrowseTestCase.class));
      suite.addTest(new TestSuite(NamespaceTestCase.class));
      suite.addTest(new TestSuite(QualifiedBrowseTestCase.class));
      suite.addTest(new TestSuite(SimpleEnumeratedBrowseTestCase.class));
      return suite;
   }
}
