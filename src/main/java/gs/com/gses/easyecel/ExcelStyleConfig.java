package gs.com.gses.easyecel;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.*;

import java.util.List;

public class ExcelStyleConfig implements CellWriteHandler {


    private List<Integer> columnList;

    private List<Integer> columnLockedList;

    /**
     * 样式类
     */
    private CellStyle cellStyle;

    /**
     * 隐藏索引数
     */
    private List<Integer> hiddenIndices;

    public ExcelStyleConfig(List<Integer> hiddenIndices, List<Integer> columnList, List<Integer> columnLockedList) {
        this.hiddenIndices = hiddenIndices;
        this.columnList = columnList;
        this.columnLockedList = columnLockedList;
    }

    @Override
    public void beforeCellCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Row row, Head head, Integer integer, Integer integer1, Boolean aBoolean) {
        cellStyle = writeSheetHolder.getSheet().getWorkbook().createCellStyle();
    }

    @Override
    public void afterCellCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Cell cell, Head head, Integer integer, Boolean aBoolean) {
        cellStyle.setLocked(false);
        cell.setCellStyle(cellStyle);
        if (!CollectionUtils.isEmpty(hiddenIndices) && hiddenIndices.contains(cell.getColumnIndex())) {
            // 设置隐藏列
            writeSheetHolder.getSheet().setColumnHidden(cell.getColumnIndex(), true);
        }
        if (!CollectionUtils.isEmpty(columnList) && columnList.contains(cell.getColumnIndex())) {
            //设置背景颜色
            cellStyle.setFillBackgroundColor(IndexedColors.YELLOW.getIndex());
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        }
        if (!CollectionUtils.isEmpty(columnLockedList) && columnLockedList.contains(cell.getColumnIndex())) {
            writeSheetHolder.getSheet().protectSheet("password");
            cellStyle.setLocked(true);
            //writeSheetHolder.getSheet().createFreezePane(cell.getColumnIndex(),0);
        }
        // 填充单元格样式
        cell.setCellStyle(cellStyle);
    }

//    @Override
//    public void afterCellDataConverted(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, CellData cellData, Cell cell, Head head, Integer integer, Boolean aBoolean) {
//
//    }
//
//    @Override
//    public void afterCellDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, List<CellData> list, Cell cell, Head head, Integer integer, Boolean aBoolean) {
//
//    }
}
