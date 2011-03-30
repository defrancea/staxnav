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

import java.util.EnumSet;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class AbstractEnumeratedBrowseTestCase extends AbstractBrowseTestCase<SampleName>
{

   public void testGetNameNoSuchElement()
   {
      StaxNavigator<SampleName> nav = navigator(getNaming(), "<zorglub/>");
      assertEquals(SampleName.DONOTEXIST, nav.getName());
   }

   public void testFindNoSuchElement()
   {
      StaxNavigator<SampleName> nav = navigator(getNaming(), "<zorglub/>");
      assertTrue(nav.find(SampleName.DONOTEXIST));
   }

   public void testNext1NoSuchElement()
   {
      StaxNavigator<SampleName> nav = navigator(getNaming(), "<foo><zorglub/></foo>");
      assertEquals(SampleName.DONOTEXIST, nav.next());
   }

   public void testNext2NoSuchElement()
   {
      StaxNavigator<SampleName> nav = navigator(getNaming(), "<foo><zorglub/></foo>");
      assertTrue(nav.next(SampleName.DONOTEXIST));
   }

   public void testNext3NoSuchElement()
   {
      StaxNavigator<SampleName> nav = navigator(getNaming(), "<foo><zorglub/></foo>");
      assertFalse(nav.next(SampleName.BAR1));
   }

   public void testNext4NoSuchElement()
   {
      StaxNavigator<SampleName> nav = navigator(getNaming(), "<foo><zorglub/></foo>");
      assertNull(nav.next(EnumSet.of(SampleName.BAR1)));
   }

   public void testNext5NoSuchElement()
   {
      StaxNavigator<SampleName> nav = navigator(getNaming(), "<foo><zorglub/></foo>");
      assertEquals(SampleName.DONOTEXIST, nav.next(EnumSet.of(SampleName.DONOTEXIST)));
   }

   public void testChild1NoSuchElement()
   {
      StaxNavigator<SampleName> nav = navigator(getNaming(), "<foo><zorglub/></foo>");
      assertTrue(nav.child(SampleName.DONOTEXIST));
   }

   public void testChild2NoSuchElement()
   {
      StaxNavigator<SampleName> nav = navigator(getNaming(), "<foo><zorglub/></foo>");
      assertFalse(nav.child(SampleName.BAR1));
   }

   public void testSibling1NoSuchElement()
   {
      StaxNavigator<SampleName> nav = navigator(getNaming(), "<foo><foo1/><zorglub/></foo>");
      assertTrue(nav.child(SampleName.FOO1));
      assertEquals(SampleName.DONOTEXIST, nav.sibling());
   }

   public void testSibling2NoSuchElement()
   {
      StaxNavigator<SampleName> nav = navigator(getNaming(), "<foo><foo1/><zorglub/></foo>");
      assertTrue(nav.child(SampleName.FOO1));
      assertTrue(nav.sibling(SampleName.DONOTEXIST));
   }

   public void testDescendantNoSuchElement()
   {
      StaxNavigator<SampleName> nav = navigator(getNaming(), "<foo><zorglub/></foo>");
      assertEquals(1, nav.descendant(SampleName.DONOTEXIST));
   }
}
