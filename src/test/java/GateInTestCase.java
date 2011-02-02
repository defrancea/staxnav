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

import junit.framework.TestCase;
import org.staxnav.SimpleStaxNavigator;

import java.io.InputStream;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class GateInTestCase extends TestCase
{
   private InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("portlet-application.xml");
   private SimpleStaxNavigator navigator = new SimpleStaxNavigator(is);

   public void testPortlet() throws Exception
   {
      navigator.root();
      assertTrue(navigator.child("portlet"));
      assertTrue(navigator.child("application-ref"));
      String applicationRef = navigator.getText();
      navigator.sibling("portlet-ref");
      String portletRef = navigator.getText();
      assertTrue(navigator.sibling("preferences"));
      assertTrue(navigator.child("preference"));
      assertTrue(navigator.child("name"));
      String prefName = navigator.getText();
      assertTrue(navigator.sibling("value"));
      String prefValue = navigator.getText();
      assertTrue(navigator.sibling("read-only"));
      String prefReadonly = navigator.getText();

      assertEquals("web", applicationRef);
      assertEquals("BannerPortlet", portletRef);
      assertEquals("template", prefName);
      assertEquals("template_value", prefValue);
      assertEquals("false", prefReadonly);
   }
}
