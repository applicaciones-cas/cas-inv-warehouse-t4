/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.cas.check.module.mnv.models;

import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Payments;
import ph.com.guanzongroup.cas.cashflow.model.Model_Payee;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;

/**
 *
 * @author User
 */
public class Model_Check_Release_Detail extends Model{

    private Model_Check_Payments poCheckPayment;

    @Override
    public void initialize() {
        
        try{
            
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());
            poEntity.last();
            poEntity.moveToInsertRow();
            
            poEntity.updateNull("sSourceNo");
            poEntity.updateNull("sSourceCd");
            poEntity.updateObject("nEntryNox", 1);
            poEntity.updateObject("dModified", poGRider.getServerDate());

            MiscUtil.initRowSet(poEntity);
            
            CashflowModels cashFlow = new CashflowModels(poGRider);
            poCheckPayment = cashFlow.CheckPayments();
            
            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            ID = poEntity.getMetaData().getColumnLabel(1);
            ID2 = poEntity.getMetaData().getColumnLabel(2);

            //add model here
            pnEditMode = EditMode.UNKNOWN;
            
        }catch(SQLException e){
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }
    
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
    public JSONObject setSourceCD(String sSourceCd) {
        return setValue("sSourceCd", sSourceCd);
    }

    public String getSourceCD() {
        return (String) getValue("sSourceCd");
    }
    
    //sSourceNo
    public JSONObject setSourceNo(String sSourceNo) {
        return setValue("sSourceNo", sSourceNo);
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
        return MiscUtil.getNextCode(this.getTable(), ID, true, poGRider.getGConnection().getConnection(), poGRider.getBranchCode());
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
}
