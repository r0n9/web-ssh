package vip.r0n9;

import com.jcraft.jsch.*;

import java.io.IOException;

public class Test {
    static  Session session;

    public static void main(String[] args) throws JSchException, IOException {
        JSch jsch = new JSch();

        session = jsch.getSession("rong", "192.168.1.94", 22);
        session.setPassword("123456");

        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);


        session.connect();

        String res = sendCommand("pwd");
        System.out.println(res);


    }

    public static String sendCommand(String command) {
        StringBuilder outputBuffer = new StringBuilder();
        Channel channel = null;
        try {
            channel = session.openChannel("shell");
            channel.setInputStream(System.in, true);
            channel.setOutputStream(System.out, true);
            channel.connect();
//            int readByte = commandOutput.read();
//
//            while (readByte != 0xffffffff) {
//                outputBuffer.append((char) readByte);
//                readByte = commandOutput.read();
//            }
//
//            channel.disconnect();
//        } catch (IOException ioX) {
//            return null;
        } catch (JSchException jschX) {
            return null;
        }

        return outputBuffer.toString();
    }

}
