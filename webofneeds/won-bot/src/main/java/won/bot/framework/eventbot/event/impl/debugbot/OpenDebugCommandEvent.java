package won.bot.framework.eventbot.event.impl.debugbot;

import won.protocol.model.Connection;

/**
 * User: ypanchenko
 * Date: 26.02.2016
 */
public class OpenDebugCommandEvent extends DebugCommandEvent
{

  public OpenDebugCommandEvent(final Connection con) {
    super(con);
  }
}
