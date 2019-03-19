package vip.r0n9.ws;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

public class WebSocketConfigrator extends ServerEndpointConfig.Configurator {

    @Override
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
        HttpSession httpSession = (HttpSession) request.getHttpSession();

        if (httpSession == null) {
            return;
        }
        //把HttpSession中保存的ClientIP放到ServerEndpointConfig中，关键字可以跟之前不同
        config.getUserProperties().put(HttpSession.class.getName(),httpSession);
    }
}
