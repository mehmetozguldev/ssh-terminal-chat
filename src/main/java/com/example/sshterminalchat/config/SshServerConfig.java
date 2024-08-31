package com.example.sshterminalchat.config;

import java.io.IOException;
import java.nio.file.Paths;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.example.sshterminalchat.ssh.ChatCommand;

@Configuration
public class SshServerConfig {
    @Value("${ssh.port:2222}")
    private int sshPort;

    private final ChatCommand chatCommand;

    public SshServerConfig(ChatCommand chatCommand) {
        this.chatCommand = chatCommand;
    }

    @Bean
    public SshServer sshServer() throws IOException {
        SshServer sshServer = SshServer.setUpDefaultServer();
        sshServer.setPort(sshPort);
        sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(Paths.get("hostkey.ser")));
        sshServer.start();
        return sshServer;
    }

}
