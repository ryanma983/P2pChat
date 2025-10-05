package com.group7.chat.gui;

import com.group7.chat.Node;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * chat interface controller++, supporting group chat, private chat, file
 * transfer and member list
 */
public class EnhancedChatController implements Initializable, com.group7.chat.MessageListener {

    // Left member panel
    @FXML
    private ListView<OnlineMember> memberListView;
    @FXML
    private Label memberCountLabel;

    // Right chat panel
    @FXML
    private Label chatTitleLabel;
    @FXML
    private Label nodeIdLabel;
    @FXML
    private Label connectionCountLabel;
    @FXML
    private ListView<ChatMessage> messageListView;
    @FXML
    private TextArea messageInput;
    @FXML
    private Button sendButton;
    @FXML
    private Button fileButton;
    @FXML
    private Button emojiButton;

    @FXML
    private Button connectButton;

    // Toolbar
    @FXML
    private HBox toolbar;

    // Menu
    @FXML
    private MenuItem aboutMenuItem;
    @FXML
    private MenuItem settingsMenuItem;

    private Node chatNode;
    private ObservableList<ChatMessage> messages;
    private ObservableList<OnlineMember> onlineMembers;
    private Timer statusUpdateTimer;
    private Map<String, PrivateChatWindow> privateChatWindows = new HashMap<>();
    private Stage emojiStage;

