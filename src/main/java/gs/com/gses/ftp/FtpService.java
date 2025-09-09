package gs.com.gses.ftp;

import org.apache.commons.net.ftp.FTPFile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface FtpService {

    boolean uploadFile(MultipartFile[] files) throws Exception;

    boolean uploadFile(byte[] fileData, String remoteFileName) throws Exception;

    boolean uploadFile( String localFileName, String remoteFileName) throws Exception;

    byte[] downloadFile(String fileFullName);

    FTPFile getFileInfo(String remoteFilePath);

    List<FTPFile> listFiles(String remoteDirPath);

    boolean fileExists(String remoteFilePath);


    ResponseEntity<byte[]> ftpDownloadFileResponseEntity(String fileFullName);

    ResponseEntity<byte[]> ftpPreviewFile(String filePath);

    void ftpDownloadFile(String filePath, HttpServletResponse response);


}
