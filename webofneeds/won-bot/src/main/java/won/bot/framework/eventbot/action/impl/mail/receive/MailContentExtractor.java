package won.bot.framework.eventbot.action.impl.mail.receive;

import won.bot.framework.eventbot.action.impl.mail.model.ActionType;
import won.protocol.model.BasicNeedType;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts need properties like title, description, need type, etc from mails. Configurable via regular expression
 * patterns in spring xml
 */
public class MailContentExtractor
{
  // check if mail is of type demand/supply/doTogether/critique?
  private Pattern demandTypePattern;
  private Pattern supplyTypePattern;
  private Pattern doTogetherTypePattern;
  private Pattern critiqueTypePattern;

  // extract array of tags from the content
  private Pattern tagExtractionPattern;

  // extract text message from the mail (e.g. excluding the quoted parts of earlier mails)
  private Pattern textMessageExtractionPattern;

  // extract title from mail subject
  private Pattern titleExtractionPattern;

  // extract description from mail content
  private Pattern descriptionExtractionPattern;

  // check if the need created from the mail should is used for testing only
  private Pattern usedForTestingPattern;

  // check if the need created from the mail should be not matched with other needs
  private Pattern doNotMatchPattern;

  // check if this is a command mail of different action types
  private Pattern cmdClosePattern;
  private Pattern cmdConnectPattern;
  private Pattern cmdSubscribePattern;
  private Pattern cmdUnsubscribePattern;

  public void setDemandTypePattern(final Pattern demandTypePattern) {
    this.demandTypePattern = demandTypePattern;
  }

  public void setSupplyTypePattern(final Pattern supplyTypePattern) {
    this.supplyTypePattern = supplyTypePattern;
  }

  public void setDoTogetherTypePattern(final Pattern doTogetherTypePattern) {
    this.doTogetherTypePattern = doTogetherTypePattern;
  }

  public void setCritiqueTypePattern(final Pattern critiqueTypePattern) {
    this.critiqueTypePattern = critiqueTypePattern;
  }

  public void setTagExtractionPattern(final Pattern tagExtractionPattern) {
    this.tagExtractionPattern = tagExtractionPattern;
  }

  public void setTextMessageExtractionPattern(final Pattern textMessageExtractionPattern) {
    this.textMessageExtractionPattern = textMessageExtractionPattern;
  }

  public void setTitleExtractionPattern(final Pattern titleExtractionPattern) {
    this.titleExtractionPattern = titleExtractionPattern;
  }

  public void setDescriptionExtractionPattern(final Pattern descriptionExtractionPattern) {
    this.descriptionExtractionPattern = descriptionExtractionPattern;
  }

  public void setUsedForTestingPattern(Pattern usedForTestingPattern) {
    this.usedForTestingPattern = usedForTestingPattern;
  }

  public void setDoNotMatchPattern(Pattern doNotMatchPattern) {
    this.doNotMatchPattern = doNotMatchPattern;
  }

  public void setCmdClosePattern(final Pattern cmdClosePattern) {
    this.cmdClosePattern = cmdClosePattern;
  }

  public void setCmdConnectPattern(final Pattern cmdConnectPattern) {
    this.cmdConnectPattern = cmdConnectPattern;
  }

  public void setCmdSubscribePattern(final Pattern cmdSubscribePattern) {
    this.cmdSubscribePattern = cmdSubscribePattern;
  }

  public void setCmdUnsubscribePattern(final Pattern cmdUnsubscribePattern) {
    this.cmdUnsubscribePattern = cmdUnsubscribePattern;
  }

  public ActionType getMailAction(MimeMessage message) throws IOException, MessagingException {

    if (cmdSubscribePattern.matcher(message.getSubject()).matches()) {
      return ActionType.SUBSCRIBE;
    } else if (cmdUnsubscribePattern.matcher(message.getSubject()).matches()) {
      return ActionType.UNSUBSCRIBE;
    } else if (cmdClosePattern.matcher(message.getSubject()).matches()) {
      return ActionType.CLOSE_CONNECTION;
    } else if (cmdConnectPattern.matcher(message.getSubject()).matches()) {
      return ActionType.OPEN_CONNECTION;
    } else {
      return ActionType.NO_ACTION;
    }
  }

  public boolean isDoNotMatch(MimeMessage message) throws MessagingException {
    return doNotMatchPattern.matcher(message.getSubject()).matches();
  }

  public boolean isUsedForTesting(MimeMessage message) throws MessagingException {
    return usedForTestingPattern.matcher(message.getSubject()).matches();
  }

  public String getTitle(MimeMessage message) throws MessagingException {

    Matcher m = titleExtractionPattern.matcher(message.getSubject());
    if (m.find()) {
      return m.group().trim();
    }

    return null;
  }

  public static String getMailText(MimeMessage message) throws MessagingException, IOException {

    if (message.isMimeType("text/plain")) {
      return message.getContent().toString();
    } else if (message.isMimeType("multipart/*")) {
      return getMailTextFromMultiPart((MimeMultipart) message.getContent());
    }

    return null;
  }

  private static String getMailTextFromMultiPart(MimeMultipart mm) throws MessagingException, IOException {

    for (int i = 0; i < mm.getCount(); i++) {
      BodyPart part = mm.getBodyPart(i);
      if (part.isMimeType("text/plain")) {
        return part.getContent().toString();
      } else if (part.isMimeType("multipart/*")) {
        return getMailTextFromMultiPart((MimeMultipart) part.getContent());
      }
    }
    return null;
  }

  public String getDescription(MimeMessage message) throws MessagingException, IOException {

    String desc = getMailText(message);
    if (desc != null) {
      Matcher m = descriptionExtractionPattern.matcher(desc);
      if (m.find()) {
        return m.group().trim();
      }
    }

    return null;
  }

  public String getTextMessage(MimeMessage message) throws MessagingException, IOException {

    String textMessage = getMailText(message);
    if (textMessage != null) {
      Matcher m = textMessageExtractionPattern.matcher(textMessage);
      if (m.find()) {
        return m.group().trim();
      }
    }
    return null;
  }

  public BasicNeedType getBasicNeedType(MimeMessage message) throws MessagingException {
    return getBasicNeedType(message.getSubject());
  }

  public BasicNeedType getBasicNeedType(String subject) {

    if (demandTypePattern.matcher(subject).matches()) {
      return BasicNeedType.DEMAND;
    } else if (supplyTypePattern.matcher(subject).matches()) {
      return BasicNeedType.SUPPLY;
    } else if (doTogetherTypePattern.matcher(subject).matches()) {
      return BasicNeedType.DO_TOGETHER;
    } else if (critiqueTypePattern.matcher(subject).matches()) {
      return BasicNeedType.CRITIQUE;
    }

    return null;
  }

  public String[] getTags(MimeMessage message) throws MessagingException, IOException {

    HashSet<String> tags = new HashSet<>();
    Matcher m = tagExtractionPattern.matcher(new StringBuilder(message.getSubject()).append(" ").append(message.getContent()).toString());

    while(m.find()) {
      tags.add(m.group());
    }

    String[] tagArray = new String[tags.size()];
    tagArray = tags.toArray(tagArray);
    Arrays.sort(tagArray);

    return tagArray;
  }

  public static String getFromAddressString(MimeMessage message) throws MessagingException{

    Address[] froms = message.getFrom();
    return (froms == null) ? null : ((InternetAddress) froms[0]).getAddress();
  }
}
