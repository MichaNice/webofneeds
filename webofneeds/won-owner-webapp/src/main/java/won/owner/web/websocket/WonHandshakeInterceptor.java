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

package won.owner.web.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: LEIH-NB
 * Date: 09.10.2014
 */
public class WonHandshakeInterceptor  extends HttpSessionHandshakeInterceptor
{
  // TODO why do we need this if there is a session cookie?
  public static final String SESSION_ATTR = "httpSession.id";
  // TODO  probably we don't need it any more, the user in WonWebSocketHandler
  // is obtained directly from session.getPrincipal().getName();
  public static final String USERNAME_ATTR = "username";

  private static final List<String> ATTRIBUTE_NAMES = new ArrayList<>(2);
  static {
    ATTRIBUTE_NAMES.add(SESSION_ATTR);
    ATTRIBUTE_NAMES.add(USERNAME_ATTR);
  }

  public WonHandshakeInterceptor() {
    super(ATTRIBUTE_NAMES);
  }

  @Override
  public boolean beforeHandshake(ServerHttpRequest request,
                                 ServerHttpResponse response, WebSocketHandler wsHandler,
                                 Map<String, Object> attributes) throws Exception{

    addSessionIdAttribute(request, attributes);
    attributes.put(USERNAME_ATTR, SecurityContextHolder.getContext().getAuthentication().getName());
    return super.beforeHandshake(request, response, wsHandler, attributes);
  }

  /* added for integrating spring-session, which we added to synchronize
    http sessions with websocket sessions.
    see: http://spring.io/blog/2014/09/16/preview-spring-security-websocket-support-sessions
    */
  private void addSessionIdAttribute(final ServerHttpRequest request, final Map<String, Object> attributes) {
    if (request instanceof ServletServerHttpRequest) {
      ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
      HttpSession session = servletRequest.getServletRequest().getSession(false);
      if (session != null) {
        attributes.put(SESSION_ATTR, session.getId());
      }
    }
  }

  @Override
  public void afterHandshake(ServerHttpRequest request,
                             ServerHttpResponse response, WebSocketHandler wsHandler,
                             Exception ex){
    super.afterHandshake(request, response,wsHandler, ex);
  }
}