    // emojis collection
    private final String[] COMMON_EMOJIS = {
            "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇",
            "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚",
            "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🤩",
            "🥳", "😏", "😒", "😞", "😔", "😟", "😕", "🙁", "☹️", "😣",
            "😖", "😫", "😩", "🥺", "😢", "😭", "😤", "😠", "😡", "🤬",
            "🤯", "😳", "🥵", "🥶", "😱", "😨", "😰", "😥", "😓", "🤗",
            "🤔", "🤭", "🤫", "🤥", "😶", "😐", "😑", "😬", "🙄", "😯",
            "😦", "😧", "😮", "😲", "🥱", "😴", "🤤", "😪", "😵", "🤐",
            "🥴", "🤢", "🤮", "🤧", "😷", "🤒", "🤕", "🤑", "🤠", "😈",
            "👋", "🤚", "🖐️", "✋", "🖖", "👌", "🤏", "✌️", "🤞", "🤟",
            "🤘", "🤙", "👈", "👉", "👆", "🖕", "👇", "☝️", "👍", "👎",
            "✊", "👊", "🤛", "🤜", "👏", "🙌", "👐", "🤲", "🤝", "🙏",
            "💪", "🦾", "🦿", "🦵", "🦶", "👂", "🦻", "👃", "🧠", "🦷",
            "🦴", "👀", "👁️", "👅", "👄", "💋", "🩸", "❤️", "🧡", "💛",
            "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔", "❣️", "💕", "💞",
            "💓", "💗", "💖", "💘", "💝", "💟", "☮️", "✝️", "☪️", "🕉",
            "☸️", "✡️", "🔯", "🕎", "☯️", "☦️", "🛐", "⛎", "♈", "♉",
            "♊", "♋", "♌", "♍", "♎", "♏", "♐", "♑", "♒", "♓",
            "🆔", "⚛️", "🉑", "☢️", "☣️", "📴", "📳", "🈶", "🈚", "🈸",
            "🈺", "🈷️", "✴️", "🆚", "💮", "🉐", "㊙️", "㊗️", "🈴", "🈵",
            "🈹", "🈲", "🅰️", "🅱️", "🆎", "🆑", "🅾️", "🆘", "❌", "⭕",
            "🛑", "⛔", "📛", "🚫", "💯", "💢", "♨️", "🚷", "🚯", "🚳",
            "🚱", "🔞", "📵", "🚭", "❗", "❕", "❓", "❔", "‼️", "⁉️",
            "🔅", "🔆", "〽️", "⚠️", "🚸", "🔱", "⚜️", "🔰", "♻️", "✅",
            "🈯", "💹", "❇️", "✳️", "❎", "🌐", "💠", "Ⓜ️", "🌀", "💤",
            "🏧", "🚾", "♿", "🅿️", "🈳", "🈂️", "🛂", "🛃", "🛄", "🛅",
            "🚹", "🚺", "🚼", "🚻", "🚮", "🎦", "📶", "🈁", "🔣", "ℹ️",
            "🔤", "🔡", "🔠", "🆖", "🆗", "🆙", "🆒", "🆕", "🆓", "0️⃣",
            "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣", "🔟",
            "🔢", "#️⃣", "*️⃣", "⏏️", "▶️", "⏸️", "⏯️", "⏹️", "⏺️", "⏭️",
            "⏮️", "⏩", "⏪", "⏫", "⏬", "◀️", "🔼", "🔽", "➡️", "⬅️",
            "⬆️", "⬇️", "↗️", "↘️", "↙️", "↖️", "↕️", "↔️", "↪️", "↩️",
            "⤴️", "⤵️", "🔀", "🔁", "🔂", "🔄", "🔃", "🎵", "🎶", "➕",
            "➖", "➗", "✖️", "♾️", "💲", "💱", "™️", "©️", "®️", "〰️",
            "➰", "➿", "🔚", "🔙", "🔛", "🔝", "🔜", "✔️", "☑️", "🔘",
            "🔴", "🟠", "🟡", "🟢", "🔵", "🟣", "⚫", "⚪", "🟤", "🔺",
            "🔻", "🔸", "🔹", "🔶", "🔷", "🔳", "🔲", "▪️", "▫️", "◾",
            "◽", "◼️", "◻️", "🟥", "🟧", "🟨", "🟩", "🟦", "🟪", "⬛",
            "⬜", "🟫", "🔈", "🔇", "🔉", "🔊", "🔔", "🔕", "📣", "📢",
            "👁️‍🗨️", "💬", "💭", "🗯️", "♠️", "♣️", "♥️", "♦️", "🃏", "🎴",
            "🀄", "🕐", "🕑", "🕒", "🕓", "🕔", "🕕", "🕖", "🕗", "🕘",
            "🕙", "🕚", "🕛", "🕜", "🕝", "🕞", "🕟", "🕠", "🕡", "🕢",
            "🕣", "🕤", "🕥", "🕦", "🕧"
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // init the data
        messages = FXCollections.observableArrayList();
        onlineMembers = FXCollections.observableArrayList();

        // setting up list view
        messageListView.setItems(messages);
        messageListView.setCellFactory(listView -> new MessageListCell());
        memberListView.setItems(onlineMembers);

        // default the group mode

        // setting the message input box
        messageInput.setWrapText(true);
        messageInput.setPrefRowCount(2);

        // binding the send button state
        sendButton.disableProperty().bind(messageInput.textProperty().isEmpty());

        // setting keyboard hot keys
        messageInput.setOnKeyPressed(this::handleKeyPressed);

        // setting the member list & click event
        memberListView.setOnMouseClicked(this::handleMemberClick);

        // setting the menu
        aboutMenuItem.setOnAction(e -> showAboutDialog());
        settingsMenuItem.setOnAction(e -> showSettingsDialog());

        // setting emoji button clickon event
        emojiButton.setOnAction(e -> showEmojiPopup());

        // adding the welcome message
        addSystemMessage("Welcome to the P2P chat app! Supports group chat, private chat and file transfer.");
    }

    /**
     * Display the emoticon selection popup - using Stage
     */
    private void showEmojiPopup() {
        // Hide if already displayed
        if (emojiStage != null && emojiStage.isShowing()) {
            emojiStage.hide();
            return;
        }

        // init emoji Stage
        initializeEmojiStage();

        // Get the position of the emoticon button and toolbar on the screen
        Bounds buttonBounds = emojiButton.localToScreen(emojiButton.getBoundsInLocal());
        Bounds toolbarBounds = toolbar.localToScreen(toolbar.getBoundsInLocal());

        // Calculate popup position - adjust X coordinate to accommodate wider popup
        double popupX = buttonBounds.getMinX() - 30;
        double popupY = toolbarBounds.getMinY() - 240;

        // Set the stage position and display
        emojiStage.setX(popupX);
        emojiStage.setY(popupY);
        emojiStage.show();
    }

