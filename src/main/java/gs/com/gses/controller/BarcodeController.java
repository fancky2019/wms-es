package gs.com.gses.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import gs.com.gses.model.response.MessageResult;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.util.*;

@RestController
@RequestMapping("/api/barcode")
public class BarcodeController {


    @GetMapping(value = "/barcodeImage", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> barcodeImage(
            @RequestParam String text,
            @RequestParam(defaultValue = "300") int width,
            @RequestParam(defaultValue = "80") int height,
            @RequestParam(defaultValue = "CODE_128") String format // CODE_128, EAN_13 ...
    ) throws Exception {

        BarcodeFormat bf = BarcodeFormat.valueOf(format);
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.MARGIN, 1); // 边距
        BitMatrix matrix = new MultiFormatWriter().encode(text, bf, width, height, hints);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", out);

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        return ResponseEntity.ok().headers(headers).contentType(MediaType.IMAGE_PNG).body(out.toByteArray());
    }

    @GetMapping("/base64")
    public Map<String,String> barcodeBase64(
            @RequestParam String text,
            @RequestParam(defaultValue = "300") int width,
            @RequestParam(defaultValue = "80") int height,
            @RequestParam(defaultValue = "CODE_128") String format
    ) throws Exception {
        BarcodeFormat bf = BarcodeFormat.valueOf(format);
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.MARGIN, 1);
        BitMatrix matrix = new MultiFormatWriter().encode(text, bf, width, height, hints);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", out);
        String base64 = java.util.Base64.getEncoder().encodeToString(out.toByteArray());
        Map<String,String> r = new HashMap<>();
        r.put("base64", base64);
        r.put("mime", "image/png");
        return r;
    }


    /**
     *             //条码type枚举
     * //            com.google.zxing.BarcodeFormat
     *       // 条码格式说明
     *         const formatDescriptions = {
     *             'QR_CODE': 'QR Code：最常用的二维码格式，可存储大量数据，支持多种数据类型',
     *             'CODE_128': 'CODE 128：高密度的一维条码，广泛应用于物流、仓储管理',
     *             'CODE_39': 'CODE 39：可编码数字和字母的一维条码，常用于工业领域',
     *             'EAN_13': 'EAN-13：国际通用的商品条码，用于零售商品标识',
     *             'EAN_8': 'EAN-8：缩短版的商品条码，用于小包装商品',
     *             'UPC_A': 'UPC-A：北美地区常用的商品条码',
     *             'ITF': 'ITF（交插二五码）：主要用于仓储、物流和包装',
     *             'PDF_417': 'PDF417：堆叠式二维条码，可存储大量数据',
     *             'DATA_MATRIX': 'Data Matrix：小型二维条码，适用于小零件标识',
     *             'AZTEC': 'Aztec Code：中心定位的二维条码，适用于打印质量较差的环境'
     *         };
     */


    /**
     * postman response 的preview tab 页下会显示 二维码图片
     * @param content
     * @param width
     * @param height
     * @param type
     * @param mode
     * @param response
     * @return
     */
    @GetMapping("/getBarcodeImage")
    public MessageResult<Void> getBarcodeImage(
            @RequestParam String content,
            @RequestParam(defaultValue = "300") int width,
            @RequestParam(defaultValue = "300") int height,
            @RequestParam(defaultValue = "QR_CODE") String type,
            @RequestParam(defaultValue = "inline") String mode,  // inline / attachment
            HttpServletResponse response) {

        try {
            // 设置参数
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            // 选择码制类型
            BarcodeFormat format = BarcodeFormat.valueOf(type);

            // 生成位图矩阵
            BitMatrix bitMatrix = new MultiFormatWriter()
                    .encode(content, format, width, height, hints);

            // 自动生成文件名：用 content + type
            String rawFileName = content + "_" + type + ".png";

            // 为了避免中文乱码，进行 UTF-8 编码
            String encodedFileName = URLEncoder.encode(rawFileName, StandardCharsets.UTF_8.toString());

            // 设置响应头
            response.setContentType("image/png");
            response.setHeader("Cache-Control", "no-cache");

            // 根据 mode 设置 Content-Disposition
            // inline → 浏览器内显示
            // attachment → 强制下载
            String disposition = mode + "; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName;
           //Postman 的 Response Headers 面板里能看到完整文件名。
            response.setHeader("Content-Disposition", disposition);

            // 输出图片
            try (OutputStream os = response.getOutputStream()) {
                MatrixToImageWriter.writeToStream(bitMatrix, "PNG", os);
                os.flush();
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return  MessageResult.success();
    }


    /**
     * getMultipleBarcodes
     * 返回多个二维码，base64
     * @param contents
     * @param width
     * @param height
     * @param type
     * @return
     */

    @GetMapping("/getMultipleBarcodes")
    @ResponseBody
    public MessageResult<List<Map<String, String>>> getMultipleBarcodes(
            @RequestParam List<String> contents, // 多个内容
            @RequestParam(defaultValue = "300") int width,
            @RequestParam(defaultValue = "300") int height,
            @RequestParam(defaultValue = "QR_CODE") String type
    ) {
        List<Map<String, String>> result = new ArrayList<>();

        try {
            BarcodeFormat format = BarcodeFormat.valueOf(type);
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            for (String content : contents) {
                BitMatrix bitMatrix = new MultiFormatWriter().encode(content, format, width, height, hints);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
                String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());

                String fileName = content + "_" + type + ".png";

                Map<String, String> map = new HashMap<>();
                map.put("fileName", fileName);
                map.put("base64", base64);
                result.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return MessageResult.success(result);
    }


}
