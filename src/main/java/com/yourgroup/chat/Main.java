package com.yourgroup.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main(String[] args) {
        // 默认端口号，可以从命令行参数读取
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("端口号参数必须是整数。使用默认端口 " + port);
            }
        }

        System.out.println("聊天节点启动，正在监听端口: " + port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                // 1. 等待并接受新的客户端连接
                Socket clientSocket = serverSocket.accept();
                System.out.println("接收到新的连接，来自: " + clientSocket.getRemoteSocketAddress());

                // 2. 为每个客户端创建一个新的线程来处理通信
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("无法在端口 " + port + " 上启动服务器。");
            e.printStackTrace();
        }
    }

    /**
     * 客户端处理器，用于在单独的线程中处理每个客户端的连接
     */
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
            ) {
                String inputLine;
                // 3. 持续读取客户端发送的消息
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("收到来自 [" + clientSocket.getRemoteSocketAddress() + "] 的消息: " + inputLine);
                    // 在这里，未来我们将把收到的消息广播给所有其他连接的节点
                }
            } catch (IOException e) {
                System.out.println("与客户端 " + clientSocket.getRemoteSocketAddress() + " 的连接发生错误。连接已断开。");
            } finally {
                try {
                    clientSocket.close();
                    System.out.println("关闭连接: " + clientSocket.getRemoteSocketAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