    private void initializeEmojiStage() {
        if (emojiStage != null) {
            emojiStage.close();
        }

        emojiStage = new Stage();
        emojiStage.initStyle(StageStyle.UNDECORATED);
        emojiStage.initModality(Modality.NONE);
        emojiStage.setResizable(false);

        emojiStage.setWidth(380);
        emojiStage.setHeight(240);

        // Create the root container - use AnchorPane to ensure content fills
        AnchorPane root = new AnchorPane();
        root.setStyle("-fx-background-color: #D7D7D7;");

        // Create the content panel - fill the entire root container
        VBox contentPanel = new VBox();
        contentPanel.getStyleClass().add("emoji-popup-panel");
        contentPanel.setPrefSize(380, 240);
        contentPanel.setMinSize(380, 240);
        contentPanel.setMaxSize(380, 240);

        // Set AnchorPane constraints so that the content panel fills the root container
        AnchorPane.setTopAnchor(contentPanel, 0.0);
        AnchorPane.setBottomAnchor(contentPanel, 0.0);
        AnchorPane.setLeftAnchor(contentPanel, 0.0);
        AnchorPane.setRightAnchor(contentPanel, 0.0);

        // Create scroll panel - fill content panel
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("emoji-scroll-pane");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Set the scroll panel to fill the content panel
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        scrollPane.setMaxWidth(Double.MAX_VALUE);
        scrollPane.setMaxHeight(Double.MAX_VALUE);

        // Create an emoji grid container
        VBox contentContainer = new VBox();
        contentContainer.setPadding(new Insets(0)); // Remove padding

        // Create an emoji grid
        FlowPane emojiGrid = new FlowPane();
        emojiGrid.getStyleClass().add("emoji-grid-pane");
        emojiGrid.setPrefWrapLength(362); // 380 - 8*2 - 12 = 362 (Total width - left and right padding - scrollbar
                                          // width)

        emojiGrid.setMaxWidth(Double.MAX_VALUE);
        emojiGrid.setMaxHeight(Double.MAX_VALUE);

        // Limit the number of emojis
        int emojiCount = Math.min(COMMON_EMOJIS.length, 120);
        for (int i = 0; i < emojiCount; i++) {
            String emoji = COMMON_EMOJIS[i];
            Button emojiBtn = createEmojiButton(emoji);
            emojiGrid.getChildren().add(emojiBtn);
        }

        contentContainer.getChildren().add(emojiGrid);
        scrollPane.setContent(contentContainer);
        contentPanel.getChildren().add(scrollPane);
        root.getChildren().add(contentPanel);

        // set the scene
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        try {
            Scene mainScene = emojiButton.getScene();
            if (mainScene != null) {
                scene.getStylesheets().addAll(mainScene.getStylesheets());
            }
        } catch (Exception e) {
            System.err.println("Failed to load CSS: " + e.getMessage());
        }

        emojiStage.setScene(scene);

        // Click outside to close
        emojiStage.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                emojiStage.hide();
            }
        });
    }

    private Button createEmojiButton(String emoji) {
        Button emojiBtn = new Button(emoji);
        emojiBtn.getStyleClass().add("emoji-popup-button");
        // emoji setting
        emojiBtn.setPrefSize(38, 38);
        emojiBtn.setMinSize(38, 38);
        emojiBtn.setMaxSize(38, 38);

        emojiBtn.setOnAction(e -> {
            insertEmoji(emoji);
            emojiStage.hide();
        });

        return emojiBtn;
    }

    /**
     * input the selected emoji
     */
    private void insertEmoji(String emoji) {
        String currentText = messageInput.getText();
        int caretPosition = messageInput.getCaretPosition();

        String newText = currentText.substring(0, caretPosition) +
                emoji +
                currentText.substring(caretPosition);

        messageInput.setText(newText);
        messageInput.positionCaret(caretPosition + emoji.length());
        messageInput.requestFocus();
    }

    /**
     * setNode
     */
    public void setNode(Node node) {
        this.chatNode = node;

        // Update interface information
        Platform.runLater(() -> {
            nodeIdLabel.setText("Node ID: " + node.getDisplayName());
            updateConnectionCount();
            updateMemberCount();
        });

        // Start the status update timer
        startStatusUpdateTimer();

        // Set up message listener
        node.setMessageListener(this);

        addSystemMessage("Linked successfully, port: " + node.getPort());
    }

    /**
     * Handle member list click events
     */
    private void handleMemberClick(MouseEvent event) {
        OnlineMember clickedMember = memberListView.getSelectionModel().getSelectedItem();
        if (clickedMember != null && !clickedMember.getNodeId().equals(chatNode.getNodeId())) {
            // Double click to open the p2p chat window
            if (event.getClickCount() == 2) {
                openPrivateChatWindow(clickedMember);
            }
        }
    }

    /**
     * open the p2p chat window
     */
    private void openPrivateChatWindow(OnlineMember member) {
        String nodeId = member.getNodeId();

        // Check if there is already a p2p chat window for this member
        PrivateChatWindow existingWindow = privateChatWindows.get(nodeId);
        if (existingWindow != null && existingWindow.isShowing()) {
            // If the window already exists, display it directly
            existingWindow.show();
            return;
        }

        // Create a new p2p chat window
        PrivateChatWindow privateChatWindow = new PrivateChatWindow(chatNode, member);
        privateChatWindows.put(nodeId, privateChatWindow);

        // display the window
        privateChatWindow.show();

        addSystemMessage("Linked the chat with " + member.getDisplayName());
    }

    // The updateChatMode method has been removed, the default mode is group chat

    /**
     * Handle the Send button click
     */
    @FXML
    private void handleSendMessage() {
        String messageText = messageInput.getText().trim();
        if (!messageText.isEmpty() && chatNode != null) {
            // Send group chat message
            addSentMessage(messageText, ChatMessage.MessageType.SENT);
            chatNode.sendChatMessage(messageText);

            // Clear the input box
            messageInput.clear();
        }
    }

    /**
     * Handle the file send button click
     */
    @FXML
    private void handleSendFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Please choise the file to sent");
        File selectedFile = fileChooser.showOpenDialog(sendButton.getScene().getWindow());

        if (selectedFile != null && chatNode != null) {
            // 检查文件大小限制（例如100MB）
            long maxFileSize = 100 * 1024 * 1024; // 100MB
            if (selectedFile.length() > maxFileSize) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Oversized file");
                alert.setHeaderText("Illegal file size");
                alert.setContentText("The file size cannot exceed 100MB");
                alert.showAndWait();
                return;
            }

            // 显示确认对话框
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Send files to group");
            confirmAlert.setHeaderText("Confirm sending file");
            confirmAlert.setContentText(
                    String.format("File name: %s\nFile Size: %.2f MB\n\nAre you sure you want to send to group?",
                            selectedFile.getName(), selectedFile.length() / (1024.0 * 1024.0)));

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Send group chat file request
                sendGroupFileRequest(selectedFile);
            }
        }
    }

    /**
     * Send group chat file request
     */
    private void sendGroupFileRequest(File file) {
        try {
            // Create file information string
            String fileInfo = String.format("%s:%d", file.getName(), file.length());

            // Send group chat file request message
            chatNode.sendGroupFileRequest(file);

            // Display file sending information on the interface
            addSentMessage(String.format("[File] %s (%.2f MB)",
                    file.getName(), file.length() / (1024.0 * 1024.0)),
                    ChatMessage.MessageType.SENT);

            addSystemMessage("Group chat file request sent: " + file.getName());

        } catch (Exception e) {
            addSystemMessage("Failed to send file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle the connect button click
     */

    @FXML
    private void handleConnect() {
        TextInputDialog dialog = new TextInputDialog("localhost:8081");
        dialog.setTitle("Connect to a node");
        dialog.setHeaderText("Please enter the node address to connect");
        dialog.setContentText("Address (host:port):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(address -> {
            if (chatNode.connectToPeer(address)) {
                addSystemMessage("Successfully linked to: " + address);
            } else {
                addSystemMessage("linked failed: " + address);
            }
        });
    }

    /**
     * The keyboard events
     */
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (event.isShiftDown()) {
                // Shift+Enter line break
                messageInput.appendText("\n");
                event.consume();
            } else {
                // Enter sent message
                event.consume();
                handleSendMessage();
            }
        }
    }

    /**
     * Add a sent message
     */
    private void addSentMessage(String content, ChatMessage.MessageType type) {
        Platform.runLater(() -> {
            ChatMessage message = new ChatMessage(chatNode.getDisplayName(), content, type);
            messages.add(message);
            scrollToBottom();
        });
    }

    /**
     * Add received messages
     */
    public void addReceivedMessage(String senderId, String content, ChatMessage.MessageType type) {
        Platform.runLater(() -> {
            // Try to convert the node ID to a display name
            String displayName = getDisplayNameForNodeId(senderId);
            ChatMessage message = new ChatMessage(displayName, content, type);
            messages.add(message);
            scrollToBottom();
        });
    }

    /**
     * Get the display name based on the node ID
     */
    private String getDisplayNameForNodeId(String nodeId) {
        // Search among online members
        OnlineMember member = onlineMembers.stream()
                .filter(m -> m.getNodeId().equals(nodeId))
                .findFirst()
                .orElse(null);

        if (member != null) {
            return member.getDisplayName();
        }

        // If not found, return the simplified node ID
        if (nodeId.length() > 8) {
            return "Node_" + nodeId.substring(0, 8);
        }
        return nodeId;
    }

    /**
     * Add system message
     */
    private void addSystemMessage(String content) {
        Platform.runLater(() -> {
            ChatMessage message = new ChatMessage("System", content, ChatMessage.MessageType.SYSTEM);
            messages.add(message);
            scrollToBottom();
        });
    }

    /**
     * rolling to btm
     */
    private void scrollToBottom() {
        Platform.runLater(() -> {
            if (!messages.isEmpty()) {
                messageListView.scrollTo(messages.size() - 1);
            }
        });
    }

    /**
     * Update the number of connections displayed
     */
    private void updateConnectionCount() {
        if (chatNode != null) {
            Platform.runLater(() -> {
                int count = chatNode.getConnectionCount();
                connectionCountLabel.setText("Connections: " + count);
            });
        }
    }

    /**
     * Update the number of members displayed
     */
    private void updateMemberCount() {
        Platform.runLater(() -> {
            memberCountLabel.setText("(" + onlineMembers.size() + ")");
        });
    }

    /**
     * Start the status update timer
     */
    private void startStatusUpdateTimer() {
        statusUpdateTimer = new Timer("StatusUpdate", true);
        statusUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateConnectionCount();
            }
        }, 1000, 5000); // Update every 5 seconds
    }

    // MessageListener interface implementation
    @Override
    public void onChatMessageReceived(String senderId, String content) {
        addReceivedMessage(senderId, content, ChatMessage.MessageType.RECEIVED);
    }

    @Override
    public void onPrivateChatMessageReceived(String senderId, String content) {
        // Display private chat messages in the main window
        addReceivedMessage(senderId, "[private] " + content, ChatMessage.MessageType.RECEIVED);

        // If there is a corresponding private chat window, it will also be displayed in
        // the private chat window
        PrivateChatWindow privateChatWindow = privateChatWindows.get(senderId);
        if (privateChatWindow != null && privateChatWindow.isShowing()) {
            privateChatWindow.addReceivedMessage(senderId, content);
        }
    }

    @Override
    public void onFileTransferRequest(String senderId, String fileName, long fileSize) {
        Platform.runLater(() -> {
            // Format file size display
            String fileSizeStr;
            if (fileSize < 1024) {
                fileSizeStr = fileSize + " bytes";
            } else if (fileSize < 1024 * 1024) {
                fileSizeStr = String.format("%.2f KB", fileSize / 1024.0);
            } else {
                fileSizeStr = String.format("%.2f MB", fileSize / (1024.0 * 1024.0));
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("File transfer request");
            alert.setHeaderText(senderId + " want to send you a file");
            alert.setContentText("File name: " + fileName + "\nFile size: " + fileSizeStr + "\n\nconfirm?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Select the save location
                String downloadPath = chooseDownloadLocation(fileName);
                if (downloadPath != null) {
                    addSystemMessage(
                            "Accepted from " + senderId + " 's Files': " + fileName + " (" + fileSizeStr + ")");
                    addSystemMessage("The file will be saved to: " + downloadPath);

                    // If there is a corresponding private chat window, it will also be displayed in
                    // the private chat window
                    PrivateChatWindow privateChatWindow = privateChatWindows.get(senderId);
                    if (privateChatWindow != null && privateChatWindow.isShowing()) {
                        privateChatWindow.addFileTransferMessage(senderId, fileName, true);
                    }

                    // Notify the sender to start file transfer
                    chatNode.acceptFileTransfer(senderId, fileName, downloadPath);
                } else {
                    addSystemMessage("File reception has been canceled: " + fileName);
                }
            } else {
                addSystemMessage("Rejected from " + senderId + " 's File': " + fileName + " (" + fileSizeStr + ")");
                // Notify the sender to reject the file transfer
                chatNode.rejectFileTransfer(senderId, fileName);
            }
        });
    }

    @Override
    public void onConnectionStatusChanged(int connectionCount) {
        updateConnectionCount();
    }

    @Override
    public void onSystemMessage(String message) {
        addSystemMessage(message);
    }

    @Override
    public void onMemberJoined(String nodeId, String address) {
        Platform.runLater(() -> {
            // Filter out your own nodes
            if (chatNode != null && nodeId.equals(chatNode.getNodeIdString())) {
                return;
            }

            // Check if it already exists
            boolean exists = onlineMembers.stream()
                    .anyMatch(member -> member.getNodeId().equals(nodeId));

            if (!exists) {
                OnlineMember newMember = new OnlineMember(nodeId, address);
                onlineMembers.add(newMember);
                updateMemberCount();
                addSystemMessage("Member " + newMember.getDisplayName() + " join the chat!");
            }
        });
    }

    @Override
    public void onMemberLeft(String nodeId) {
        Platform.runLater(() -> {
            // Find the member to be removed and get its display name
            OnlineMember memberToRemove = onlineMembers.stream()
                    .filter(member -> member.getNodeId().equals(nodeId))
                    .findFirst()
                    .orElse(null);

            if (memberToRemove != null) {
                onlineMembers.remove(memberToRemove);
                updateMemberCount();
                addSystemMessage("Member " + memberToRemove.getDisplayName() + " leave the chat!");
            }
            // Handling when a member leaves (simplified)

            // Update the status of the corresponding private chat window
            PrivateChatWindow privateChatWindow = privateChatWindows.get(nodeId);
            if (privateChatWindow != null && privateChatWindow.isShowing()) {
                privateChatWindow.updateOnlineStatus(false);
                privateChatWindow.addSystemMessage("User offline");
            }
        });
    }

    /**
     * Show the about dialog
     */
    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("P2P chat Application");
        alert.setContentText("A fully functional decentralized chat application\n\n" +
                "characteristic:\n" +
                "• Group and private chats\n" +
                "• The p2p file transfer\n" +
                "• Online members list\n" +
                "• Decentralized network architecture\n" +
                "• Message flooding\n" +
                "• Automatic node discovery");
        alert.showAndWait();
    }

    /**
     * Display the settings dialog
     */
    private void showSettingsDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Setting");
        alert.setHeaderText("Setting");
        alert.setContentText("Settings is under development...");
        alert.showAndWait();
    }

    /**
     * Select the file download location
     */
    private String chooseDownloadLocation(String fileName) {
        // Create the default download directory
        String userHome = System.getProperty("user.home");
        String defaultDownloadDir = userHome + File.separator + "P2PChat_Downloads";
        File downloadDir = new File(defaultDownloadDir);

        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }

        // Use the file chooser to let the user choose a save location
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a location to save the file");
        fileChooser.setInitialDirectory(downloadDir);
        fileChooser.setInitialFileName(fileName);

        File selectedFile = fileChooser.showSaveDialog(sendButton.getScene().getWindow());

        if (selectedFile != null) {
            return selectedFile.getAbsolutePath();
        } else {
            // If the user deselects, use the default position
            File defaultFile = new File(downloadDir, fileName);
            // If the file already exists, add a numeric suffix
            int counter = 1;
            while (defaultFile.exists()) {
                String nameWithoutExt = fileName;
                String extension = "";
                int lastDot = fileName.lastIndexOf('.');
                if (lastDot > 0) {
                    nameWithoutExt = fileName.substring(0, lastDot);
                    extension = fileName.substring(lastDot);
                }
                defaultFile = new File(downloadDir, nameWithoutExt + "_" + counter + extension);
                counter++;
            }
            return defaultFile.getAbsolutePath();
        }
    }

    /**
     * Clean up resources
     */
    public void cleanup() {
        if (statusUpdateTimer != null) {
            statusUpdateTimer.cancel();
        }

        // Close all private chat windows
        for (PrivateChatWindow window : privateChatWindows.values()) {
            if (window.isShowing()) {
                window.close();
            }
        }

        if (emojiStage != null) {
            emojiStage.close();
        }

        privateChatWindows.clear();
    }
}
