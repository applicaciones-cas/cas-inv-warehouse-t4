package ph.com.guanzongroup.cas.inv.warehouse.t4.model;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.client.model.Model_Client_Master;
import org.guanzon.cas.client.services.ClientModels;
import org.guanzon.cas.parameter.model.Model_Branch;
import org.guanzon.cas.parameter.model.Model_Category;
import org.guanzon.cas.parameter.model.Model_Company;
import org.guanzon.cas.parameter.model.Model_Industry;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.inv.warehouse.t4.constant.DeliveryScheduleStatus;

/**
 *
 * @author maynevval 08-09-2025
 */
public class Model_Inventory_Transfer_Master extends Model {

    //reference objects
    Model_Industry poIndustry;
    Model_Company poCompany;
    Model_Branch poBranch;
    Model_Branch poBranchDestination;
    Model_Category poCategory;
    Model_Client_Master poTrucking;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            poEntity.updateObject("dTransact", poGRider.getServerDate());
            poEntity.updateDouble("nFreightx", 0.00d);
            poEntity.updateDouble("nTranTotl", 0.00d);
            poEntity.updateDouble("nDiscount", 0.00d);
            poEntity.updateObject("nEntryNox", 0);
            poEntity.updateNull("dReceived");
            poEntity.updateNull("sApproved");
            poEntity.updateNull("sApprvCde");
            poEntity.updateNull("sOrderNox");
            poEntity.updateString("cStockNew", "1");
            poEntity.updateString("cDelivrTp", "0");
            poEntity.updateString("cPrintedx", "0");
            poEntity.updateObject("dModified", poGRider.getServerDate());
            poEntity.updateString("cTranStat", DeliveryScheduleStatus.OPEN);

            this.poTrucking = (new ClientModels(this.poGRider)).ClientMaster();
            this.poBranchDestination = (new ParamModels(this.poGRider)).Branch();
            this.poBranch = (new ParamModels(this.poGRider)).Branch();
            this.poCompany = (new ParamModels(this.poGRider)).Company();
            this.poIndustry = (new ParamModels(this.poGRider)).Industry();
            this.poCategory = (new ParamModels(this.poGRider)).Category();

            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            ID = poEntity.getMetaData().getColumnLabel(1);

