package org.staxnav;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class SimpleEnumeratedBrowseTestCase extends AbstractBrowseTestCase<SampleName>
{

   @Override
   protected Naming<SampleName> getNaming()
   {
      return new Naming.Enumerated.Simple<SampleName>(SampleName.class, SampleName.DONOTEXIST);
   }
}
