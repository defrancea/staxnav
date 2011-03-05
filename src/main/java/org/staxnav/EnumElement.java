package org.staxnav;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public interface EnumElement<E extends Enum<E>>
{
   String getLocalName();
}
