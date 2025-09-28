package com.yourgroup.chat.test;

import com.yourgroup.chat.util.EmojiSupport;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * 表情支持测试应用
 */
public class EmojiTestApp extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("表情字体支持测试");
        
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_LEFT);
        
        // 标题
        Label titleLabel = new Label("P2P Chat 表情字体支持测试");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        // 系统信息
        TextArea systemInfo = new TextArea();
        systemInfo.setText(EmojiSupport.getEmojiSupportInfo());
        systemInfo.setPrefRowCount(10);
        systemInfo.setEditable(false);
        
        // 表情测试区域
        Label testLabel = new Label("表情显示测试:");
        testLabel.setStyle("-fx-font-weight: bold;");
        
        // 创建表情测试网格
        GridPane emojiGrid = createEmojiTestGrid();
        
        // 字体安装指南
        Label guideLabel = new Label("字体安装指南:");
        guideLabel.setStyle("-fx-font-weight: bold;");
        
        TextArea guideArea = new TextArea();
        guideArea.setText(EmojiSupport.getFontInstallationGuide());
        guideArea.setPrefRowCount(8);
        guideArea.setEditable(false);
        
        // 刷新按钮
        Button refreshButton = new Button("刷新检测");
        refreshButton.setOnAction(e -> {
            systemInfo.setText(EmojiSupport.getEmojiSupportInfo());
            refreshEmojiGrid(emojiGrid);
        });
        
        // 添加所有组件
        root.getChildren().addAll(
            titleLabel,
            new Label("系统信息:"),
            systemInfo,
            testLabel,
            emojiGrid,
            guideLabel,
            guideArea,
            refreshButton
        );
        
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        
        Scene scene = new Scene(scrollPane, 800, 700);
        
        // 加载表情字体CSS
        try {
            scene.getStylesheets().add(getClass().getResource("/css/dynamic-emoji-fonts.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("无法加载表情字体CSS: " + e.getMessage());
        }
        
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // 打印检测信息到控制台
        EmojiSupport.printEmojiSupportInfo();
    }
    
    private GridPane createEmojiTestGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        
        // 测试表情数组
        String[][] testEmojis = {
            {"😀", "开心", ":D"},
            {"😃", "大笑", ":)"},
            {"😄", "哈哈", "^_^"},
            {"😍", "爱心眼", "<3"},
            {"😘", "飞吻", ":*"},
            {"👍", "点赞", "+1"},
            {"👎", "点踩", "-1"},
            {"❤️", "红心", "<3"},
            {"🎉", "庆祝", "party"},
            {"🔥", "火", "fire"},
            {"⭐", "星星", "*"},
            {"💯", "百分百", "100"}
        };
        
        // 添加表头
        grid.add(new Label("Unicode表情"), 0, 0);
        grid.add(new Label("名称"), 1, 0);
        grid.add(new Label("文字替代"), 2, 0);
        grid.add(new Label("检测结果"), 3, 0);
        
        // 添加测试表情
        for (int i = 0; i < testEmojis.length; i++) {
            String emoji = testEmojis[i][0];
            String name = testEmojis[i][1];
            String fallback = testEmojis[i][2];
            
            // Unicode表情标签
            Label emojiLabel = new Label(emoji);
            emojiLabel.setStyle("-fx-font-size: 24px; " + EmojiSupport.getEmojiFontCSS());
            
            // 名称标签
            Label nameLabel = new Label(name);
            
            // 文字替代标签
            Label fallbackLabel = new Label(fallback);
            fallbackLabel.setStyle("-fx-font-family: monospace; -fx-font-weight: bold;");
            
            // 检测结果
            boolean supported = EmojiSupport.testEmojiSupport(emoji);
            Label resultLabel = new Label(supported ? "✓ 支持" : "✗ 不支持");
            resultLabel.setStyle(supported ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
            
            grid.add(emojiLabel, 0, i + 1);
            grid.add(nameLabel, 1, i + 1);
            grid.add(fallbackLabel, 2, i + 1);
            grid.add(resultLabel, 3, i + 1);
        }
        
        return grid;
    }
    
    private void refreshEmojiGrid(GridPane grid) {
        // 重新检测表情字体
        EmojiSupport.detectEmojiFont();
        
        // 更新表情标签的字体
        grid.getChildren().forEach(node -> {
            if (node instanceof Label) {
                Label label = (Label) node;
                String text = label.getText();
                if (text.length() <= 4 && !text.matches("[a-zA-Z\\s]+")) {
                    // 这可能是表情，更新字体
                    label.setStyle("-fx-font-size: 24px; " + EmojiSupport.getEmojiFontCSS());
                }
            }
        });
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
