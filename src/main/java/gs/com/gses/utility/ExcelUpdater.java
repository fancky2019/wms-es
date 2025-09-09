package gs.com.gses.utility;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;

import java.io.*;
import java.util.*;

public class ExcelUpdater {
    /**
     * 更新Excel文件中指定单元格的值
     * @param sourceFile 源文件
     * @param outputFile 输出文件
     * @param updateMap Map<单元格地址, 新值> 如: {"A1": "新值", "B2": 123}
     */
    public static void updateCells(String sourceFile, String outputFile, Map<String, Object> updateMap) throws Exception {

        // 1. 使用POI读取现有工作簿
        Workbook workbook = WorkbookFactory.create(new FileInputStream(sourceFile));
        Sheet sheet = workbook.getSheetAt(0);

        // 2. 修改特定单元格
        modifyCellsWithPoi(sheet, updateMap);

        // 3. 保存修改
        FileOutputStream out = new FileOutputStream(outputFile);
        workbook.write(out);
        out.close();
        workbook.close();

        System.out.println("POI修改完成：" + outputFile);


    }

    public static void updateCells(InputStream inputStream, String outputFile, Map<String, Object> updateMap) throws Exception {

        // 1. 使用POI读取现有工作簿
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        // 2. 修改特定单元格
        modifyCellsWithPoi(sheet, updateMap);

        // 3. 保存修改
        FileOutputStream out = new FileOutputStream(outputFile);
        workbook.write(out);
        out.close();
        workbook.close();

        System.out.println("POI修改完成：" + outputFile);

    }

    private static void modifyCellsWithPoi(Sheet sheet, Map<String, Object> updateMap) {
        Workbook workbook = sheet.getWorkbook();
        // 创建一个可重用的“常规”格式样式
        // 注意：CellStyle在工作簿内是有限的，最好将其作为类变量缓存起来，而不是每次调用都创建。
//        CellStyle generalNumberStyle = workbook.createCellStyle();
//        generalNumberStyle.setDataFormat(workbook.createDataFormat().getFormat("General"));

        // 遍历并修改指定单元格
        for (Map.Entry<String, Object> entry : updateMap.entrySet()) {
            String cellAddress = entry.getKey();
            Object value = entry.getValue();

            // 解析单元格地址
            CellReference ref = new CellReference(cellAddress);
            int rowIndex = ref.getRow();
            int colIndex = ref.getCol();

            // 获取或创建行
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                row = sheet.createRow(rowIndex);
            }

            // 获取或创建单元格
            Cell cell = row.getCell(colIndex);
            if (cell == null) {
                cell = row.createCell(colIndex);
            }
//            cell.setCellStyle(generalNumberStyle);
            // 设置值
            if (value instanceof String) {
                cell.setCellValue((String) value);
            } else if (value instanceof Number) {
                cell.setCellValue(((Number) value).doubleValue());
            } else if (value instanceof Boolean) {
                cell.setCellValue((Boolean) value);
            }
        }
    }


}