            //add model here
            pnEditMode = EditMode.UNKNOWN;

        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }
    //Getter & Setter 
    //sTransNox
    //sIndstCdx
    //sCompnyID
    //sBranchCd*
    //sCategrCd
    //dTransact*
    //sDestinat*
    //sRemarksx
    //sTruckIDx
    //nFreightx
    //sReceived
    //dReceived
    //sApproved
    //sApprvCde
    //nTranTotl
    //nDiscount
    //nEntryNox
    //sOrderNox
    //cStockNew
    //cDelivrTp
    //cTranStat

    //sTransNox
    public JSONObject setTransactionNo(String transactionNo) {
        return setValue("sTransNox", transactionNo);
    }

    public String getTransactionNo() {
        return (String) getValue("sTransNox");
    }

    //sIndstCdx
    public JSONObject setIndustryId(String industryId) {
        return setValue("sIndstCdx", industryId);
    }

    public String getIndustryId() {
        return (String) getValue("sIndstCdx");
    }

    //sCompnyID
    public JSONObject setCompanyID(String companyID) {
        return setValue("sCompnyID", companyID);
    }

    public String getCompanyID() {
        return (String) getValue("sCompnyID");
    }

    //sBranchCd
    public JSONObject setBranchCode(String branchCode) {
        return setValue("sBranchCd", branchCode);
    }

    public String getBranchCode() {
        return (String) getValue("sBranchCd");
    }

    //sCategrCd
    public JSONObject setCategoryId(String categoryId) {
        return setValue("sCategrCd", categoryId);
    }

    public String getCategoryId() {
        return (String) getValue("sCategrCd");
    }

    //dTransact
    public JSONObject setTransactionDate(Date transactionDate) {
        return setValue("dTransact", transactionDate);
    }

    public Date getTransactionDate() {
        return (Date) getValue("dTransact");
    }

    //sDestinat
    public JSONObject setDestination(String destination) {
        return setValue("sDestinat", destination);
    }

    public String getDestination() {
        return (String) getValue("sDestinat");
    }

    //sRemarksx
    public JSONObject setRemarks(String remarks) {
        return setValue("sRemarksx", remarks);
    }

    public String getRemarks() {
        return (String) getValue("sRemarksx");
    }

    //sTruckIDx
    public JSONObject setTruckId(String truckId) {
        return setValue("sTruckIDx", truckId);
    }

    public String getTruckId() {
        return (String) getValue("sTruckIDx");
    }

    //nFreightx
    public JSONObject setFreight(Double freight) {
        return setValue("nFreightx", freight);
    }

    public Double getFreight() {
        return Double.valueOf(getValue("nFreightx").toString());
    }

    //sReceived
    public JSONObject setReceivedBy(String receivedBy) {
        return setValue("sReceived", receivedBy);
    }

    public String getReceivedBy() {
        return (String) getValue("sReceived");
    }

    //dReceived
    public JSONObject setReceivedDate(LocalDateTime  receivedDate) {
        return setValue("dReceived", Timestamp.valueOf(receivedDate));
    }

    public Date getReceivedDate() {
        return (Date) getValue("dReceived");
    }

    //sApproved
    public JSONObject setApprovedBy(String approvedBy) {
        return setValue("sApproved", approvedBy);
    }

    public String getApprovedBy() {
        return (String) getValue("sApproved");
    }

    //sApprvCde
    public JSONObject setApprovalCode(String approvedCode) {
        return setValue("sApprvCde", approvedCode);
    }

    public String getApprovalCode() {
        return (String) getValue("sApprvCde");
    }

    //nTranTotl
    public JSONObject setTransactionTotal(Double transactionTotal) {
        return setValue("nTranTotl", transactionTotal);
    }

    public Double getTransactionTotal() {
        return Double.valueOf(getValue("nTranTotl").toString());
    }

    //nDiscount
    public JSONObject setDiscount(Double discountrate) {
        return setValue("nDiscount", discountrate);
    }

    public Double getDiscount() {
        return Double.valueOf(getValue("nDiscount").toString());
    }

    //nEntryNox
    public JSONObject setEntryNo(int entryNo) {
        return setValue("nEntryNox", entryNo);
    }

    public int getEntryNo() {
        return (int) getValue("nEntryNox");
    }

    //sApprvCde
    public JSONObject setOrderNo(String orderNo) {
        return setValue("sOrderNox", orderNo);
    }

    public String getOrderNo() {
        return (String) getValue("sOrderNox");
    }

    //cStockNew
    public JSONObject setStockNew(String stockNew) {
        return setValue("cStockNew", stockNew);
    }

    public String getStockNew() {
        return (String) getValue("cStockNew");
    }

    //cDelivrTp
    public JSONObject setDeliveryType(String orderNo) {
        return setValue("cDelivrTp", orderNo);
    }

    public String getDeliveryType() {
        return (String) getValue("cDelivrTp");
    }

    //cPrintedx
    public JSONObject setPrintStatus(String printStatus) {
        return setValue("cPrintedx", printStatus);
    }

    public String getPrintStatus() {
        return (String) getValue("cPrintedx");
    }
    
     public boolean isPrintedStatus() {
        return RecordStatus.ACTIVE.equals(getValue("cPrintedx"));
    }

    //cTranStat
    public JSONObject setTransactionStatus(String transactionStatus) {
        return setValue("cTranStat", transactionStatus);
    }

    public String getTransactionStatus() {
        return (String) getValue("cTranStat");
    }

    //sModified
    public JSONObject setModifyingId(String modifyingId) {
        return setValue("sModified", modifyingId);
    }

    public String getModifyingId() {
        return (String) getValue("sModified");
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

    public Model_Client_Master TruckingCompany() throws SQLException, GuanzonException {
        if (!"".equals(getValue("sTruckIDx"))) {
            if (this.poTrucking.getEditMode() == 1 && this.poTrucking
                    .getClientId().equals(getValue("sTruckIDx"))) {
                return this.poTrucking;
            }
            this.poJSON = this.poTrucking.openRecord((String) getValue("sTruckIDx"));
            if ("success".equals(this.poJSON.get("result"))) {
                return this.poTrucking;
            }
            this.poTrucking.initialize();
            return this.poTrucking;
        }
        this.poTrucking.initialize();
        return this.poTrucking;
    }

    public Model_Branch BranchDestination() throws SQLException, GuanzonException {
        if (!"".equals(getValue("sDestinat"))) {
            if (this.poBranchDestination.getEditMode() == 1 && this.poBranchDestination
                    .getBranchCode().equals(getValue("sDestinat"))) {
                return this.poBranchDestination;
            }
            this.poJSON = this.poBranchDestination.openRecord((String) getValue("sDestinat"));
            if ("success".equals(this.poJSON.get("result"))) {
                return this.poBranchDestination;
            }
            this.poBranchDestination.initialize();
            return this.poBranchDestination;
        }
        this.poBranchDestination.initialize();
        return this.poBranchDestination;
    }

    public Model_Category Category() throws SQLException, GuanzonException {
        if (!"".equals(getValue("sCategrCd"))) {
            if (this.poCategory.getEditMode() == 1 && this.poCategory
                    .getCategoryId().equals(getValue("sCategrCd"))) {
                return this.poCategory;
            }
            this.poJSON = this.poCategory.openRecord((String) getValue("sCategrCd"));
            if ("success".equals(this.poJSON.get("result"))) {
                return this.poCategory;
            }
            this.poCategory.initialize();
            return this.poCategory;
        }
        this.poCategory.initialize();
        return this.poCategory;
    }

    public Model_Branch Branch() throws SQLException, GuanzonException {
        if (!"".equals(getValue("sBranchCd"))) {
            if (this.poBranch.getEditMode() == 1 && this.poBranch
                    .getBranchCode().equals(getValue("sBranchCd"))) {
                return this.poBranch;
            }
            this.poJSON = this.poBranch.openRecord((String) getValue("sBranchCd"));
            if ("success".equals(this.poJSON.get("result"))) {
                return this.poBranch;
            }
            this.poBranch.initialize();
            return this.poBranch;
        }
        this.poBranch.initialize();
        return this.poBranch;
    }

    public Model_Company Company() throws SQLException, GuanzonException {
        if (!"".equals(getValue("sCompnyID"))) {
            if (this.poCompany.getEditMode() == 1 && this.poCompany
                    .getCompanyId().equals(getValue("sCompnyID"))) {
                return this.poCompany;
            }
            this.poJSON = this.poCompany.openRecord((String) getValue("sCompnyID"));
            if ("success".equals(this.poJSON.get("result"))) {
                return this.poCompany;
            }
            this.poCompany.initialize();
            return this.poCompany;
        }
        this.poCompany.initialize();
        return this.poCompany;
    }

    public Model_Industry Industry() throws SQLException, GuanzonException {
        if (!"".equals(getValue("sIndstCdx"))) {
            if (this.poIndustry.getEditMode() == 1 && this.poIndustry
                    .getIndustryId().equals(getValue("sIndstCdx"))) {
                return this.poIndustry;
            }
            this.poJSON = this.poIndustry.openRecord((String) getValue("sIndstCdx"));
            if ("success".equals(this.poJSON.get("result"))) {
                return this.poIndustry;
            }
            this.poIndustry.initialize();
            return this.poIndustry;
        }
        this.poIndustry.initialize();
        return this.poIndustry;
    }

}
