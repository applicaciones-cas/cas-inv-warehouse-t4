package ph.com.guanzongroup.cas.check.module.mnv.models;

import ph.com.guanzongroup.cas.inv.warehouse.t4.model.*;
import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.inv.model.Model_Inv_Serial;
import org.guanzon.cas.inv.model.Model_Inventory;
import org.guanzon.cas.inv.services.InvModels;
import org.guanzon.cas.inv.warehouse.model.Model_Inv_Stock_Request_Detail;
import org.guanzon.cas.inv.warehouse.services.InvWarehouseModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Payments;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;

/**
 *
 * @author maynevval 08-09-2025
 */
public class Model_Check_Transfer_Detail extends Model {

    private Model_Check_Payments poCheckPayment;
//    private Model_Check_Receive poCheckReceive;

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
            poEntity.updateNull("sSourceNo");
            poEntity.updateNull("sSourceCd");
            ID = poEntity.getMetaData().getColumnLabel(1);
            ID2 = poEntity.getMetaData().getColumnLabel(2);

            poCheckPayment = new CashflowModels(poGRider).CheckPayments();
//            poCheckReceive =  new CashflowModels(poGRider).CheckReceive();

            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }
    //Getter & Setter 
    //sTransNox
    //nEntryNox*
    //sSourceCd*
    //sSourceNo*

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

    //sSourceCd
    public JSONObject setSourceCode(String sourceCd) {
        return setValue("sSourceCd", sourceCd);
    }

    public String getSourceCode() {
        return (String) getValue("sSourceCd");
    }

    //sSourceNo
    public JSONObject setSourceNo(String originalid) {
        return setValue("sSourceNo", originalid);
    }

    public String getSourceNo() {
        return (String) getValue("sSourceNo");
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

    public Model_Check_Payments CheckPayment() throws SQLException, GuanzonException {
        if (!"".equals(getValue("sSourceNo"))) {
            if (this.poCheckPayment.getEditMode() == 1 && this.poCheckPayment
                    .getTransactionNo().equals(getValue("sSourceNo"))) {
                return this.poCheckPayment;
            }
            this.poJSON = this.poCheckPayment.openRecord((String) getValue("sSourceNo"));
            if ("success".equals(this.poJSON.get("result"))) {
                return this.poCheckPayment;
            }
            this.poCheckPayment.initialize();
            return this.poCheckPayment;
        }
        poCheckPayment.initialize();
        return this.poCheckPayment;
    }
    
//    public Model_Check_Receive CheckPayment() throws SQLException, GuanzonException {
//        if (!"".equals(getValue("sSourceNo"))) {
//            if (this.poCheckReceive.getEditMode() == 1 && this.poCheckReceive
//                    .getTransactionNo().equals(getValue("sSourceNo"))) {
//                return this.poCheckReceive;
//            }
//            this.poJSON = this.poCheckReceive.openRecord((String) getValue("sSourceNo"));
//            if ("success".equals(this.poJSON.get("result"))) {
//                return this.poCheckReceive;
//            }
//            this.poCheckReceive.initialize();
//            return this.poCheckReceive;
//        }
//        poCheckReceive.initialize();
//        return this.poCheckReceive;
//    }

}
