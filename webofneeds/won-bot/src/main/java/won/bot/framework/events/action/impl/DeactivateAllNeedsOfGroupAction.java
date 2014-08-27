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

package won.bot.framework.events.action.impl;

import won.bot.framework.events.event.Event;
import won.bot.framework.events.action.BaseEventBotAction;
import won.bot.framework.events.event.impl.NeedDeactivatedEvent;
import won.bot.framework.events.EventListenerContext;

import java.net.URI;
import java.util.List;

/**
* User: fkleedorfer
* Date: 28.03.14
*/
public class DeactivateAllNeedsOfGroupAction extends BaseEventBotAction
{
  private String groupName;
  public DeactivateAllNeedsOfGroupAction(EventListenerContext eventListenerContext, String groupName) {
    super(eventListenerContext);
    this.groupName = groupName;
  }

  @Override
  protected void doRun(Event event) throws Exception {
    List<URI> toDeactivate = getEventListenerContext().getBotContext().getNamedNeedUriList(groupName);
    for (URI uri: toDeactivate){
      getEventListenerContext().getOwnerService().deactivate(uri, null);
      getEventListenerContext().getEventBus().publish(new NeedDeactivatedEvent(uri));
    }
  }
}