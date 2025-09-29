package com.group7.chat;

import java.io.File;
import java.io.IOException;

/**
 * 文件传输测试程序
 */
public class FileTransferTest {
    
    public static void main(String[] args) {
        System.out.println("======================================");
        System.out.println("P2P 文件传输功能测试");
        System.out.println("======================================");
        
        try {
            // 创建两个节点
            System.out.println("创建测试节点...");
            
            Node node1 = new Node(8080);
            Node node2 = new Node(8081);
            
            System.out.println("节点1 ID: " + node1.getNodeId().toString().substring(0, 8) + "...");
            System.out.println("节点2 ID: " + node2.getNodeId().toString().substring(0, 8) + "...");
            
            // 启动节点
            System.out.println("启动节点...");
            node1.start();
            node2.start();
            
            // 等待节点启动
            Thread.sleep(2000);
            
            // 连接节点
            System.out.println("连接节点...");
            node2.connectToPeer("127.0.0.1:8080");
            
            // 等待连接建立
            Thread.sleep(1000);
            
            // 检查连接状态
            System.out.println("节点1连接数: " + node1.getConnections().size());
            System.out.println("节点2连接数: " + node2.getConnections().size());
            
            if (node1.getConnections().size() > 0 && node2.getConnections().size() > 0) {
                System.out.println("连接建立成功！");
                
                // 创建测试文件
                File testFile = new File("/home/ubuntu/P2pChat/test-file.txt");
                if (testFile.exists()) {
                    System.out.println("测试文件: " + testFile.getName() + " (" + testFile.length() + " bytes)");
                    
                    // 测试文件传输
                    System.out.println("开始文件传输测试...");
                    node2.getFileTransferService().sendFile("broadcast", testFile, "received-" + testFile.getName());
                    
                    // 等待传输完成
                    Thread.sleep(3000);
                    
                    System.out.println("文件传输测试完成");
                } else {
                    System.out.println("测试文件不存在: " + testFile.getAbsolutePath());
                }
            } else {
                System.out.println("节点连接失败");
            }
            
            // 停止节点
            System.out.println("停止节点...");
            node1.stop();
            node2.stop();
            
            System.out.println("测试完成");
            
        } catch (Exception e) {
            System.err.println("测试过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
