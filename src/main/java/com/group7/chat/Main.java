package com.group7.chat;

import java.util.Scanner;

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

        // 创建并启动节点
        Node node = new Node(port);
        
        // 如果提供了其他节点的地址，添加为已知节点
        for (int i = 1; i < args.length; i++) {
            node.addKnownPeer(args[i]);
        }
        
        node.start();
        
        // 添加关闭钩子，确保程序退出时正确关闭节点
        Runtime.getRuntime().addShutdownHook(new Thread(node::stop));
        
        // 简单的命令行界面
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n=== P2P 聊天节点已启动 ===");
        System.out.println("节点ID: " + node.getNodeId());
        System.out.println("监听端口: " + node.getPort());
        System.out.println("\n可用命令:");
        System.out.println("  connect <host:port> - 连接到指定节点");
        System.out.println("  send <message>      - 发送消息到所有连接的节点");
        System.out.println("  status              - 显示当前状态");
        System.out.println("  quit                - 退出程序");
        System.out.println("\n请输入命令:");
        
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                continue;
            }
            
            String[] parts = input.split(" ", 2);
            String command = parts[0].toLowerCase();
            
            switch (command) {
                case "connect":
                    if (parts.length < 2) {
                        System.out.println("用法: connect <host:port>");
                    } else {
                        String address = parts[1];
                        if (node.connectToPeer(address)) {
                            System.out.println("连接成功!");
                        } else {
                            System.out.println("连接失败!");
                        }
                    }
                    break;
                    
                case "send":
                    if (parts.length < 2) {
                        System.out.println("用法: send <message>");
                    } else {
                        String message = parts[1];
                        node.sendChatMessage(message);
                    }
                    break;
                    
                case "status":
                    System.out.println("节点状态:");
                    System.out.println("  节点ID: " + node.getNodeId());
                    System.out.println("  监听端口: " + node.getPort());
                    System.out.println("  连接数: " + node.getConnectionCount());
                    break;
                    
                case "quit":
                case "exit":
                    System.out.println("正在关闭节点...");
                    node.stop();
                    scanner.close();
                    System.exit(0);
                    break;
                    
                default:
                    System.out.println("未知命令: " + command);
                    System.out.println("输入 'quit' 退出程序");
                    break;
            }
        }
    }
}
