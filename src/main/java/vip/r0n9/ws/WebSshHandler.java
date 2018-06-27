package vip.r0n9.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import org.springframework.stereotype.Component;
import vip.r0n9.JsonUtil;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint(value = "/ssh/{id}", configurator = WebSocketConfigrator.class)
@Component
public class WebSshHandler {


    private static int onlineCount = 0;

    private static CopyOnWriteArraySet<WebSshHandler> webSocketSet = new CopyOnWriteArraySet<>();

    private Session session;

    private StringBuilder dataToDst = new StringBuilder();


    private static JSch jsch = new JSch();

    private com.jcraft.jsch.Session jschSession;

    private Channel channel;

    private InputStream inputStream;

    private OutputStream outputStream;

    private Thread thread;

    @OnOpen
    public void onOpen(final Session session, @PathParam("id") String id) throws JSchException, IOException, EncodeException, InterruptedException {
        this.session = session;
        webSocketSet.add(this);
        addOnlineCount();
        System.out.println("有新链接 " + session.getUserProperties().get("ClientIP") + " 加入!当前在线人数为" + getOnlineCount());

        jschSession = jsch.getSession("rong", "192.168.1.94", 22);
        jschSession.setPassword("123456");
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        jschSession.setConfig(config);
        jschSession.connect();

        channel = jschSession.openChannel("shell");
        inputStream = channel.getInputStream();
        outputStream = channel.getOutputStream();
        channel.connect();


        thread = new Thread() {

            @Override
            public void run() {

                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String msg = null;
                    while ((msg = bufferedReader.readLine()) != null) { // 这里会阻塞，所以必须起线程来读取channel返回内容
                        System.out.println("-- " + msg);
                        byte[] bytes = ("\r\n" + msg).getBytes();
                        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, 0, bytes.length);
                        session.getBasicRemote().sendBinary(byteBuffer);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();

        Thread.sleep(100);
        this.onMessage("{\"data\":\"\\r\"}", this.session);

    }

    @OnClose
    public void onClose() {
        webSocketSet.remove(this);
        subOnlineCount();
        System.out.println("有一链接关闭! 当前在线人数为" + getOnlineCount());

        channel.disconnect();
        jschSession.disconnect();
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException, JSchException {


        System.out.println("来自客户端 " + session.getUserProperties().get("ClientIP") + " 的消息:" + message);

        JsonNode node = JsonUtil.strToJsonObject(message);

        if (node.has("resize")) {
            // do nothing
            return;
        }

        if (node.has("data")) {
            String str = node.get("data").asText();

            if ("\r".equals(str)) {
                if (dataToDst.length() > 0) {
                    str = "\r\n";
                    dataToDst = new StringBuilder();
                }
            } else {
                dataToDst.append(str);
            }

            byte[] bytes = str.getBytes();
            outputStream.write(bytes);
            outputStream.flush();

            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, 0, bytes.length);
            session.getBasicRemote().sendBinary(byteBuffer);

            System.out.println("dataToDst = " + dataToDst);

            return;
        }


    }


    public static synchronized int getOnlineCount() {
        return WebSshHandler.onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSshHandler.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSshHandler.onlineCount--;
    }
}
