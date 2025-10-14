package gs.com.gses.utility;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
    //    @Async
    public static List<String> saveFiles(MultipartFile[] files, String directory) throws IOException {
        List<String> tempFiles = new ArrayList<String>();
        if (files != null && files.length > 0) {
            //遍历文件
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    LocalDateTime localDateTime = LocalDateTime.now();
                    String dateStr = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

//                    String directory = "\\uploadfiles" + "\\" + dateStr + "\\";
                    File destFile = new File(directory);
                    //判断路径是否存在,和C#不一样。她判断路径和文件是否存在
                    if (!destFile.exists()) {
                        destFile.mkdirs();
                    }

                    //获取文件名称
                    String sourceFileName = file.getOriginalFilename();
                    //写入目的文件
                    String fileFullName = directory + sourceFileName;
                    File existFile = new File(fileFullName);
                    if (existFile.exists()) {
                        existFile.delete();
                    }
                    //用 file.transferTo 要指定盘符，不然报错找不到文件路径
                    //引入commons-io 依赖，用下面方法不用指定盘符
                    file.transferTo(existFile);
//                    FileUtils.copyInputStreamToFile(file.getInputStream(), existFile);
                    tempFiles.add(fileFullName);
                }
            }
        }
        return tempFiles;
    }


}
