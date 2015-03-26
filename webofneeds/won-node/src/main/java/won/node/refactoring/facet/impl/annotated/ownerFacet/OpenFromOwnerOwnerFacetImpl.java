package won.node.refactoring.facet.impl.annotated.ownerFacet;

import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;
import won.node.messaging.processors.AbstractFromOwnerCamelProcessor;
import won.node.messaging.processors.DefaultFacetMessageProcessor;
import won.node.messaging.processors.FacetMessageProcessor;
import won.protocol.vocabulary.WON;
import won.protocol.vocabulary.WONMSG;

/**
 * User: syim
 * Date: 05.03.2015
 */
@Component
@DefaultFacetMessageProcessor(direction=WONMSG.TYPE_FROM_OWNER_STRING,messageType = WONMSG.TYPE_OPEN_STRING)
@FacetMessageProcessor(facetType = WON.OWNER_FACET_STRING,direction=WONMSG.TYPE_FROM_OWNER_STRING,messageType =
  WONMSG.TYPE_OPEN_STRING)
public class OpenFromOwnerOwnerFacetImpl extends AbstractFromOwnerCamelProcessor
{
  @Override
  public void process(final Exchange exchange) {
    logger.debug("default facet implementation, not doing anything");
  }
}
