package ph.com.guanzongroup.cas.inv.warehouse.t4.report.view;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.swing.JRViewer;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONObject;

/**
 * FXML Controller class
 *
 * @author Maynard
 */
public class ReportUtilViewController implements Initializable {

    @FXML
    private AnchorPane apMainReport, apReport;
    @FXML
    private Text lblCaption;
    @FXML
    private Button btnPrint, btnExport, btnClose;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO

        lblCaption.setText(lblCaption.getText() + " (" + psReportName + ")");
    }

    @FXML
    private void cmdButton_click(ActionEvent event) {
    }

    public void setApplicationDriver(Object applicationDriver) {
        poGRider = (GRiderCAS) applicationDriver;
    }

    public void setJasperPath(String jasperPath) {
        psJasperPath = jasperPath;
    }

    public void isWithExport(boolean iswithExport) {
        pbisWithExport = iswithExport;
    }

    public void setSQLReport(String SQLRecord) {
        psSQLRecord = SQLRecord;
    }

    public void setResultSet(ResultSet rs) {
        this.loRSRecord = rs;
        pbUseDirectRS = true; // Direct RS mode
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
        if (!pbUseDirectRS) {
            loRSRecord = poGRider.executeQuery(psSQLRecord);
            System.out.println("Jasper Report Query: " + psSQLRecord);
        }
        if (loRSRecord == null) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid ResultSet");
            return poJSON;
        }

        if (MiscUtil.RecordCount(loRSRecord) <= 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Result Set");
            return poJSON;
        }

        //initial to avoid skipping record's
        loRSRecord.beforeFirst();
        JRResultSetDataSource record = new JRResultSetDataSource(loRSRecord);

        System.out.println("Jasper Report Path:" + poGRider.getReportPath() + psJasperPath);

        reportPrint = JasperFillManager.fillReport(poGRider.getReportPath() + psJasperPath + ".jrxml",
                poParamater,
                record);

        String printFileName = reportPrint.toString();
        if (printFileName != null) {
            return showReport();
        }
        poJSON.put("result", "success");
        poJSON.put("message", "Report generated.");
        return poJSON;

    }

    private JSONObject showReport() {

        jrViewer = new JRViewer(reportPrint);

        SwingNode swingNode = new SwingNode();
        jrViewer.setOpaque(true);
        jrViewer.setVisible(true);
        jrViewer.setFitPageZoomRatio();

        swingNode.setContent(jrViewer);
        swingNode.setVisible(true);
        apReport.setTopAnchor(swingNode, 0.0);
        apReport.setBottomAnchor(swingNode, 0.0);
        apReport.setLeftAnchor(swingNode, 0.0);
        apReport.setRightAnchor(swingNode, 0.0);
        apReport.getChildren().add(swingNode);
        apReport.setVisible(true);

        poJSON.put("result", "success");
        poJSON.put("message", "Report generated.");
        return poJSON;

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

    private GRiderCAS poGRider;
    private Map<String, Object> poParamater = new HashMap<>();
    private String psWatermarkPath = "";
    private String psJasperPath;
    private String psSQLRecord;
    private JasperPrint reportPrint = null;
    private ResultSet loRSRecord;
    private boolean pbisWithExport = false;
    private boolean pbisCancelled = false;
    private String psReportName = "";
    private JSONObject poJSON;
    private boolean pbUseDirectRS = false;
    private JRViewer jrViewer = null;

}
