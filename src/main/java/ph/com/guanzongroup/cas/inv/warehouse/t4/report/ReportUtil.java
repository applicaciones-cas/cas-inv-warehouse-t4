package ph.com.guanzongroup.cas.inv.warehouse.t4.report;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.inv.warehouse.t4.report.controller.ReportUtilViewController;

/**
 *
 *
 * @author Maynard
 */
public class ReportUtil {

    protected GRiderCAS poGRider;
    protected Map<String, Object> poParamater = new HashMap<>();
    private JSONObject poJSON = new JSONObject();

    protected String psReportName = "sheet";
    protected String psWatermarkPath;
    protected String psJasperPath = "";
    protected String psSQLRecord = "";
    protected String psMessage = "";

    protected boolean pbisAlwaysTop = false;
    protected boolean pbisWithExport = false;
    protected boolean pbisWithExportPDF = false;
    protected boolean pbWillExport = false;
    protected boolean pbWithUI = false;
    protected boolean pbUseDirectRS = false;

    private double xOffset = 0;
    private double yOffset = 0;

    private ReportUtilListener plReportListener;
    private ResultSet loRSRecord;
    private JasperPrint reportPrint = null;
    private JRResultSetDataSource JasperResultResultSetData = null;
    private JRBeanCollectionDataSource JasperCollectionData = null;
    Stage reportStage = null;

    public ReportUtil(GRiderCAS applicationDriver) {
        this.poGRider = applicationDriver;
    }

    public void setApplicationDriver(Object applicationDriver) {
        poGRider = (GRiderCAS) applicationDriver;
    }

    public void setJasperPath(String jasperPath) {
        psJasperPath = jasperPath;
    }

    public void willExport(boolean willExport) {
        pbWillExport = willExport;
    }

    public void isWithExport(boolean iswithExport) {
        pbisWithExport = iswithExport;
    }

    public void isWithExportPDF(boolean iswithExport) {
        pbisWithExportPDF = iswithExport;
    }

    public void isWithUI(boolean iswithUI) {
        pbWithUI = iswithUI;
    }

    public void setSQLReport(String SQLRecord) {
        psSQLRecord = SQLRecord;
    }

    public void setJRBeanCollectionDataSource(JRBeanCollectionDataSource listCollection) {
        JasperCollectionData = listCollection;
    }

    public void setJasperPrint(JasperPrint jasperPrintData) {
        reportPrint = jasperPrintData;
    }

    public void setReportName(String reportName) {
        psReportName = reportName;
    }

    public void setResultSet(ResultSet rs) {
        this.loRSRecord = rs;
        pbUseDirectRS = true; // Direct RS mode
    }

    public void addParameter(String keyParameter, Object value) {
        poParamater.put(keyParameter, value);
    }

    public void isAlwaysTop(boolean value) {
        pbisAlwaysTop = value;
    }

    public void setReportListener(ReportUtilListener listener) {
        this.plReportListener = listener;
    }

    public String getMessage() {
        return this.psMessage;
    }

    public JSONObject generateReport() throws SQLException, JRException {
        poJSON = processReport();
        if (!"error".equals((String) poJSON.get("result")) && reportPrint != null) {

            if (pbWillExport) {
                poJSON = exportReportAuto();
            }
            if (pbWithUI) {
                poJSON = new JSONObject();
                poJSON = showUI();

            } else {
                //use standard of jasper
                JasperViewer jv = new JasperViewer(reportPrint, false);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
                Rectangle screenBounds = defaultScreen.getDefaultConfiguration().getBounds();
                Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(defaultScreen.getDefaultConfiguration());
                int adjustedHeight = screenBounds.height - screenInsets.bottom;
                Rectangle adjustedBounds = new Rectangle(screenBounds.x, screenBounds.y, screenBounds.width, adjustedHeight);
                jv.setBounds(adjustedBounds);
                jv.setVisible(true);
                jv.setAlwaysOnTop(pbisAlwaysTop);
            }
        }
        return poJSON;
    }

