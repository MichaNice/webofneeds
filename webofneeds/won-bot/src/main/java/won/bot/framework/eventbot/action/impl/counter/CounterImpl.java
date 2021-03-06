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

package won.bot.framework.eventbot.action.impl.counter;

/**
 * Counter that is intended to be shared between actions and listeners.
 */
public class CounterImpl implements Counter
{
  private int count;
  private String name;

  public CounterImpl(String name, final int initialCount) {
    this.count = initialCount;
    this.name = name;
  }

  public CounterImpl(String name) {
    this(name, 0);
  }

  @Override
  public synchronized int getCount(){
    return count;
  }

  @Override
  public synchronized int increment(){
    return ++count;
  }

  @Override
  public synchronized  int decrement(){
    return --count;
  }

  public String getName() {
    return name;
  }
}
