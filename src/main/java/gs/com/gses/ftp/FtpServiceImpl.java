package gs.com.gses.ftp;

import gs.com.gses.model.elasticsearch.InventoryInfo;
import gs.com.gses.model.entity.Inventory;
import gs.com.gses.model.entity.InventoryItem;
import gs.com.gses.model.entity.Laneway;
import gs.com.gses.model.entity.Location;
import gs.com.gses.model.utility.RedisKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SourceFilter;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FtpServiceImpl implements FtpService {


    //    private static final Logger log = LoggerFactory.getLogger(FtpServiceImpl.class);
    private FTPClient ftpClient;

    @Autowired
    private FtpConfig ftpConfig;

    @Autowired
    private RedissonClient redissonClient;

//    @Value("${ftp.base-path}")
//    private String basePath;

    @PostConstruct
    public void init() {
        ftpClient = new FTPClient();
        ftpClient.setControlEncoding("GBK");
        //windows
//        ftpClient.setControlEncoding("GBK");
//        ftpClient.setAutodetectUTF8(true);

        //linux 在连接后立即发送OPTS UTF8 ON命令
//        ftpClient.sendCommand("OPTS UTF8 ON");
//        ftpClient.setControlEncoding("UTF-8");
//
        try {
            connectAndLogin(ftpClient);
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to FTP server", e);
        }
    }


//    private void connectAndLogin(FTPClient ftpClient) throws IOException {
//        ftpClient.connect(ftpConfig.getHost(), ftpConfig.getPort());
//        if (!ftpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword())) {
//            throw new IOException("FTP login failed.");
//        }
//        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
//        if (ftpConfig.getPassiveMode()) {
//            ftpClient.enterLocalPassiveMode();
//        }
//        ftpClient.setControlKeepAliveTimeout(60);
//
//
//    }


    private void connectAndLogin(FTPClient ftpClient) throws IOException {
        try {
            // 先检查是否已连接，避免重复连接
            if (ftpClient.isConnected()) {
                try {
                    // 发送NOOP命令测试连接是否仍然有效
                    ftpClient.sendNoOp();
                    return; // 连接有效，直接返回
                } catch (IOException e) {
                    // NOOP失败，说明连接已断开，需要重新连接
                    log.info("连接已断开，尝试重新连接...");
                    disconnectQuietly(ftpClient);
                }
            }

            // 设置连接超时和数据处理超时
            ftpClient.setConnectTimeout(30000);
            ftpClient.setDefaultTimeout(30000);
            ftpClient.setDataTimeout(30000);

            // 连接服务器
            ftpClient.connect(ftpConfig.getHost(), ftpConfig.getPort());

            // 检查连接响应
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                throw new IOException("FTP连接失败，响应码: " + replyCode);
            }

            // 登录
            if (!ftpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword())) {
                throw new IOException("FTP登录失败");
            }

            // 设置文件类型
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // 设置传输模式
            if (ftpConfig.getPassiveMode()) {
                ftpClient.enterLocalPassiveMode();
            } else {
                ftpClient.enterLocalActiveMode();
            }

            // 设置keep-alive（您原来的设置）
            ftpClient.setControlKeepAliveTimeout(60);

            System.out.println("FTP连接成功建立");

        } catch (IOException e) {
            disconnectQuietly(ftpClient);
            throw new IOException("FTP连接失败: " + e.getMessage(), e);
        }
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