    public JSONObject processReport() throws SQLException, JRException {
        poJSON = new JSONObject();

        System.out.println("Jasper Report Path:" + poGRider.getReportPath() + psJasperPath + ".jasper");

        if (reportPrint != null) {
            poJSON.put("result", "success");
            poJSON.put("message", "Report generated.");
            return poJSON;

        }
        //collection
        if (JasperCollectionData != null) {
            reportPrint = JasperFillManager.fillReport(poGRider.getReportPath() + psJasperPath + ".jasper",
                    poParamater,
                    JasperCollectionData);

            if (reportPrint != null) {
                poJSON.put("result", "success");
                poJSON.put("message", "Report generated.");
                return poJSON;
            }

            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Data Detected");
            return poJSON;
        }

        //ResultSetData
        System.out.println("is Direct Result Set = " + pbUseDirectRS);
        if (!pbUseDirectRS) {

            System.out.println("is SQL Record is Empty = " + psSQLRecord.isEmpty());
            if (!psSQLRecord.isEmpty()) {
                loRSRecord = poGRider.executeQuery(psSQLRecord);
                System.out.println("Jasper Report Query: " + psSQLRecord);

            }

            System.out.println("ResultSet Count = " + MiscUtil.RecordCount(loRSRecord));
            if (MiscUtil.RecordCount(loRSRecord) <= 0) {
                poJSON.put("result", "error");
                poJSON.put("message", "No record to print.");
                return poJSON;
            }

            //initial to avoid skipping record's
            loRSRecord.beforeFirst();
            JasperResultResultSetData = new JRResultSetDataSource(loRSRecord);

            reportPrint = JasperFillManager.fillReport(poGRider.getReportPath() + psJasperPath + ".jasper",
                    poParamater,
                    JasperResultResultSetData);

            if (reportPrint != null) {
                poJSON.put("result", "success");
                poJSON.put("message", "Report generated.");
                return poJSON;
            }

            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Data Detected");
            return poJSON;
        }

        poJSON.put("result", "error");
        poJSON.put("message", "Invalid Data Detected. Data is not found");
        return poJSON;
    }

    private JSONObject exportReportAuto() {
        poJSON = new JSONObject();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(psReportName);

            // Get metadata from ResultSet
            ResultSetMetaData metaData = loRSRecord.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Create header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columnCount; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(metaData.getColumnLabel(i + 1));
                cell.setCellStyle(getHeaderCellStyle(workbook)); // Custom header style method
            }
            headerRow.setHeightInPoints(20);

            // Style for doubles
            CellStyle doubleStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            doubleStyle.setDataFormat(format.getFormat("#,##0.00"));

