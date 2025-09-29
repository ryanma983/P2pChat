package com.group7.chat;

/**
 * 地址解析测试程序
 */
public class AddressParsingTest {
    
    public static void main(String[] args) {
        System.out.println("=== 地址解析测试 ===");
        
        // 测试各种地址格式
        String[] testAddresses = {
            "/127.0.0.1:8080",
            "127.0.0.1:8080", 
            "localhost:8080",
            "localhost/127.0.0.1:8080",
            "/localhost/127.0.0.1:8080",
            "//127.0.0.1:8080"
        };
        
        for (String address : testAddresses) {
            String normalized = normalizeAddress(address);
            System.out.println("原始: " + address + " -> 标准化: " + normalized);
            
            String[] parts = normalized.split(":");
            if (parts.length == 2) {
                try {
                    String host = parts[0];
                    int port = Integer.parseInt(parts[1]);
                    int fileTransferPort = port + 1000;
                    System.out.println("  解析结果: 主机=" + host + ", 端口=" + port + ", 文件传输端口=" + fileTransferPort);
                } catch (NumberFormatException e) {
                    System.out.println("  解析失败: 无效端口号");
                }
            } else {
                System.out.println("  解析失败: 地址格式错误");
            }
            System.out.println();
        }
    }
    
    private static String normalizeAddress(String targetAddress) {
        // 解析地址和端口
        String normalizedAddress = targetAddress.replace("localhost", "127.0.0.1");
        
        // 移除所有前缀斜杠，处理如 "/127.0.0.1:8080" 或 "localhost/127.0.0.1:9081" 的格式
        while (normalizedAddress.startsWith("/")) {
            normalizedAddress = normalizedAddress.substring(1);
        }
        
        // 处理可能的复杂地址格式，如 "localhost/127.0.0.1:9081"
        if (normalizedAddress.contains("/")) {
            // 取斜杠后面的部分
            normalizedAddress = normalizedAddress.substring(normalizedAddress.lastIndexOf("/") + 1);
        }
        
        return normalizedAddress;
    }
}