//    // 一个安静关闭连接的工具方法
//    private void disconnectQuietly(FTPClient ftpClient) {
//        if (ftpClient.isConnected()) {
//            try {
//                ftpClient.disconnect();
//            } catch (IOException e) {
//                // 忽略断开连接时出现的异常
//            }
//        }
//    }


    // 安全的断开连接方法
    private void disconnectQuietly(FTPClient ftpClient) {
        try {
            if (ftpClient != null && ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        } catch (IOException e) {
            // 安静地处理断开连接异常
            log.error("断开连接时发生错误: " + e.getMessage());
        }
    }


    @Override
    public boolean uploadFile(MultipartFile[] files) throws Exception {
        //        List<String> fileNames = saveFiles(files);
        //获取body中的参数
//            String value = (String)request.getAttribute("paramName");

        if (files == null || files.length == 0) {
            throw new Exception("files is null");
        }

        String rootPath = ftpConfig.getBasePath();
        String basePath = rootPath + buildDateBasedPath();


        List<String> filePathList = new ArrayList<>();
        String filePath = "";
        for (MultipartFile file : files) {
            filePath = basePath + file.getOriginalFilename();
            boolean success = uploadFile(file.getBytes(), filePath);
            filePathList.add(filePath);
        }
        return true;
    }

    /**
     *
     * @param fileData
     * @param filaPath
     * @return
     * @throws Exception
     */
    @Override
    public boolean uploadFile(byte[] fileData, String filaPath) throws Exception {
        // 使用 try-with-resources 确保 InputStream 被关闭
        try (InputStream inputStream = new ByteArrayInputStream(fileData)) {

            ensureConnected(ftpClient);

            String basePath = extractDirectoryPath(filaPath);
            String fileName = extractFileName(filaPath);
            boolean changedRoot = ftpClient.changeWorkingDirectory("/");
            // 确保基础路径存在
            if (!ftpClient.changeWorkingDirectory(basePath)) {
                // 如果目录不存在，则创建（包括所有不存在的父目录）
                createDirectory(basePath);
                boolean re = ftpClient.changeWorkingDirectory(basePath);
            }

            // 执行上传
            boolean isUploaded = ftpClient.storeFile(fileName, inputStream);
            if (isUploaded) {
                log.info("File uploaded successfully: {}", fileName);
            } else {
                throw new Exception("File upload failed. Reply code: " + ftpClient.getReplyCode());
            }
            return isUploaded;

        } catch (Exception e) {
            throw e;

        }
    }

    @Override
    public boolean uploadFile(String localFileName, String remoteFileName) throws Exception {
        File localFile = new File(localFileName);

        // 检查本地文件是否存在
        if (!localFile.exists()) {
            throw new FileNotFoundException("本地文件不存在: " + localFileName);
        }

        // 使用 try-with-resources 确保 FileInputStream 被关闭
        try (FileInputStream inputStream = new FileInputStream(localFile)) {

            ensureConnected(ftpClient);

            String basePath = extractDirectoryPath(remoteFileName);
            String fileName = extractFileName(remoteFileName);
            boolean changedRoot = ftpClient.changeWorkingDirectory("/");
            // 确保基础路径存在
            if (!ftpClient.changeWorkingDirectory(basePath)) {
                // 如果目录不存在，则创建（包括所有不存在的父目录）
                createDirectory(basePath);
                ftpClient.changeWorkingDirectory(basePath);
            }
            log.info("当前工作目录: {}", ftpClient.printWorkingDirectory());

            // 执行上传
            boolean isUploaded = ftpClient.storeFile(fileName, inputStream);
            if (isUploaded) {
                log.info("文件上传成功: {} -> {}", localFileName, remoteFileName);
            } else {
                String replyString = ftpClient.getReplyString();
                int replyCode = ftpClient.getReplyCode();
                throw new Exception("文件上传失败. 响应码: " + replyCode + ", 响应信息: " + replyString);

            }
            return isUploaded;

        } catch (Exception e) {
            log.error("文件上传异常: {}", localFileName, e);
            throw e;
        }
    }

    /**
     * 递归创建远程目录（如果不存在）
     * @param path 要创建的目录路径
     */
    private void createDirectory(String path) throws IOException {
        String[] pathParts = path.split("/");
        StringBuilder currentPath = new StringBuilder();

        for (String dir : pathParts) {
            if (dir.isEmpty()) continue;
            currentPath.append("/").append(dir);
            // 检查目录是否存在
            if (!ftpClient.changeWorkingDirectory(currentPath.toString())) {
                // 如果不存在，则创建
                if (ftpClient.makeDirectory(currentPath.toString())) {
                    log.info("Created directory: " + currentPath);
                } else {
//                    throw new IOException("Could not create directory: " + currentPath);

                    int replyCode = ftpClient.getReplyCode();
                    String replyString = ftpClient.getReplyString();
                    log.error("创建目录失败: {}", currentPath);
                    log.error("FTP响应码: {}, 响应信息: {}", replyCode, replyString);

                    throw new IOException("Could not create directory: " + currentPath +
                            ", Reply: " + replyCode + " - " + replyString);
                }
            }
        }
    }


    @Override
    public byte[] downloadFile(String fileFullName) {

        try {
            ensureConnected(ftpClient);

            String fullPath = extractDirectoryPath(fileFullName);
            String fileName = extractFileName(fileFullName);
            // 切换到文件所在目录
            boolean changedRoot = ftpClient.changeWorkingDirectory("/");
            if (!ftpClient.changeWorkingDirectory(fullPath)) {
                throw new IOException("目录不存在: " + fullPath);
            }

            // 下载文件
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                boolean isDownloaded = ftpClient.retrieveFile(fileName, outputStream);

                if (isDownloaded) {
                    log.info("文件下载成功: {}/{}", fullPath, fileName);
                    return outputStream.toByteArray();
                } else {
                    log.error("文件下载失败. 响应码: {}", ftpClient.getReplyCode());
                    throw new IOException("FTP retrieveFile failed");
                }
            }

        } catch (IOException e) {
            log.error("FTP下载过程中发生错误", e);
            throw new RuntimeException("文件下载失败", e);
        }
    }

    // 辅助方法：提取目录路径
    private String extractDirectoryPath(String fullPath) {
        int lastSlashIndex = fullPath.lastIndexOf('/');
        if (lastSlashIndex > 0) {
            return fullPath.substring(0, lastSlashIndex);
        }
        return "/"; // 如果只有文件名，返回根目录
    }

    // 辅助方法：提取文件名
    private String extractFileName(String fullPath) {
        int lastSlashIndex = fullPath.lastIndexOf('/');
        if (lastSlashIndex >= 0 && lastSlashIndex < fullPath.length() - 1) {
            return fullPath.substring(lastSlashIndex + 1);
        }
        return fullPath; // 如果没有斜杠，整个字符串就是文件名
    }

    /**
     * 构建基于日期的相对路径
     */
    public String buildDateBasedPath() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd/");
        return today.format(formatter);
    }

    /**
     * 生成一个带时间戳的唯一文件名，避免覆盖
     * @param originalName 原始文件名（不含路径）
     * @return 带时间戳的新文件名
     */
    public String generateFileName(String originalName) {
        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        int dotIndex = originalName.lastIndexOf('.');
        String nameWithoutExt = (dotIndex == -1) ? originalName : originalName.substring(0, dotIndex);
        String extension = (dotIndex == -1) ? "" : originalName.substring(dotIndex);
        return nameWithoutExt + "_" + timeStamp + extension;
    }


    /**
     * 获取文件信息
     * @param remoteFilePath 远程文件路径
     * @return FTPFile对象，如果文件不存在返回null
     */
    @Override
    public FTPFile getFileInfo(String remoteFilePath) {
        try {
            ensureConnected(ftpClient);
            FTPFile[] files = ftpClient.listFiles(remoteFilePath);
            if (files.length > 0) {
                return files[0];
            }
            return null;
        } catch (IOException e) {
            log.error("获取文件信息时发生错误: {}", remoteFilePath, e);
            return null;
        }
    }

    /**
     * 列出目录下的文件
     * @param remoteDirPath 远程目录路径
     * @return 文件列表
     */
    @Override
    public List<FTPFile> listFiles(String remoteDirPath) {
        List<FTPFile> fileList = new ArrayList<>();
        try {
            ensureConnected(ftpClient);
            FTPFile[] files = ftpClient.listFiles(remoteDirPath);
            if (files != null) {
                fileList.addAll(Arrays.asList(files));
            }
        } catch (IOException e) {
            log.error("列出目录文件时发生错误: {}", remoteDirPath, e);
        }
        return fileList;
    }


    /**
     * 检查文件是否存在
     * @param remoteFilePath 远程文件路径  /wms/2025/08/23/readSpecificCell.xlsx
     * @return 是否存在
     */
    @Override
    public boolean fileExists(String remoteFilePath) {
        try {
            ensureConnected(ftpClient);
            FTPFile[] files = ftpClient.listFiles(remoteFilePath);
            return files.length > 0;
        } catch (IOException e) {
            log.error("检查文件是否存在时发生错误: {}", remoteFilePath, e);
            return false;
        }
    }

    @Override
    public ResponseEntity<byte[]> ftpDownloadFileResponseEntity(String filePath) {

        try {
            // 检查文件是否存在
            if (!fileExists(filePath)) {
                log.warn("文件不存在: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            // 获取文件信息
            FTPFile fileInfo = getFileInfo(filePath);
            if (fileInfo == null) {
                log.warn("无法获取文件信息: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            // 下载文件内容
            byte[] fileData = downloadFile(filePath);

            // 获取文件名（优先使用FTPFile的名称，否则从路径中提取）
            String fileName = fileInfo.getName();
            if (fileName == null || fileName.isEmpty()) {
                fileName = extractFileNameFromPath(filePath);
            }

            // 设置正确的Content-Type .APPLICATION_OCTET_STREAM 是通用的二进制流类型
            String contentType = getContentType(fileName);

            // 处理中文文件名编码
            String encodedFileName = encodeFileName(fileName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.set("Content-Disposition", createContentDispositionHeader(fileName));
            headers.setContentLength(fileData.length);

            // 添加缓存控制头（可选）
            headers.setCacheControl(CacheControl.noCache().mustRevalidate());
            headers.setPragma("no-cache");
            headers.setExpires(0);

            log.info("成功下载文件: {}, 大小: {} bytes", filePath, fileData.length);
            return new ResponseEntity<>(fileData, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("下载文件失败: {}", filePath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("下载失败: " + e.getMessage()).getBytes());
        }
    }

    @Override
    public ResponseEntity<byte[]> ftpPreviewFile(String filePath) {
        try {
            if (!fileExists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            byte[] fileData = downloadFile(filePath);

            // 根据文件类型设置Content-Type
            String contentType = getContentType(filePath);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDisposition(
                    ContentDisposition.inline()
                            .filename(filePath.substring(filePath.lastIndexOf("/") + 1), StandardCharsets.UTF_8)
                            .build()
            );

            return new ResponseEntity<>(fileData, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 创建兼容的Content-Disposition头
     */
    private String createContentDispositionHeader(String fileName) {
        try {
            // URL编码文件名
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name())
                    .replaceAll("\\+", "%20");

            // 兼容格式：同时提供filename和filename*
            return String.format("attachment; filename=\"%s\"; filename*=UTF-8''%s",
                    getSafeFileName(fileName), // 用于旧浏览器
                    encodedFileName);          // 用于新浏览器
        } catch (Exception e) {
            return "attachment; filename=\"download.xlsx\"";
        }
    }

    /**
     * 获取安全的文件名（移除特殊字符）
     */
    private String getSafeFileName(String fileName) {
        if (fileName == null) return "download.xlsx";
        // 移除可能引起问题的字符
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    @Override
    public void ftpDownloadFile(String filePath, HttpServletResponse response) {
        try {
            // 检查文件是否存在
            if (!fileExists(filePath)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("文件不存在");
                return;
            }

            // 获取文件信息
            FTPFile fileInfo = getFileInfo(filePath);
            if (fileInfo == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("文件信息获取失败");
                return;
            }

            // 设置响应头
            String fileName = fileInfo.getName() != null ? fileInfo.getName() :
                    filePath.substring(filePath.lastIndexOf("/") + 1);

            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");

            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
            response.setContentLengthLong(fileInfo.getSize());
            byte[] fileData = downloadFile(filePath);

//            // 加载到内存下载
//            try (OutputStream outputStream = response.getOutputStream()) {
//                outputStream.write(fileData);
//                outputStream.flush();
//            }

//            // 流式下载，避免内存溢出
            OutputStream outputStream = response.getOutputStream();

            // 流式下载 - 直接从FTP服务器流式传输到HTTP响应
            boolean downloadSuccess = downloadFileToStream(filePath, outputStream);

            if (!downloadSuccess) {
                throw new IOException("FTP文件下载失败");
            }

            outputStream.flush();


        } catch (Exception e) {
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write("下载失败: " + e.getMessage());
            } catch (IOException ex) {
                // 记录日志
                log.error("写入错误响应失败", ex);
            }
        }
    }

    /**
     * 流式下载文件到输出流
     */
    private boolean downloadFileToStream(String remoteFilePath, OutputStream outputStream) {
        try {
            ensureConnected(ftpClient);
            return ftpClient.retrieveFile(remoteFilePath, outputStream);
        } catch (IOException e) {
            log.error("流式下载失败: {}", remoteFilePath, e);
            return false;
        }
    }

    /**
     * 从文件路径中提取文件名
     */
    private String extractFileNameFromPath(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "download.xlsx";
        }

        // 处理Windows和Linux路径分隔符
        int lastSlashIndex = Math.max(
                filePath.lastIndexOf("/"),
                filePath.lastIndexOf("\\")
        );

        if (lastSlashIndex >= 0 && lastSlashIndex < filePath.length() - 1) {
            return filePath.substring(lastSlashIndex + 1);
        }

        return filePath; // 如果没有分隔符，返回整个路径
    }

    /**
     * 根据文件扩展名获取Content-Type
     */
    private String getContentType(String fileName) {
        if (fileName == null) {
            return "application/octet-stream";
        }

        String extension = fileName.toLowerCase();
        if (extension.endsWith(".xlsx")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else if (extension.endsWith(".xls")) {
            return "application/vnd.ms-excel";
        } else if (extension.endsWith(".pdf")) {
            return "application/pdf";
        } else if (extension.endsWith(".jpg") || extension.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (extension.endsWith(".png")) {
            return "image/png";
        } else if (extension.endsWith(".txt")) {
            return "text/plain";
        } else if (extension.endsWith(".html") || extension.endsWith(".htm")) {
            return "text/html";
        } else {
            return "application/octet-stream";
        }
    }

    /**
     * 处理中文文件名编码（兼容不同浏览器）
     */
    private String encodeFileName(String fileName) {
        try {
            // 对文件名进行URL编码
            String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name())
                    .replaceAll("\\+", "%20");

            // 返回兼容格式：UTF-8编码的文件名，用双引号包裹
            return "UTF-8''" + encoded;
        } catch (UnsupportedEncodingException e) {
            log.warn("文件名编码失败，使用原始文件名: {}", fileName, e);
            return fileName;
        }
    }


    /**
     * 删除指定目录下的所有文件（但不删除子目录本身）
     * @param directoryPath 要清空的目录路径
     * @return 成功返回 true，失败返回 false
     */
    @Override
    public boolean deleteAllFilesInDirectory(String directoryPath) throws Exception {

        boolean success = true;
        ensureConnected(ftpClient);

        // 切换到目标目录
        boolean changedRoot = ftpClient.changeWorkingDirectory("/");
        boolean changed = ftpClient.changeWorkingDirectory(directoryPath);
        if (!changed) {
            log.error("无法切换到目录: {}", directoryPath);
            // 如果目录不存在，则创建（包括所有不存在的父目录）
            createDirectory(directoryPath);
            changed = ftpClient.changeWorkingDirectory(directoryPath);
            return true;
        }

        // 列出目录下的所有文件和子目录
        FTPFile[] files = ftpClient.listFiles();

        if (files == null || files.length == 0) {
            log.info("目录为空: {}", directoryPath);
            return true;
        }

        // 遍历并删除所有文件
        for (FTPFile file : files) {
            if (file.isFile()) { // 只处理文件，不处理目录
                String fileName = file.getName();
                boolean deleted = ftpClient.deleteFile(fileName);
                if (deleted) {
                    success = true;
                    log.info("文件删除成功: {}/{}", directoryPath, fileName);
                } else {
                    log.error("文件删除失败: {}/{}", directoryPath, fileName);
                    success = false; // 记录有失败，但继续尝试删除其他文件
                }
            }
        }
        return success;


    }

    @Override
    public boolean createWorkingDirectory(String directoryPath) throws Exception {

        String lockKey = RedisKey.CREATE_WORKING_DIRECTORY;
        RLock lock = redissonClient.getLock(lockKey);
        boolean lockSuccessfully = false;
        try {
            lockSuccessfully = lock.tryLock(30, 60, TimeUnit.SECONDS);
            if (!lockSuccessfully) {
                log.info("createWorkingDirectory - {} fail ,get lock fail", lockKey);
                return false;
            }
            log.info("createWorkingDirectory - {} acquire lock  success ", lockKey);


            ensureConnected(ftpClient);
            log.info("当前工作目录：" + ftpClient.printWorkingDirectory());
            log.info("目标目录：" + directoryPath);
            // 切换到目标目录
            boolean changedRoot = ftpClient.changeWorkingDirectory("/");
            boolean changed = ftpClient.changeWorkingDirectory(directoryPath);
            if (!changed) {
                log.info("无法切换到目录: {}", directoryPath);
                // 如果目录不存在，则创建（包括所有不存在的父目录）
                createDirectory(directoryPath);
                changed = ftpClient.changeWorkingDirectory(directoryPath);
                if (!changed) {
                    throw new Exception(ftpClient.getReplyString());
                }
            }
            return changed;

        } catch (Exception ex) {
            log.error("", ex);
            throw ex;
        } finally {

            if (lockSuccessfully && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }

            log.info("InventoryInfo - {} release lock  success ", lockKey);

        }


    }


}
