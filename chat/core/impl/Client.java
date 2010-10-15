package core.impl;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;

import core.ChatEventListener;
import chatsession.pdu.ChatAction;

public class Client implements ChatEventListener {
    private ClientCommunicator communicator;
    private String userName;
    private Frame loginFrame, chatFrame;
    private List userList;
    private TextArea chatArea;
    private TextField nameField, chatField;
    private Button submitButton, logoutButton;

    private void showLoginFrame() {
        loginFrame = new Frame();
        loginFrame.setLayout(new FlowLayout());

        loginFrame.add(new Label("Name:"));

        nameField = new TextField(20);
        loginFrame.add(nameField);

        Button loginButton = new Button("login");
        loginButton.addMouseListener(new LoginListener());
        loginFrame.add(loginButton);

        loginFrame.pack();
        loginFrame.setVisible(true);
    }

    private void showChatFrame() {
        Panel usersPanel = new Panel();
        usersPanel.setLayout(new BorderLayout());
        usersPanel.add(new Label("Current users:"), BorderLayout.NORTH);
        userList = new List(5, false);
        usersPanel.add(userList, BorderLayout.SOUTH);

        Panel chatPanel = new Panel();
        chatPanel.setLayout(new BorderLayout());
        chatPanel.add(new Label("Chat:"), BorderLayout.NORTH);
        chatArea = new TextArea(10, 20);
        chatPanel.add(chatArea, BorderLayout.SOUTH);

        Panel ownPanel = new Panel();
        ownPanel.setLayout(new BorderLayout());
        ownPanel.add(new Label("What I want to say:"), BorderLayout.NORTH);
        chatField = new TextField(20);
        ownPanel.add(chatField, BorderLayout.CENTER);
        Panel buttonsPanel = new Panel();
        submitButton = new Button("submit");
        submitButton.addMouseListener(new ChatListener());
        buttonsPanel.add(submitButton);
        logoutButton = new Button("logout");
        logoutButton.addMouseListener(new ChatListener());
        buttonsPanel.add(logoutButton);
        ownPanel.add(buttonsPanel, BorderLayout.SOUTH);

        chatFrame = new Frame();
        chatFrame.setLayout(new BorderLayout());
        chatFrame.add(usersPanel, BorderLayout.NORTH);
        chatFrame.add(chatPanel, BorderLayout.CENTER);
        chatFrame.add(ownPanel, BorderLayout.SOUTH);
        chatFrame.pack();
        chatFrame.setVisible(true);
    }

    @Override
    public void onMessage(String username, String message) {
        chatArea.append(username + ": " + message + "\n");
        chatFrame.setVisible(true);
    }

    @Override
    public void onUserListUpdate(String[] userList) {
        fillUserList(new Vector<String>(Arrays.asList(userList)));
        chatFrame.setVisible(true);
    }

    @Override
    public void onAction(int actionId, String reserved) {
        if (actionId == ChatAction.CHATACTION_USERNAME_SCHON_VERGEBEN) {

            chatArea
                .append("Der Username ist schon vergeben. Bitte loggen Sie sich aus und starten Sie die Anwendung neu");
        }

    }

    private void fillUserList(Vector<String> names) {
        userList.removeAll();
        for (int i = 0; i < names.size(); i++)
            if (names.get(i).equals(userName)) userList.add("<" + names.get(i) + ">");
            else userList.add(names.get(i) + " ");
    }

    public Client() {
        PropertyConfigurator.configureAndWatch("log4j.properties", 60 * 1000);

        communicator = new ClientCommunicator(this);
        showLoginFrame();
    }

    public static void main(String[] args) {
        new Client();
    }

    class LoginListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            userName = nameField.getText();

            loginFrame.dispose();
            showChatFrame();
            communicator.login(userName);
        }
    }

    class ChatListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            if (e.getSource() == submitButton) {
                communicator.tell(userName, chatField.getText());
                chatField.setText("");
            } else if (e.getSource() == logoutButton) {
                communicator.logout(userName);
                System.exit(0);
            }
        }
    }

} // Client
