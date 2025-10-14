package gs.com.gses.utility;

public class PathUtils {

    /**
     * 去除路径中的盘符
     * @param path 原始路径，如 "D://upload//test.zip"
     * @return 去除盘符后的路径，如 "upload/test.zip"
     */
    public static String removeDriveLetter(String path) {
        if (path == null)
            return null;

        // 匹配盘符（如 C:, D:, E: 等）及其后的分隔符
        return path.replaceAll("^[A-Za-z]:[/\\\\]+", "");
    }

    /**
     * 去除盘符并统一使用正斜杠
     */
    public static String removeDriveLetterAndNormalize(String path) {
        if (path == null) return null;

        String removed = removeDriveLetter(path);
        // 统一使用正斜杠
        return removed.replaceAll("[/\\\\]+", "/");
    }
}
