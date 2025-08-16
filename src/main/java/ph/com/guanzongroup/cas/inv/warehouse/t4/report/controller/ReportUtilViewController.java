package ph.com.guanzongroup.cas.inv.warehouse.t4.report.controller;

import java.awt.print.PrinterJob;
import java.io.File;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javax.swing.JButton;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.swing.JRViewer;
import net.sf.jasperreports.swing.JRViewerToolbar;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.inv.warehouse.t4.report.ReportUtilListener;

/**
 * FXML Controller class
 *
 * @author Maynard
 */
public class ReportUtilViewController implements Initializable {

    private GRiderCAS poGRider;
    private Map<String, Object> poParamater = new HashMap<>();
    private String psJasperPath = "";
    private String psSQLRecord = "";
    private String psReportName = "";

    private ResultSet loRSRecord;

    private JSONObject poJSON;

    private boolean pbisWithExportPDF = false;
    private boolean pbisWithExport = false;
    private boolean pbisCancelled = false;

    private ReportUtilListener plReportListener;
    private JRViewer jrViewer = null;
    private JasperPrint psjpReport = null;

    @FXML
    private AnchorPane apMainReport, apReport;
    @FXML
    private Text lblCaption;
    @FXML
    private Button btnPrint, btnExport, btnClose, btnExit, btnExportPDF;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        poJSON = new JSONObject();
        lblCaption.setText(lblCaption.getText() + " (" + psReportName + ")");
        initButton();
        try {
            generateReport();
        } catch (JRException | SQLException ex) {
            Logger.getLogger(ReportUtilViewController.class.getName()).log(Level.SEVERE, null, ex);

            poJSON.put("result", "error");
            poJSON.put("message", "Report is Generating! Encountered Error !" + ex.getMessage());
            CommonUtils.closeStage(btnExit);
        }

    }

    private void initButton() {
        btnExport.setVisible(pbisWithExport);
        btnExport.setManaged(pbisWithExport);
        btnExportPDF.setVisible(pbisWithExportPDF);
        btnExportPDF.setManaged(pbisWithExportPDF);
        btnPrint.setManaged(true);
        btnClose.setManaged(true);
    }

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        String lsButton = ((Button) event.getSource()).getId();
        switch (lsButton) {

            case "btnPrint":
                if (!isJSONSuccess(printRecord(), "Initialize printing! ")) {
                    return;
                }

                if (plReportListener != null) {
                    plReportListener.onReportPrint();
                }
                break;

            case "btnExport":
                if (plReportListener != null) {
                    plReportListener.onReportExport();
                }
                break;
            case "btnExportPDF":
                if (!isJSONSuccess(exportByPDF(), "Initialize export PDF! ")) {
                    return;
                }

                if (plReportListener != null) {
                    plReportListener.onReportExportPDF();
                }
                break;
            case "btnClose":
            case "btnExit":
                if (plReportListener != null) {
                    plReportListener.onReportClose();
                }
                poJSON = new JSONObject();
                poJSON.put("result", "success");
                CommonUtils.closeStage(btnExit);
                break;
        }

    }

    public void setApplicationDriver(Object applicationDriver) {
        poGRider = (GRiderCAS) applicationDriver;
    }

    public void setJasperPath(String jasperPath) {
        psJasperPath = jasperPath;
    }

    public void setReportName(String reportName) {
        psReportName = reportName;
    }

    public void isWithExport(boolean iswithExport) {
        pbisWithExport = iswithExport;
    }

    public void isWithExportPDF(boolean iswithExport) {
        pbisWithExportPDF = iswithExport;
    }

    public JSONObject getMessage() {
        return poJSON;
    }

    public void setJasperPrint(JasperPrint jasperPrint) {
        this.psjpReport = jasperPrint;
    }

    public void setParameter(Map<String, Object> value) {
        poParamater = value;
    }

    public void addParameter(String keyParameter, Object value) {
        poParamater.put(keyParameter, value);
    }

    public boolean isCancelled() {
        return pbisCancelled;
    }

    public void setReportListener(ReportUtilListener listener) {
        this.plReportListener = listener;
    }

    public JSONObject generateReport() throws JRException, SQLException {

        hideReport();
        StackPane overlay = getOverlayProgress(apReport);
        ProgressIndicator pi = (ProgressIndicator) overlay.getChildren().get(0);
        overlay.setVisible(true);
        pi.setVisible(true);

        Task<Void> clusterDeliveryTask = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                LoadReport();
                return null;
            }

            @Override
            protected void succeeded() {
                overlay.setVisible(false);
                pi.setVisible(false);
            }

            @Override
            protected void failed() {
                overlay.setVisible(false);
                pi.setVisible(false);
            }

            @Override
            protected void cancelled() {
                overlay.setVisible(false);
                pi.setVisible(false);
            }
        };

        Thread thread = new Thread(clusterDeliveryTask);
        thread.setDaemon(true);
        thread.start();

        poJSON.put("result", "success");
        poJSON.put("message", "Report is Generating");
        return poJSON;
    }

    private JSONObject LoadReport() throws SQLException, JRException {

        String printFileName = psjpReport.toString();
        if (printFileName != null) {
            return showReport();
        } else {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid JasperPrint. Please inform MIS.");
            return poJSON;

        }

    }

    private JSONObject showReport() {
        poJSON = new JSONObject();
        if (plReportListener != null) {
            plReportListener.onReportOpen();
        }

        jrViewer = new JRViewer(psjpReport);

        // Remove Print and Save buttons from JRViewer toolbar
        for (int lnComponent = 0; lnComponent < jrViewer.getComponentCount(); lnComponent++) {
            if (jrViewer.getComponent(lnComponent) instanceof JRViewerToolbar) {
                JRViewerToolbar toolbar = (JRViewerToolbar) jrViewer.getComponent(lnComponent);

                for (int lnToolBar = 0; lnToolBar < toolbar.getComponentCount(); lnToolBar++) {
                    if (toolbar.getComponent(lnToolBar) instanceof JButton) {
                        JButton button = (JButton) toolbar.getComponent(lnToolBar);

                        if (button.getToolTipText() != null) {
                            if (button.getToolTipText().equals("Save") || button.getToolTipText().equals("Print")) {
                                button.setEnabled(false);
                                button.setVisible(false);
                            }
                        }
                    }
                }

                toolbar.revalidate();
                toolbar.repaint();
            }
        }
        SwingNode swingNode = new SwingNode();
        javafx.application.Platform.runLater(() -> {
            swingNode.setContent(jrViewer);

            AnchorPane.setTopAnchor(swingNode, 0.0);
            AnchorPane.setBottomAnchor(swingNode, 0.0);
            AnchorPane.setLeftAnchor(swingNode, 0.0);
            AnchorPane.setRightAnchor(swingNode, 0.0);

            apReport.getChildren().add(swingNode);
            apReport.setVisible(true);
        });

        poJSON.put("result", "success");
        poJSON.put("message", "Report generated.");
        return poJSON;
    }

    private JSONObject printRecord() {
        poJSON = new JSONObject();
        try {
            if (psjpReport == null) {
                poJSON.put("result", "error");
                poJSON.put("message", "Invalid Jasper Print Detected.");

            }
            PrinterJob job = PrinterJob.getPrinterJob();
            if (job.printDialog()) {
                JasperPrintManager.printReport(psjpReport, false);
                System.out.println("Print successfully sent to printer.");
                poJSON.put("result", "success");
                return poJSON;
            } else {
                System.out.println("Print is canceled by user.");
                poJSON.put("result", "error");
                return poJSON;
            }
        } catch (JRException ex) {
            Logger.getLogger(ReportUtilViewController.class.getName()).log(Level.SEVERE, null, ex);
            poJSON.put("result", "error");
            poJSON.put("message", ex.getMessage());
            return poJSON;
        }
    }

    private JSONObject exportByPDF() {
        poJSON = new JSONObject();

        if (psjpReport == null) {
            poJSON.put("result", "error");
            poJSON.put("message", "Report is null");
            return poJSON;
        }

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Report as PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );
            fileChooser.setInitialFileName(psReportName + ".pdf");

            File file = fileChooser.showSaveDialog(this.apMainReport.getScene().getWindow());
            if (file != null) {
                JasperExportManager.exportReportToPdfFile(psjpReport, file.getAbsolutePath());
                System.out.println("PDF exported successfully to: " + file.getAbsolutePath());
                poJSON.put("result", "success");
                poJSON.put("message", "Report is Exported to PDF");

                return poJSON;
            } else {
                poJSON = new JSONObject();
                System.out.println("Export canceled by user.");
                poJSON.put("result", "error");

                return poJSON;
            }
        } catch (Exception e) {
            e.printStackTrace();

            poJSON = new JSONObject();
            System.out.println("Error exporting PDF: " + e.getMessage());
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
            return poJSON;
        }

    }

    private void hideReport() {
        jrViewer = new JRViewer(null);
        apReport.getChildren().clear();
        jrViewer.setVisible(false);
        apReport.setVisible(false);
    }

    private StackPane getOverlayProgress(AnchorPane foAnchorPane) {
        ProgressIndicator localIndicator = null;
        StackPane localOverlay = null;

        // Check if overlay already exists
        for (Node node : foAnchorPane.getChildren()) {
            if (node instanceof StackPane) {
                StackPane stack = (StackPane) node;
                for (Node child : stack.getChildren()) {
                    if (child instanceof ProgressIndicator) {
                        localIndicator = (ProgressIndicator) child;
                        localOverlay = stack;
                        break;
                    }
                }
            }
        }

        if (localIndicator == null) {
            localIndicator = new ProgressIndicator();
            localIndicator.setMaxSize(50, 50);
            localIndicator.setVisible(false);
            localIndicator.setStyle("-fx-progress-color: orange;");
        }

        if (localOverlay == null) {
            localOverlay = new StackPane();
            localOverlay.setPickOnBounds(false); // Let clicks through
            localOverlay.getChildren().add(localIndicator);

            AnchorPane.setTopAnchor(localOverlay, 0.0);
            AnchorPane.setBottomAnchor(localOverlay, 0.0);
            AnchorPane.setLeftAnchor(localOverlay, 0.0);
            AnchorPane.setRightAnchor(localOverlay, 0.0);

            foAnchorPane.getChildren().add(localOverlay);
        }

        return localOverlay;
    }

    private boolean isJSONSuccess(JSONObject loJSON, String fsModule) {
        String result = (String) loJSON.get("result");
        if ("error".equals(result)) {
            String message = (String) loJSON.get("message");
            Platform.runLater(() -> {
                if (message != null) {
                    ShowMessageFX.Warning(null, psReportName, fsModule + ": " + message);
                }
            });
            return false;
        }
        String message = (String) loJSON.get("message");

        Platform.runLater(() -> {
            if (message != null) {
                ShowMessageFX.Information(null, psReportName, fsModule + ": " + message);
            }
        });
        return true;

    }
}
