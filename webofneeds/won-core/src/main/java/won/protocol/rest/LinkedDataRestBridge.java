package won.protocol.rest;

import org.apache.http.conn.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import won.cryptography.service.CryptographyUtils;
import won.cryptography.service.KeyStoreService;
import won.cryptography.service.TrustStoreService;
import won.cryptography.ssl.PrivateKeyStrategyGenerator;

import javax.annotation.PostConstruct;

/**
 * User: ypanchenko
 * Date: 02.02.2016
 */
public class LinkedDataRestBridge
{

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private RestTemplate restTemplateWithDefaultWebId;

  private Integer readTimeout;
  private Integer connectionTimeout;

  private PrivateKeyStrategyGenerator privateKeyStrategyGenerator;
  private KeyStoreService keyStoreService;
  private TrustStoreService trustStoreService;
  private TrustStrategy trustStrategy;



  public LinkedDataRestBridge(KeyStoreService keyStoreService, PrivateKeyStrategyGenerator
    privateKeyStrategyGenerator, TrustStoreService trustStoreService, TrustStrategy trustStrategy) {
    this.readTimeout = 10000;
    this.connectionTimeout = 10000; //DEF. TIMEOUT IS 10sec
    this.keyStoreService = keyStoreService;
    this.privateKeyStrategyGenerator = privateKeyStrategyGenerator;
    this.trustStoreService = trustStoreService;
    this.trustStrategy = trustStrategy;
  }

  @PostConstruct
  public void initialize() {
    try {
      restTemplateWithDefaultWebId = createRestTemplateForReadingLinkedData(this.keyStoreService
                                                                              .getDefaultAlias());
    } catch (Exception e) {
      logger.error("Failed to create ssl tofu rest template", e);
      throw new RuntimeException(e);
    }
  }


  public RestTemplate getRestTemplate() {
    return restTemplateWithDefaultWebId;
  }

  public RestTemplate getRestTemplate(String requesterWebID) {
    RestTemplate restTemplate;
    try {
      restTemplate = getRestTemplateForReadingLinkedData(requesterWebID);
    } catch (Exception e) {
      logger.error("Failed to create ssl tofu rest template", e);
      throw new RuntimeException(e);
    }

    return restTemplate;
  }


  private RestTemplate getRestTemplateForReadingLinkedData(String webID) throws Exception {

    if (webID.equals(keyStoreService.getDefaultAlias())) {
      return restTemplateWithDefaultWebId;
    }
    return createRestTemplateForReadingLinkedData(webID);
  }

  private RestTemplate createRestTemplateForReadingLinkedData(String webID) throws Exception {
    RestTemplate template = CryptographyUtils.createSslRestTemplate(
      this.keyStoreService.getUnderlyingKeyStore(),
      this.keyStoreService.getPassword(),
      privateKeyStrategyGenerator.createPrivateKeyStrategy(webID),
      this.trustStoreService.getUnderlyingKeyStore(),
      this.trustStrategy,
      readTimeout, connectionTimeout);
    //prevent the RestTemplate from throwing an exception when the server responds with 4xx or 5xx status
    //because we want to hand the orginal response back to the original caller in BridgeForLinkedDataController
    template.setErrorHandler(new DefaultResponseErrorHandler(){
      @Override
      protected boolean hasError(final HttpStatus statusCode) {
        return false;
      }
    });
    return template;
  }
}
