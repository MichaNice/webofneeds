package won.node.facet.impl;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import won.protocol.exception.ConnectionAlreadyExistsException;
import won.protocol.exception.IllegalMessageForNeedStateException;
import won.protocol.exception.NoSuchNeedException;
import won.protocol.model.Connection;
import won.protocol.model.FacetType;
import won.protocol.util.RdfUtils;
import won.protocol.vocabulary.SIOC;

/**
 * User: gabriel
 * Date: 17/01/14
 */
public class CommentModeratedFacet extends AbstractFacet
{
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  @Override
  public FacetType getFacetType() {
    return FacetType.CommentModeratedFacet;
  }

  @Override
  public void connectFromOwner(Connection con, Model content, Dataset messageEvent) throws NoSuchNeedException, IllegalMessageForNeedStateException, ConnectionAlreadyExistsException {
    super.connectFromOwner(con, content, messageEvent);
    /* when connected change linked data*/
    PrefixMapping prefixMapping = PrefixMapping.Factory.create();
    prefixMapping.setNsPrefix(SIOC.getURI(),"sioc");
    content.withDefaultMappings(prefixMapping);
    content.setNsPrefix("sioc",SIOC.getURI());
    Resource post = content.createResource(con.getConnectionURI() + "/p/", SIOC.POST);
    content.add(content.createStatement(content.getResource(con.getConnectionURI().toString()), SIOC.HAS_REPLY,
                                        content.getResource(con.getRemoteConnectionURI().toString())));
    logger.debug(RdfUtils.toString(content));
    rdfStorageService.storeContent(con.getConnectionURI(),content);
  }
}