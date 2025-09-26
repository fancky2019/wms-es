package gs.com.gses.utility;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import gs.com.gses.model.response.MessageResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayOutputStream;
import java.util.*;

public class BarcodeUtil {

    public static List<Map<String, String>> getMultipleBarcodes(
            List<String> contents, // 多个内容
            int width,
            int height,
            String type
    ) throws Exception {
        if (CollectionUtils.isEmpty(contents)) {
            throw new Exception("Contents is emppty");
        }
        if (width <= 0) {
            width = 300;
        }
        if (height <= 0) {
            height = 300;
        }
        if (StringUtils.isEmpty(type)) {
            type = "QR_CODE";
        }
        List<Map<String, String>> result = new ArrayList<>();
        BarcodeFormat format = BarcodeFormat.valueOf(type);
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        for (String content : contents) {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, format, width, height, hints);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());

//            String fileName = content + "_" + type + ".png";

            Map<String, String> map = new HashMap<>();
            map.put("barcode", content);
            map.put("base64", base64);
            result.add(map);
        }
        return result;
    }

}
