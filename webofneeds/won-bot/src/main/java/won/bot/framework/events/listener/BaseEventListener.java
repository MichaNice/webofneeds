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

package won.bot.framework.events.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import won.bot.framework.events.Event;
import won.bot.framework.events.EventListener;
import won.bot.framework.events.event.FinishedEvent;

/**
 * Base class for event listeners
 */
public abstract class BaseEventListener implements EventListener
{
  protected final Logger logger = LoggerFactory.getLogger(getClass());
  private EventListenerContext context;
  private int eventCount = 0;
  private int exceptionCount = 0;
  private long millisExecuting = 0;
  private EventFilter eventFilter = null;

  /**
   * Constructor is private so that subclasses must implement the one-arg constructor.
   */
  private BaseEventListener(){}

  protected BaseEventListener(final EventListenerContext context)
  {
    this.context = context;
  }

  protected BaseEventListener(final EventListenerContext context, final EventFilter eventFilter)
  {
    this(context);
    this.eventFilter = eventFilter;
  }

  @Override
  public final void onEvent(final Event event) throws Exception{
    if (!shouldHandleEvent(event)){
      //allow for ignoring events. Such event are not counted.
      return;
    }
    countEvent(event);
    long startTime = System.currentTimeMillis();
    try {
      doOnEvent(event);
    } catch (Exception e) {
      countException(e);
      throw e;
    } finally {
      noteTimeExecuting(startTime);
    }
  }

  /**
   * Publishes an event indicating that the listener is finished. Useful for chaining listeners.
   * Only use when this is really the case.
   */
  protected void publishFinishedEvent()
  {
    getEventListenerContext().getEventBus().publish(new FinishedEvent(this));
  }

  public long getMillisExecuting()
  {
    return millisExecuting;
  }

  public int getExceptionCount()
  {
    return exceptionCount;
  }

  public int getEventCount()
  {
    return eventCount;
  }

  protected synchronized void countException(final Exception e){
    this.exceptionCount ++;
   }

  private synchronized void noteTimeExecuting(final long startTime)
  {
    this.millisExecuting += System.currentTimeMillis() - startTime;
  }

  private synchronized void countEvent(final Event event)
  {
    this.eventCount ++;
  }

  protected abstract void doOnEvent(Event event) throws Exception;

  protected EventListenerContext getEventListenerContext(){
    return context;
  }

  /**
   * Determines whether the given event should be processed or ignored. If it is ignored,
   * it is not counted and does not influence the listener's behavior. The default implementation
   * accepts all events.
   * @param event
   * @return
   */
  protected final boolean shouldHandleEvent(final Event event){
    return eventFilter == null ? true : eventFilter.accept(event);
  }
}