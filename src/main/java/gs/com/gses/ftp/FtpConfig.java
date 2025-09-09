package gs.com.gses.ftp;


import lombok.Data;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Data
@Component
@ConfigurationProperties(prefix = "ftp")
public class FtpConfig {

//    @Value("${ftp.host}")
    private String host;

//    @Value("${ftp.port}")
    private Integer port;

//    @Value("${ftp.username}")
    private String username;

//    @Value("${ftp.password}")
    private String password;

    private String basePath;
//    @Value("${ftp.passive-mode}")
    private Boolean passiveMode;

//    @Bean
//    public FTPClient ftpClient() throws IOException {
//        FTPClient ftpClient = new FTPClient();
//        try {
//            // 连接服务器
//            ftpClient.connect(host, port);
//            // 登录
//            boolean isLoggedIn = ftpClient.login(username, password);
//            if (!isLoggedIn) {
//                throw new IOException("FTP login failed. Please check credentials.");
//            }
//            // 设置文件类型为二进制，防止文件损坏
//            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
//            // 设置被动模式（重要对于大多数客户端网络环境）
//            if (passiveMode) {
//                ftpClient.enterLocalPassiveMode();
//            }
//
//            // 可选：检查服务器是否支持 UTF-8，解决中文乱码问题
//            if (FTPReply.isPositiveCompletion(ftpClient.sendCommand("OPTS UTF8", "ON"))) {
//                // 如果服务器支持，则使用 UTF-8 编码
//                // 但 Commons Net 3.9.0 默认行为已改善，通常无需手动设置
//            }
//
//        } catch (IOException e) {
//            disconnectQuietly(ftpClient);
//            throw new IOException("Failed to initialize FTP client", e);
//        }
//        return ftpClient;
//    }



    public FTPClient ftpClient() throws IOException {
        FTPClient ftpClient = new FTPClient();
        connectAndLogin(ftpClient);
        return ftpClient;
    }

    private void connectAndLogin(FTPClient ftpClient) throws IOException {
        ftpClient.connect(host, port);
        if (!ftpClient.login(username, password)) {
            throw new IOException("FTP login failed.");
        }
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        if (passiveMode) {
            ftpClient.enterLocalPassiveMode();
        }
        ftpClient.setControlKeepAliveTimeout(60);
    }

    // 工具方法：调用前检测连接是否可用，否则重连
    public FTPClient ensureConnected(FTPClient ftpClient) throws IOException {
        try {
            if (!ftpClient.isConnected() || !ftpClient.sendNoOp()) {
                disconnectQuietly(ftpClient);
                connectAndLogin(ftpClient);
            }
        } catch (IOException e) {
            disconnectQuietly(ftpClient);
            connectAndLogin(ftpClient);
        }
        return ftpClient;
    }

    // 一个安静关闭连接的工具方法
    private void disconnectQuietly(FTPClient ftpClient) {
        if (ftpClient.isConnected()) {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                // 忽略断开连接时出现的异常
            }
        }
    }
}