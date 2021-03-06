/*
 * Copyright 2012  Research Studios Austria Forschungsges.m.b.H.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package won.bot.framework.eventbot.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import won.bot.framework.bot.context.BotContext;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.impl.mail.model.WonURI;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.wonmessage.FailureResponseEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.SuccessResponseEvent;
import won.bot.framework.eventbot.filter.impl.AcceptOnceFilter;
import won.bot.framework.eventbot.filter.impl.OriginalMessageUriRemoteResponseEventFilter;
import won.bot.framework.eventbot.filter.impl.OriginalMessageUriResponseEventFilter;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.framework.eventbot.listener.impl.ActionOnEventListener;
import won.protocol.message.WonMessage;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * User: fkleedorfer
 * Date: 02.02.14
 */
public class EventBotActionUtils
{
    private static final Logger logger = LoggerFactory.getLogger(EventBotActionUtils.class);

    private static final String USER_SUBSCRIBE_COLLECTION = "user_subscribe_status";
    private static final String USER_CACHED_MAILS_COLLECTION = "user_cached_mails";

    public static void rememberInList(EventListenerContext ctx, URI uri, String uriListName) {
        if (uriListName != null && uriListName.trim().length() > 0){
            ctx.getBotContext().appendToNamedNeedUriList(uri, uriListName);
            logger.debug("remembering need in NamedNeedList {} ", uri);
        } else {
            throw new IllegalArgumentException("'uriListName' must not not be null or empty");
        }
    }

    public static void rememberInNodeListIfNamePresent(EventListenerContext ctx, URI uri){
        ctx.getBotContext().rememberNodeUri(uri);
    }

    public static void removeFromList(EventListenerContext ctx, URI uri, String uriListName) {
        if (uriListName != null && uriListName.trim().length() > 0){
            ctx.getBotContext().removeNeedUriFromNamedNeedUriList(uri, uriListName);
            logger.debug("removing need from NamedNeedList {} ", uri);
        } else {
            throw new IllegalArgumentException("'uriListName' must not not be null or empty");
        }
    }

    /**
    * Creates a listener that waits for the response to the specified message. If a SuccessResponse is received,
    * the successCallback is executed, if a FailureResponse is received, the failureCallback is executed.
    * @param outgoingMessage
    * @param successCallback
    * @param failureCallback
    * @param context
    * @return
    */
    public static EventListener makeAndSubscribeResponseListener(final WonMessage outgoingMessage,
        final EventListener successCallback, final EventListener failureCallback, EventListenerContext context) {

        //create an event listener that processes the response to the wonMessage we're about to send
        EventListener listener = new ActionOnEventListener(context,
            new AcceptOnceFilter(OriginalMessageUriResponseEventFilter.forWonMessage(outgoingMessage)),
            new BaseEventBotAction(context)
            {
                @Override
                protected void doRun(final Event event) throws Exception {
                    if (event instanceof SuccessResponseEvent) {
                        successCallback.onEvent(event);
                    } else  if (event instanceof FailureResponseEvent){
                        failureCallback.onEvent(event);
                    }
                }
            }
        );
        context.getEventBus().subscribe(SuccessResponseEvent.class, listener);
        context.getEventBus().subscribe(FailureResponseEvent.class, listener);
        return listener;
    }


    /**
    * Creates a listener that waits for the remote response to the specified message. If a SuccessResponse is received,
    * the successCallback is executed, if a FailureResponse is received, the failureCallback is executed.
    * @param outgoingMessage
    * @param successCallback
    * @param failureCallback
    * @param context
    * @return
    */
    public static EventListener makeAndSubscribeRemoteResponseListener(final WonMessage outgoingMessage,
        final EventListener successCallback, final EventListener failureCallback, EventListenerContext context) {

        //create an event listener that processes the remote response to the wonMessage we're about to send
        EventListener listener = new ActionOnEventListener(context,
            new AcceptOnceFilter(OriginalMessageUriRemoteResponseEventFilter.forWonMessage(outgoingMessage)),
            new BaseEventBotAction(context)
            {
                @Override
                protected void doRun(final Event event) throws Exception {
                    if (event instanceof SuccessResponseEvent) {
                        successCallback.onEvent(event);
                    } else  if (event instanceof FailureResponseEvent){
                        failureCallback.onEvent(event);
                    }
                }
            }
        );
        context.getEventBus().subscribe(SuccessResponseEvent.class, listener);
        context.getEventBus().subscribe(FailureResponseEvent.class, listener);
        return listener;
    }

    //Util Methods to Get/Remove/Add Uri -> MimeMessage Relation
    public static void removeUriMimeMessageRelation(EventListenerContext context, String mapName, URI needURI) {
        context.getBotContext().removeFromObjectMap(mapName, needURI.toString());
    }

    public static MimeMessage getMimeMessageForURI(EventListenerContext context, String mapName, URI uri)
      throws MessagingException {

        // use the empty default session here for reconstructing the mime message
        byte[] byteMsg = (byte[]) context.getBotContext().loadFromObjectMap(mapName, uri.toString());
        ByteArrayInputStream is = new ByteArrayInputStream(byteMsg);
        return new MimeMessage(Session.getDefaultInstance(new Properties(), null), is);
    }

    public static void addUriMimeMessageRelation(EventListenerContext context, String mapName, URI needURI, MimeMessage mimeMessage)
      throws IOException, MessagingException {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        mimeMessage.writeTo(os);
        context.getBotContext().saveToObjectMap(mapName, needURI.toString(), os.toByteArray());
    }

    //Util Methods to Get/Remove/Add MailId -> URI Relation
    public static void removeMailIdWonURIRelation(EventListenerContext context, String mapName, String mailId) {
        context.getBotContext().removeFromObjectMap(mapName, mailId);
    }

    public static WonURI getWonURIForMailId(EventListenerContext context, String mapName, String mailId) {
        return (WonURI) context.getBotContext().loadFromObjectMap(mapName, mailId);
    }

    public static void addMailIdWonURIRelation(EventListenerContext context, String mapName, String mailId, WonURI uri) {
        context.getBotContext().saveToObjectMap(mapName, mailId, uri);
    }

    public static void addCachedMailsForMailAddress(BotContext botContext, MimeMessage mimeMessage)
      throws IOException, MessagingException {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        mimeMessage.writeTo(os);
        botContext.addToListMap(USER_CACHED_MAILS_COLLECTION, mimeMessage.getFrom().toString(), os.toByteArray());
    }

    public static Collection<MimeMessage> loadCachedMailsForMailAddress(BotContext botContext, String mailAddress)
      throws MessagingException {

        List<MimeMessage> mimeMessages = new LinkedList<>();
        List<Object> objectList = botContext.loadFromListMap(USER_CACHED_MAILS_COLLECTION, mailAddress);
        for (Object o : objectList) {

            // use the empty default session here for reconstructing the mime message
            byte[] byteMsg = (byte[]) o;
            ByteArrayInputStream is = new ByteArrayInputStream(byteMsg);
            mimeMessages.add(new MimeMessage(Session.getDefaultInstance(new Properties(), null), is));
        }

        return mimeMessages;
    }

    public static void removeCachedMailsForMailAddress(BotContext botContext, String mailAddress) {
        botContext.dropCollection(mailAddress);
    }
}
