package com.example.sshterminalchat.ssh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.springframework.stereotype.Component;
import com.example.sshterminalchat.service.ChatService;
import com.example.sshterminalchat.service.UserService;

@Component
public class ChatCommand implements Command {
    private final ChatService chatService;
    private final UserService userService;
    private InputStream inputStream;
    private OutputStream outputStream;
    private ExitCallback exitCallback;
    private Thread thread;

    public ChatCommand(ChatService chatService, UserService userService) {
        this.chatService = chatService;
        this.userService = userService;
    }

    @Override
    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void setErrorStream(OutputStream errorStream) {
    }

    @Override
    public void setExitCallback(ExitCallback exitCallback) {
        this.exitCallback = exitCallback;
    }

    @Override
    public void start(ChannelSession channelSession, Environment env) throws IOException {
        outputStream.write("Welcome to ssh-terminal-chat\n".getBytes());
        outputStream.write("Type 'register' to create a new user or 'login' to start chatting\n".getBytes());
        outputStream.flush();

        thread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if ("register".equalsIgnoreCase(line.trim())) {
                        registerUser(reader);
                    } else if ("login".equalsIgnoreCase(line.trim())) {
                        if (login(reader)) {
                            startChat(reader);
                        }
                    } else {
                        outputStream.write("Unknown command. Type 'register' or 'login'.\n".getBytes());
                        outputStream.flush();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                exitCallback.onExit(0);
            }
        });
        thread.start();
    }

    private void registerUser(BufferedReader reader) throws IOException {
        outputStream.write("Enter username: ".getBytes());
        outputStream.flush();
        String username = reader.readLine();
        outputStream.write("Enter password: ".getBytes());
        outputStream.flush();
        String password = reader.readLine();

        try {
            userService.createUser(username, password);
            outputStream.write("User registered successfully. You can now login.\n".getBytes());
        } catch (Exception e) {
            outputStream.write(("Registration failed: " + e.getMessage() + "\n").getBytes());
        }
        outputStream.flush();
    }

    private boolean login(BufferedReader reader) throws IOException {
        outputStream.write("Enter username: ".getBytes());
        outputStream.flush();
        String username = reader.readLine();
        outputStream.write("Enter password: ".getBytes());
        outputStream.flush();
        String password = reader.readLine();

        if (userService.verifyUser(username, password)) {
            outputStream.write("Login successful. Welcome to the chat!\n".getBytes());
            outputStream.flush();
            return true;
        } else {
            outputStream.write("Login failed. Please try again.\n".getBytes());
            outputStream.flush();
            return false;
        }
    }

    private void startChat(BufferedReader reader) throws IOException {
        chatService.addListener(message -> {
            try {
                outputStream.write((message + "\n").getBytes());
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        String line;
        while ((line = reader.readLine()) != null) {
            chatService.sendMessage(line);
        }
    }

    @Override
    public void destroy(ChannelSession channelSession) throws Exception {
        if (thread != null) {
            thread.interrupt();
        }
    }
}