            // Populate data rows
            int rowIndex = 1;
            loRSRecord.beforeFirst();
            while (loRSRecord.next()) {
                Row row = sheet.createRow(rowIndex++);
                for (int col = 0; col < columnCount; col++) {
                    Object value = loRSRecord.getObject(col + 1);
                    Cell cell = row.createCell(col);

                    if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                        cell.setCellStyle(doubleStyle);
                    } else if (value != null) {
                        cell.setCellValue(value.toString());
                    }
                }
            }

            // Auto-size and adjust width
            for (int i = 0; i < columnCount; i++) {
                sheet.autoSizeColumn(i);
                int currentWidth = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, currentWidth + 1000);
            }

            // Ensure directory exists
            File directory = new File(poGRider.getApplPath() + "\\excel export\\");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Generate unique file name if needed
            String fileFullPath = poGRider.getApplPath() + "\\excel export\\" + psReportName + ".xlsx";
            File file = new File(fileFullPath);
            int count = 1;
            while (file.exists()) {
                String baseName = psReportName.contains(".")
                        ? psReportName.substring(0, psReportName.lastIndexOf("."))
                        : psReportName;
                String extension = psReportName.contains(".")
                        ? psReportName.substring(psReportName.lastIndexOf("."))
                        : "";
                fileFullPath = poGRider.getApplPath() + "\\excel export\\" + baseName + "-" + count + extension + ".xlsx";
                file = new File(fileFullPath);
                count++;
            }

            // Write file
            try (FileOutputStream fileOut = new FileOutputStream(fileFullPath)) {
                workbook.write(fileOut);
            }

            poJSON.put("result", "success");
            poJSON.put("message", "Exported to Excel successfully: " + fileFullPath);

        } catch (SQLException | IOException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
            e.printStackTrace();
        }

        return poJSON;
    }

    public JSONObject exportReportbyExcel() {
        poJSON = new JSONObject();
        if (reportStage == null) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Stage detected");
            return poJSON;
        }
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(psReportName);

            // Get metadata from ResultSet
            ResultSetMetaData metaData = loRSRecord.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Create header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columnCount; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(metaData.getColumnLabel(i + 1));
                cell.setCellStyle(getHeaderCellStyle(workbook));
            }
            headerRow.setHeightInPoints(20);

            // Style for doubles
            CellStyle doubleStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            doubleStyle.setDataFormat(format.getFormat("#,##0.00"));

            // Populate data rows
            int rowIndex = 1;
            loRSRecord.beforeFirst();
            while (loRSRecord.next()) {
                Row row = sheet.createRow(rowIndex++);
                for (int col = 0; col < columnCount; col++) {
                    Object value = loRSRecord.getObject(col + 1);
                    Cell cell = row.createCell(col);

                    if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                        cell.setCellStyle(doubleStyle);
                    } else if (value != null) {
                        cell.setCellValue(value.toString());
                    }
                }
            }

            // Auto-size and adjust width
            for (int i = 0; i < columnCount; i++) {
                sheet.autoSizeColumn(i);
                int currentWidth = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, currentWidth + 1000);
            }

            // Let user choose save location
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Report As Excel");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
            fileChooser.setInitialFileName(psReportName + ".xlsx");

            File file = fileChooser.showSaveDialog(reportStage);
            if (file == null) {
                poJSON.put("result", "success");
                return poJSON;
            }

            // Ensure .xlsx extension
            String fileFullPath = file.getAbsolutePath();
            if (!fileFullPath.toLowerCase().endsWith(".xlsx")) {
                fileFullPath += ".xlsx";
            }

            // Write file
            try (FileOutputStream fileOut = new FileOutputStream(fileFullPath)) {
                workbook.write(fileOut);
            }
            System.out.println("Exported to Excel successfully: " + fileFullPath);
            poJSON = new JSONObject();
            poJSON.put("result", "success");
            poJSON.put("message", "Exported to Excel successfully!");

        } catch (SQLException | IOException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
            e.printStackTrace();
        }

        return poJSON;
    }

    private static CellStyle getHeaderCellStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();

        // Set background color
        headerStyle.setFillForegroundColor(IndexedColors.OLIVE_GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 12);
        headerStyle.setFont(font);

        // Set center alignment
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        // Set borders for the header cells
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setTopBorderColor(IndexedColors.WHITE.getIndex()); // Set top border color to black
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBottomBorderColor(IndexedColors.WHITE.getIndex()); // Set bottom border color to black
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setLeftBorderColor(IndexedColors.WHITE.getIndex()); // Set left border color to black
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setRightBorderColor(IndexedColors.WHITE.getIndex()); // Set right border color to black

        return headerStyle;
    }

    private JSONObject showUI() throws JRException, SQLException {
        URL fxURLResource = getClass().getResource(
                "/ph/com/guanzongroup/cas/inv/warehouse/t4/report/view/ReportUtilView.fxml"
        );
        FXMLLoader fxmlLoader = new FXMLLoader(fxURLResource);
        fxmlLoader.setLocation(fxURLResource);

        ReportUtilViewController loController = new ReportUtilViewController();
        loController.setApplicationDriver(poGRider);
        loController.setJasperPath(psJasperPath);
        loController.setJasperPrint(reportPrint);
        loController.setReportName(psReportName);
        loController.isWithExport(pbisWithExport);
        loController.isWithExportPDF(pbisWithExportPDF);

        if (plReportListener != null) {
            loController.setReportListener(plReportListener);
        }

        try {
            fxmlLoader.setController(loController);
            Parent parent = fxmlLoader.load();
            reportStage = new Stage();

            /* SET FORM MOVABLE */
            parent.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });
            parent.setOnMouseDragged(event -> {
                reportStage.setX(event.getScreenX() - xOffset);
                reportStage.setY(event.getScreenY() - yOffset);
            });
            /* END SET FORM MOVABLE */

            Scene scene = new Scene(parent);

            // Get screen size and set stage to 75% of it
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            reportStage.setWidth(screenBounds.getWidth() * 0.75);
            reportStage.setHeight(screenBounds.getHeight() * 0.75);

            // Center stage
            reportStage.setX((screenBounds.getWidth() - reportStage.getWidth()) / 2);
            reportStage.setY((screenBounds.getHeight() - reportStage.getHeight()) / 2);

            scene.setFill(Color.TRANSPARENT);
            reportStage.initModality(Modality.APPLICATION_MODAL);
            reportStage.initStyle(StageStyle.TRANSPARENT);
            reportStage.setAlwaysOnTop(pbisAlwaysTop);
            reportStage.setScene(scene);
            reportStage.showAndWait();

        } catch (IOException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
            e.printStackTrace();
            return poJSON;
        }
        if (!loController.isCancelled()) {
            poJSON.put("result", "success");
//            poJSON.put("message", "UI Displayed!");
            return poJSON;
        }
        

        return loController.getMessage();
    }
}
