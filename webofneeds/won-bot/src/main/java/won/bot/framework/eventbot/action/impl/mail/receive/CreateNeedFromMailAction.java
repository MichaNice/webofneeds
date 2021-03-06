package won.bot.framework.eventbot.action.impl.mail.receive;

import com.hp.hpl.jena.rdf.model.Model;
import org.apache.commons.lang3.StringUtils;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.EventBotActionUtils;
import won.bot.framework.eventbot.action.impl.needlifecycle.AbstractCreateNeedAction;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.mail.CreateNeedFromMailEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.FailureResponseEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.protocol.message.WonMessage;
import won.protocol.model.BasicNeedType;
import won.protocol.model.FacetType;
import won.protocol.service.WonNodeInformationService;
import won.protocol.util.NeedModelBuilder;
import won.protocol.util.RdfUtils;
import won.protocol.util.WonRdfUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by fsuda on 30.09.2016.
 */
public class CreateNeedFromMailAction extends AbstractCreateNeedAction {
    private String uriMimeMessageRelationsName;
    private MailContentExtractor mailContentExtractor;

    public CreateNeedFromMailAction(EventListenerContext eventListenerContext, String uriListName,
                                    MailContentExtractor mailContentExtractor, String uriMimeMessageRelationsName,
                                    URI... facets) {

        super(eventListenerContext, uriListName);
        this.uriMimeMessageRelationsName = uriMimeMessageRelationsName;
        this.mailContentExtractor = mailContentExtractor;

        if (facets == null || facets.length == 0) {
            //add the default facet if none is present.
            this.facets = new ArrayList<URI>(1);
            this.facets.add(FacetType.OwnerFacet.getURI());
        } else {
            this.facets = Arrays.asList(facets);
        }
    }

    protected void doRun(Event event) throws Exception {
        if(event instanceof CreateNeedFromMailEvent){
            MimeMessage message = ((CreateNeedFromMailEvent) event).getMessage();

            try {
                BasicNeedType type = mailContentExtractor.getBasicNeedType(message);
                assert type != null; //Done as a failsafe, this Action should never be called if it is not a valid CreateNeed-Mail

                String title = mailContentExtractor.getTitle(message);
                String description = mailContentExtractor.getDescription(message);
                String[] tags = mailContentExtractor.getTags(message);
                boolean isUsedForTesting = mailContentExtractor.isUsedForTesting(message);
                boolean isDoNotMatch = mailContentExtractor.isDoNotMatch(message);

                EventListenerContext ctx = getEventListenerContext();
                WonNodeInformationService wonNodeInformationService = ctx.getWonNodeInformationService();

                final URI wonNodeUri = ctx.getNodeURISource().getNodeURI();
                final URI needURI = wonNodeInformationService.generateNeedURI(wonNodeUri);
                Model model = new NeedModelBuilder()
                        .setTitle(title)
                        .setBasicNeedType(type)
                        .setDescription(description)
                        .setUri(needURI)
                        .setTags(tags)
                        .setFacetTypes(facets)
                        .build();

                logger.debug("creating need on won node {} with content {} ", wonNodeUri, StringUtils.abbreviate(RdfUtils.toString(model), 150));

                WonMessage createNeedMessage = createWonMessage(wonNodeInformationService, needURI, wonNodeUri,
                                                                model, isUsedForTesting, isDoNotMatch);
                EventBotActionUtils.rememberInList(ctx, needURI, uriListName);
                EventBotActionUtils.addUriMimeMessageRelation(ctx, uriMimeMessageRelationsName, needURI, message);

                EventListener successCallback = new EventListener()
                {
                    @Override
                    public void onEvent(Event event) throws Exception {
                        logger.debug("need creation successful, new need URI is {}", needURI);
                        String sender = MailContentExtractor.getFromAddressString(EventBotActionUtils.getMimeMessageForURI(getEventListenerContext(), uriMimeMessageRelationsName, needURI));
                        logger.debug("created need was from sender: " + sender);
                    }
                };

                EventListener failureCallback = new EventListener()
                {
                    @Override
                    public void onEvent(Event event) throws Exception {
                        String textMessage = WonRdfUtils.MessageUtils.getTextMessage(((FailureResponseEvent) event).getFailureMessage());
                        logger.debug("need creation failed for need URI {}, original message URI {}: {}", new Object[]{needURI, ((FailureResponseEvent) event).getOriginalMessageURI(), textMessage});
                        EventBotActionUtils.removeFromList(getEventListenerContext(), needURI, uriListName);
                        EventBotActionUtils.removeUriMimeMessageRelation(getEventListenerContext(), uriMimeMessageRelationsName, needURI);
                    }
                };
                EventBotActionUtils.makeAndSubscribeResponseListener(createNeedMessage, successCallback, failureCallback, getEventListenerContext());

                logger.debug("registered listeners for response to message URI {}", createNeedMessage.getMessageURI());
                getEventListenerContext().getWonMessageSender().sendWonMessage(createNeedMessage);
                logger.debug("need creation message sent with message URI {}", createNeedMessage.getMessageURI());
            }  catch (MessagingException me){
                logger.error("messaging exception occurred: {}", me);
            }
        }
    }
}
