package won.bot.framework.bot.context;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.Serializable;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Bot context implementation using persistent mongo db for storage.
 *
 * Created by hfriedrich on 27.10.2016.
 */
public class MongoBotContext implements BotContext
{
  private static final String NEED_URI_NAME_COLLECTION = "need_uri_names";
  private static final String NODE_URI_COLLECTION = "node_uris";
  private static final String NAMED_NEED_URI_COLLECTION_PREFIX = "named_need_uri_";

  @Autowired
  private MongoTemplate template;

  public void setTemplate(final MongoTemplate template) {
    this.template = template;
  }

  @Override
  public Set<URI> retrieveAllNeedUris() {

    Set<URI> needUris = new HashSet<>();
    Set<String> needUriNames = getNeedUriNames();

    for (String needUriCollection : needUriNames) {
      List<MongoContextObject> contextObjects = template.findAll(
        MongoContextObject.class, NAMED_NEED_URI_COLLECTION_PREFIX + needUriCollection);
      needUris.addAll(contextObjects.stream().map(x -> (URI) x.getObject()).collect(Collectors.toSet()));
    }

    return needUris;
  }

  private Set<String> getNeedUriNames() {

    Set<String> needUriNames = new HashSet<>();
    List<MongoContextObject> contextObjects = template.findAll(MongoContextObject.class, NEED_URI_NAME_COLLECTION);
    needUriNames.addAll(contextObjects.stream().map(x -> (String) x.getObject()).collect(Collectors.toSet()));
    return needUriNames;
  }

  @Override
  public boolean isNeedKnown(final URI needURI) {

    Set<String> needUriNames = getNeedUriNames();
    for (String needUriCollection : needUriNames) {
      if (null != get(NAMED_NEED_URI_COLLECTION_PREFIX + needUriCollection, needURI.toString())) {
        return true;
      }
    }

    return false;
  }

  @Override
  public void removeNeedUriFromNamedNeedUriList(final URI uri, final String name) {
    remove(NAMED_NEED_URI_COLLECTION_PREFIX + name, uri.toString());
  }

  @Override
  public void appendToNamedNeedUriList(final URI uri, final String name) {
    put(NAMED_NEED_URI_COLLECTION_PREFIX + name, uri.toString(), uri);
    put(NEED_URI_NAME_COLLECTION, name, name);
  }

  @Override
  public List<URI> getNamedNeedUriList(final String name) {

    List<MongoContextObject> contextObjects = template.findAll(
      MongoContextObject.class, NAMED_NEED_URI_COLLECTION_PREFIX + name);
    List<URI> uriList = new LinkedList<>();
    uriList.addAll(contextObjects.stream().map(x -> (URI) x.getObject()).collect(Collectors.toSet()));
    return uriList;
  }

  @Override
  public boolean isNodeKnown(final URI wonNodeURI) {
    return null != get(NODE_URI_COLLECTION, wonNodeURI.toString());
  }

  @Override
  public void rememberNodeUri(final URI uri) {
    put(NODE_URI_COLLECTION, uri.toString(), uri);
  }

  @Override
  public void removeNodeUri(final URI uri) {
    remove(NODE_URI_COLLECTION, uri.toString());
  }

  /**
   * Use this method to make sure that certain collections (need and node uris) are only accessed with non-generic
   * methods
   *
   * @param collectionName
   */
  private void checkValidCollectionName(String collectionName) {
    if (collectionName == null || collectionName.equals(NEED_URI_NAME_COLLECTION) || collectionName.equals
      (NODE_URI_COLLECTION) || collectionName.startsWith(NAMED_NEED_URI_COLLECTION_PREFIX) ||
      collectionName.trim().isEmpty()) {
      throw new IllegalArgumentException("Generic collection name must be valid and not one of the following:" +
                                           " " + NODE_URI_COLLECTION + ", " + NEED_URI_NAME_COLLECTION + ", " +
                                           NAMED_NEED_URI_COLLECTION_PREFIX + "...");
    }
  }

  @Override
  public void putGeneric(String collectionName, String key, final Serializable value) {
    checkValidCollectionName(collectionName);
    put(collectionName, key, value);
  }

  private void put(String collectionName, String key, final Object value) {
    MongoContextObject mco = new MongoContextObject(key, value);
    template.save(mco, collectionName);
  }

  @Override
  public final Object getGeneric(String collectionName, String key) {
    checkValidCollectionName(collectionName);
    return get(collectionName, key);
  }

  private Object get(String collectionName, String key) {

    MongoContextObject mco = template.findById(key, MongoContextObject.class, collectionName);
    if (mco != null) {
      return mco.getObject();
    }
    return null;
  }

  @Override
  public Collection<Object> genericValues(String collectionName) {
    checkValidCollectionName(collectionName);
    return values(collectionName);
  }

  private Collection<Object> values(String collectionName) {

    List<MongoContextObject> contextObjects = template.findAll(MongoContextObject.class, collectionName);
    Collection<Object> objects = new HashSet<>();
    objects.addAll(contextObjects.stream().map(x -> x.getObject()).collect(Collectors.toList()));
    return objects;
  }

  @Override
  public final void removeGeneric(String collectionName, String key) {
    checkValidCollectionName(collectionName);
    remove(collectionName, key);
  }

  private void remove(String collectionName, String key) {
    MongoContextObject mco = new MongoContextObject(key, null);
    template.remove(mco, collectionName);
  }
}