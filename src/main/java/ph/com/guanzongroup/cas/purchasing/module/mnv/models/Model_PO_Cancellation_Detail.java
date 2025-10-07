package ph.com.guanzongroup.cas.purchasing.module.mnv.models;

import ph.com.guanzongroup.cas.check.module.mnv.models.*;
import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.purchasing.model.Model_PO_Detail;
import org.guanzon.cas.purchasing.model.Model_PO_Master;
import org.guanzon.cas.purchasing.services.PurchaseOrderModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Payments;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;

/**
 *
 * @author maynevval 08-09-2025
 */
public class Model_PO_Cancellation_Detail extends Model {

    Model_PO_Detail poPurchaseOrder;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            poEntity.updateObject("nEntryNox", 1);
            poEntity.updateNull("sOrderNox");
            poEntity.updateDouble("nQuantity", 0.00d);
            poEntity.updateDouble("nUnitPrce", 0.00d);
            poEntity.updateObject("dModified", poGRider.getServerDate());

            ID = poEntity.getMetaData().getColumnLabel(1);
            ID2 = poEntity.getMetaData().getColumnLabel(2);

            poPurchaseOrder = new PurchaseOrderModels(poGRider).PurchaseOrderDetails();

            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }
    //Getter & Setter 
    //sTransNox
    //nEntryNox*
    //sOrderNox*
    //sStockIDx*
    //nQuantity*
    //nUnitPrce*
    //dModified*

    //sTransNox
    public JSONObject setTransactionNo(String transactionNo) {
        return setValue("sTransNox", transactionNo);
    }

    public String getTransactionNo() {
        return (String) getValue("sTransNox");
    }

    //nEntryNox
    public JSONObject setEntryNo(int entryNo) {
        return setValue("nEntryNox", entryNo);
    }

    public int getEntryNo() {
        return (int) getValue("nEntryNox");
    }

    //sOrderNox
    public JSONObject setOrderNo(String orderNo) {
        return setValue("sOrderNox", orderNo);
    }

    public String getOrderNo() {
        return (String) getValue("sOrderNox");
    }

    //sStockIDx
    public JSONObject setStockId(String stockId) {
        return setValue("sStockIDx", stockId);
    }

    public String getStockId() {
        return (String) getValue("sStockIDx");
    }

    //nQuantity
    public JSONObject setQuantity(Double quantity) {
        return setValue("nQuantity", quantity);
    }

    public Double getQuantity() {
        return Double.valueOf(getValue("nQuantity").toString());
    }

    //nUnitPrce
    public JSONObject setUnitPrice(Double unitPrice) {
        return setValue("nUnitPrce", unitPrice);
    }

    public Double getUnitPrice() {
        return Double.valueOf(getValue("nUnitPrce").toString());
    }

    //dModified
    public JSONObject setModifiedDate(Date modifiedDate) {
        return setValue("dModified", modifiedDate);
    }

    public Date getModifiedDate() {
        return (Date) getValue("dModified");
    }

    @Override
    public String getNextCode() {
        return "";
    }

    public Model_PO_Detail PurchaseOrderDetail() throws SQLException, GuanzonException {
        if (!"".equals(getValue("sOrderNox"))) {
            if (this.poPurchaseOrder.getEditMode() == 1 && this.poPurchaseOrder
                    .getTransactionNo().equals(getValue("sOrderNox"))) {
                return this.poPurchaseOrder;
            }
            this.poJSON = this.poPurchaseOrder.openRecord((String) getValue("sOrderNox"), (String) getValue("nEntryNox"));
            if ("success".equals(this.poJSON.get("result"))) {
                return this.poPurchaseOrder;
            }
            this.poPurchaseOrder.initialize();
            return this.poPurchaseOrder;
        }
        poPurchaseOrder.initialize();
        return this.poPurchaseOrder;
    }

}
