package won.bot.framework.eventbot.listener.baStateBots.baPCMessagingBots.atomicBots.coordinationMessageAsTextBot;

import won.bot.framework.eventbot.listener.baStateBots.BATestBotScript;
import won.bot.framework.eventbot.listener.baStateBots.BATestScriptAction;
import won.bot.framework.eventbot.listener.baStateBots.NopAction;
import won.node.facet.impl.WON_TX;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Danijel
 * Date: 17.4.14.
 */
public class ActiveSPCancelingFailingBot extends BATestBotScript
{

  @Override
  protected List<BATestScriptAction> setupActions() {
    List<BATestScriptAction> actions = new ArrayList();

    //automatic
    //actions.add(new BATestScriptAction(false, "MESSAGE_CANCEL", URI.create(WON_BA.STATE_ACTIVE.getURI()), 3));

    actions.add(new NopAction());
    actions.add(new BATestScriptAction(true, "MESSAGE_FAIL", URI.create(WON_TX.STATE_CANCELING.getURI())));
    actions.add(new BATestScriptAction(false, "MESSAGE_FAILED", URI.create(WON_TX.STATE_FAILING_ACTIVE_CANCELING.getURI())));

    return actions;
  }
}