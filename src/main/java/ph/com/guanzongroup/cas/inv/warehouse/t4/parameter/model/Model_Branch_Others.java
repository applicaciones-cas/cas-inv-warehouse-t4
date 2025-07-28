package ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.model;

import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.parameter.model.Model_Branch;
import org.guanzon.cas.parameter.model.Model_Brand;
import org.guanzon.cas.parameter.model.Model_Industry;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.services.DeliveryParamModels;

/**
 *
 * @author maynevval 07-26-2025
 */
public class Model_Branch_Others extends Model {

    private Model_Branch poBranch;
    private Model_Branch_Area poBranchArea;
    private Model_Brand poBrand;
    private Model_Industry poIndustry;
    //add dealer

    @Override
    public void initialize() {
        try {
            this.poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            this.poEntity.last();
            this.poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            this.poEntity.updateString("cAllowQMx", "0");
            this.poEntity.updateString("cAccredtd", "0");
            this.poEntity.updateString("cRealTime", "0");

            this.poEntity.insertRow();
            this.poEntity.moveToCurrentRow();

            this.poEntity.absolute(1);

            ID = this.poEntity.getMetaData().getColumnLabel(1);
            this.poBranch = (new ParamModels(this.poGRider)).Branch();
            this.poBrand = (new ParamModels(this.poGRider)).Brand();
            this.poIndustry = (new ParamModels(this.poGRider)).Industry();
            this.poBranchArea = (new DeliveryParamModels(this.poGRider)).BranchArea();
            this.pnEditMode = -1;
            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            this.logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }
    //Getter & Setter 
    //sBranchCD
    //xBranchCD
    //tBranchCD
    //sAreaCode
    //sClustrID
    //dOpenedxx*
    //cDivision*
    //cShopType
    //sDealerCd
    //sBrandIDx
    //cAccredtd
    //sSuppCode
    //sRegionID
    //nSuppDsc1
    //sPyClustr
    //sPayBankx
    //dPayrollx
    //dSalesInv
    //dReceiptx
    //sSalesInv
    //sReceiptx
    //sLenderOR
    //dLenderOR
    //sHostName
    //sHostIPAd
    //sSlvIPAdd
    //sUserName
    //sPassword
    //sDBHostNm
    //sDBIPAddr
    //dRenovatd
    //dCntrtBeg
    //dCntrtEnd
    //nRentlBeg
    //nRentlCur
    //nMCCapcty
    //cRealTime
    //dIssuedSI
    //cAllowQMx
    //dCRInvBeg
    //dOpenD2Dx
    //sHAssgnID
    //sSAssgnID
    //sCAssgnID
    //cAllowRbt
    //cAllowGwy
    //cPayDivCd
    //cPromoDiv
    //cPOSStart
    //nMaxRebte
    //dUnEncode
    //sTimeOpen
    //sOpenRcpt

    //sBranchCD
    public JSONObject setBranchCode(String branchCode) {
        return setValue("sBranchCD", branchCode);
    }

    public String getBranchCode() {
        return (String) getValue("sBranchCD");
    }

    //xBranchCD
    public JSONObject setxBranchCode(String xbranchCode) {
        return setValue("xBranchCD", xbranchCode);
    }

    public String getxBranchCode() {
        return (String) getValue("xBranchCD");
    }

    //tBranchCD
    public JSONObject settBranchCode(String tbranchCode) {
        return setValue("tBranchCD", tbranchCode);
    }

    public String gettBranchCode() {
        return (String) getValue("tBranchCD");
    }

    //sAreaCode
    public JSONObject setAreaCode(String areaCode) {
        return setValue("sAreaCode", areaCode);
    }

    public String getAreaCode() {
        return (String) getValue("sAreaCode");
    }

    //sClustrID
    public JSONObject setClusterID(String clusterID) {
        return setValue("sClustrID", clusterID);
    }

    public String getClusterID() {
        return (String) getValue("sClustrID");
    }

    //sPyClustr
    public JSONObject setPhysicalCluster(String physicalClusterID) {
        return setValue("sPyClustr", physicalClusterID);
    }

    public String getPhysicalCluster() {
        return (String) getValue("sPyClustr");
    }

    //dOpenedxx
    public JSONObject setOpenedDate(Date openedDate) {
        return setValue("dOpenedxx", openedDate);
    }

    public Date getOpenedDate() {
        return (Date) getValue("dOpenedxx");
    }

    //cDivision
    public JSONObject setDivision(String division) {
        return setValue("cDivision", division);
    }

    public String getDivision() {
        return (String) getValue("cDivision");
    }

    //cShopType
    public JSONObject setShopType(String shopType) {
        return setValue("cShopType", shopType);
    }

    public String getShopType() {
        return (String) getValue("cShopType");
    }

    //sDealerCd
    public JSONObject setDealerCode(String dealerCode) {
        return setValue("sDealerCd", dealerCode);
    }

    public String getDealerCode() {
        return (String) getValue("sDealerCd");
    }

    //sBrandIDx
    public JSONObject setBrandID(String brandID) {
        return setValue("sBrandIDx", brandID);
    }

    public String getBrandID() {
        return (String) getValue("sBrandIDx");
    }

    //cAccredtd
    public JSONObject setAccredited(String accredited) {
        return setValue("cAccredtd", accredited);
    }

    public String getAccredited() {
        return (String) getValue("cAccredtd");
    }

    //cAccredtd
    public JSONObject isAccredited(boolean isAccredited) {
        return setValue("cAccredtd", (isAccredited == true) ? "1" : "0");
    }

    public boolean isAccredited() {
        return RecordStatus.ACTIVE.equals(getValue("cAccredtd"));
    }

    //sSuppCode
    public JSONObject setSupportCode(String supportCode) {
        return setValue("sSuppCode", supportCode);
    }

    public String getSupportCode() {
        return (String) getValue("sSuppCode");
    }

    //sRegionID
    public JSONObject setRegionID(String regionID) {
        return setValue("sRegionID", regionID);
    }

    public String getRegionID() {
        return (String) getValue("sRegionID");
    }

    //nSuppDsc1
    public JSONObject setSupportDescription1(Number supportDescription1) {
        return setValue("nSuppDsc1", supportDescription1);
    }

    public Number getSupportDescription1() {
        return (Number) getValue("nSuppDsc1");
    }

    //sPayBankx
    public JSONObject setPayBank(String payBank) {
        return setValue("sPayBankx", payBank);
    }

    public String getPayBank() {
        return (String) getValue("sPayBankx");
    }

    //dSalesInv
    public JSONObject setSalesInvoiceDate(Date salesInvoiceDate) {
        return setValue("dSalesInv", salesInvoiceDate);
    }

    public Date getSalesInvoiceDate() {
        return (Date) getValue("dSalesInv");
    }

    //dReceiptx
    public JSONObject setReceiptDate(Date receiptDate) {
        return setValue("dReceiptx", receiptDate);
    }

    public Date getReceiptDate() {
        return (Date) getValue("dReceiptx");
    }

    //sSalesInv
    public JSONObject setSalesInvoice(String salesInvoice) {
        return setValue("sSalesInv", salesInvoice);
    }

    public String getSalesInvoice() {
        return (String) getValue("sSalesInv");
    }

    //sReceiptx
    public JSONObject setReceipt(String receipt) {
        return setValue("sReceiptx", receipt);
    }

    public String getReceipt() {
        return (String) getValue("sReceiptx");
    }

    //sLenderOR
    public JSONObject setLenderOR(String lenderOR) {
        return setValue("sLenderOR", lenderOR);
    }

    public String getLenderOR() {
        return (String) getValue("sLenderOR");
    }

    //dLenderOR
    public JSONObject setLenderORDate(Date lenderORDate) {
        return setValue("dLenderOR", lenderORDate);
    }

    public Date getLenderORDate() {
        return (Date) getValue("dLenderOR");
    }

    //sHostName
    public JSONObject setHostName(String hostName) {
        return setValue("sHostName", hostName);
    }

    public String getHostName() {
        return (String) getValue("sHostName");
    }

    //sHostIPAd
    public JSONObject setHostIPAddress(String hostIPAddress) {
        return setValue("sHostIPAd", hostIPAddress);
    }

    public String getHostIPAddress() {
        return (String) getValue("hostIPAddress");
    }

    //sSlvIPAdd
    public JSONObject setSlaveIPAddress(String slaveIPAddress) {
        return setValue("sSlvIPAdd", slaveIPAddress);
    }

    public String getSlaveIPAddress() {
        return (String) getValue("sSlvIPAdd");
    }

    //sUserName
    public JSONObject setUserName(String userName) {
        return setValue("sUserName", userName);
    }

    public String getUserName() {
        return (String) getValue("sUserName");
    }

    //sPassword
    public JSONObject setPassword(String password) {
        return setValue("sPassword", password);
    }

    public String getPassword() {
        return (String) getValue("sPassword");
    }

    //sDBHostNm
    public JSONObject setDataBaseHostName(String dataBaseHostName) {
        return setValue("sDBHostNm", dataBaseHostName);
    }

    public String getDataBaseHostName() {
        return (String) getValue("sDBHostNm");
    }

    //sDBIPAddr
    public JSONObject setDataBaseIPAdrress(String dataBaseIPAdrress) {
        return setValue("sDBIPAddr", dataBaseIPAdrress);
    }

    public String getDataBaseIPAdrress() {
        return (String) getValue("sDBIPAddr");
    }

    //dRenovatd
    public JSONObject setRenovateDate(Date renovateDate) {
        return setValue("dRenovatd", renovateDate);
    }

    public Date getRenovateDate() {
        return (Date) getValue("dRenovatd");
    }

    //dCntrtBeg
    public JSONObject setCenterTBeginning(Date cntrtBegDate) {
        return setValue("dCntrtBeg", cntrtBegDate);
    }

    public Date getCenterTBeginning() {
        return (Date) getValue("dCntrtBeg");
    }

    //dCntrtEnd
    public JSONObject setRentlBeg(Date cntrtEndDate) {
        return setValue("dCntrtEnd", cntrtEndDate);
    }

    public Date getCenterTEnd() {
        return (Date) getValue("dCntrtEnd");
    }

    //nRentlCur
    public JSONObject setRentalBeginning(Number rentalBeginning) {
        return setValue("nRentlBeg", rentalBeginning);
    }

    public Number getRentalBeginning() {
        return (Number) getValue("nRentlBeg");
    }

    //nRentlBeg
    public JSONObject setRentalCurrent(Number rentalCurrent) {
        return setValue("nRentlCur", rentalCurrent);
    }

    public Number getRentalCurrent() {
        return (Number) getValue("nRentlCur");
    }

    //nMCCapcty
    public JSONObject setMotorCycleCapacity(Number mtorCycleCapacity) {
        return setValue("nMCCapcty", mtorCycleCapacity);
    }

    public Number getMotorCycleCapacity() {
        return (Number) getValue("nMCCapcty");
    }

    //cRealTime
    public JSONObject setRealTime(String realTime) {
        return setValue("cRealTime", realTime);
    }

    public String getRealTime() {
        return (String) getValue("cRealTime");
    }

    //cRealTime
    public JSONObject isRealTime(boolean isRealTime) {
        return setValue("cRealTime", (isRealTime == true) ? "1" : "0");
    }

    public boolean isRealTime() {
        return RecordStatus.ACTIVE.equals(getValue("cRealTime"));
    }

    //dIssuedSI
    public JSONObject setIssuedSaleInvoiceDate(Date issuedSaleInvoiceDate) {
        return setValue("dIssuedSI", issuedSaleInvoiceDate);
    }

    public Date getIssuedSaleInvoiceDate() {
        return (Date) getValue("dIssuedSI");
    }

    //cAllowQMx
    public JSONObject setAllowQuickMatch(String realTime) {
        return setValue("cAllowQMx", realTime);
    }

    public String getAllowQuickMatch() {
        return (String) getValue("cAllowQMx");
    }

    //cAllowQMx
    public JSONObject isAllowQuickMatch(boolean isAllowQuickMatch) {
        return setValue("cAllowQMx", (isAllowQuickMatch == true) ? "1" : "0");
    }

    public boolean isAllowQuickMatch() {
        return RecordStatus.ACTIVE.equals(getValue("cAllowQMx"));
    }

    //dCRInvBeg
    public JSONObject setCRInventoryBeginningDate(Date crInventoryBeginningDate) {
        return setValue("dCRInvBeg", crInventoryBeginningDate);
    }

    public Date getCRInventoryBeginningDate() {
        return (Date) getValue("dCRInvBeg");
    }

    //dOpenD2Dx
    public JSONObject setOpenD2DDate(Date openD2DDate) {
        return setValue("dOpenD2Dx", openD2DDate);
    }

    public Date getOpenD2DDate() {
        return (Date) getValue("dOpenD2Dx");
    }

    //sHAssgnID
    public JSONObject setHeadAssignID(String headAssignID) {
        return setValue("sHAssgnID", headAssignID);
    }

    public String getHeadAssignID() {
        return (String) getValue("sHAssgnID");
    }

    //sSAssgnID
    public JSONObject setSuperVisorAssgnID(String superVisorAssgnID) {
        return setValue("sSAssgnID", superVisorAssgnID);
    }

    public String getSuperVisorAssgnID() {
        return (String) getValue("sSAssgnID");
    }

    //sCAssgnID
    public JSONObject setCollectAssignID(String collectAssignID) {
        return setValue("sCAssgnID", collectAssignID);
    }

    public String getCollectAssignID() {
        return (String) getValue("sCAssgnID");
    }

    //cAllowRbt
    public JSONObject setAllowRebate(String allowRebate) {
        return setValue("cAllowRbt", allowRebate);
    }

    public String getAllowRebate() {
        return (String) getValue("cAllowRbt");
    }

    //cAllowRbt
    public JSONObject isAllowRebate(boolean isAllowRebate) {
        return setValue("cAllowRbt", (isAllowRebate == true) ? "1" : "0");
    }

    public boolean isAllowRebate() {
        return RecordStatus.ACTIVE.equals(getValue("cAllowRbt"));
    }

    //cAllowGwy
    public JSONObject setAllowGiveAway(String allowGiveAway) {
        return setValue("cAllowGwy", allowGiveAway);
    }

    public String gtAllowGiveAway() {
        return (String) getValue("cAllowGwy");
    }

    //cAllowGwy
    public JSONObject isAllowGiveAway(boolean isAllowGiveAway) {
        return setValue("cAllowGwy", (isAllowGiveAway == true) ? "1" : "0");
    }

    public boolean isAllowGiveAway() {
        return RecordStatus.ACTIVE.equals(getValue("cAllowGwy"));
    }

    //cPayDivCd
    public JSONObject setPayDivisionCode(String payDivisionCode) {
        return setValue("cPayDivCd", payDivisionCode);
    }

    public String getPayDivisionCode() {
        return (String) getValue("cPayDivCd");
    }

    //cPromoDiv
    public JSONObject setPromoDivision(String promoDivision) {
        return setValue("cPromoDiv", promoDivision);
    }

    public String getPromoDivision() {
        return (String) getValue("cPromoDiv");
    }

    //cPOSStart
    public JSONObject setPointOfSalesStart(String pointOfSalesStart) {
        return setValue("cPOSStart", pointOfSalesStart);
    }

    public String getPointOfSalesStart() {
        return (String) getValue("cPOSStart");
    }

    //nMaxRebte
    public JSONObject setMaxRebate(Number maxRebate) {
        return setValue("nMaxRebte", maxRebate);
    }

    public Number getMaxRebate() {
        return (Number) getValue("nMaxRebte");
    }

    //dUnEncode
    public JSONObject setUnEncodedDate(Date unEncodedDate) {
        return setValue("dUnEncode", unEncodedDate);
    }

    public Date getUnEncodedDate() {
        return (Date) getValue("dUnEncode");
    }

    //sTimeOpen
    public JSONObject setTimeOpen(String timeOpen) {
        return setValue("sTimeOpen", timeOpen);
    }

    public String getTimeOpen() {
        return (String) getValue("sTimeOpen");
    }

    //sOpenRcpt
    public JSONObject setOpenReciept(String openReciept) {
        return setValue("sOpenRcpt", openReciept);
    }

    public String getOpenReciept() {
        return (String) getValue("sOpenRcpt");
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

    public Model_Branch Branch() throws SQLException, GuanzonException {
        if (!"".equals(getValue("sBranchCD"))) {
            if (this.poBranch.getEditMode() == 1 && this.poBranch
                    .getBranchCode().equals(getValue("sBranchCD"))) {
                return this.poBranch;
            }
            this.poJSON = this.poBranch.openRecord((String) getValue("sBranchCD"));
            if ("success".equals(this.poJSON.get("result"))) {
                return this.poBranch;
            }
            this.poBranch.initialize();
            return this.poBranch;
        }
        this.poBranch.initialize();
        return this.poBranch;
    }

    public Model_Brand Brand() throws SQLException, GuanzonException {
        if (!"".equals(getValue("sBrandIDx"))) {
            if (this.poBrand.getEditMode() == 1 && this.poBrand
                    .getBrandId().equals(getValue("sBrandIDx"))) {
                return this.poBrand;
            }
            this.poJSON = this.poBrand.openRecord((String) getValue("sBrandIDx"));
            if ("success".equals(this.poJSON.get("result"))) {
                return this.poBrand;
            }
            this.poBrand.initialize();
            return this.poBrand;
        }
        this.poBranch.initialize();
        return this.poBrand;
    }

    public Model_Industry Industry() throws SQLException, GuanzonException {
        if (!"".equals(getValue("cDivision"))) {
            if (this.poIndustry.getEditMode() == 1 && this.poIndustry
                    .getIndustryId().equals(getValue("cDivision"))) {
                return this.poIndustry;
            }
            this.poJSON = this.poIndustry.openRecord((String) getValue("cDivision"));
            if ("success".equals(this.poJSON.get("result"))) {
                return this.poIndustry;
            }
            this.poIndustry.initialize();
            return this.poIndustry;
        }
        this.poIndustry.initialize();
        return this.poIndustry;
    }

    public Model_Branch_Area BranchArea() throws SQLException, GuanzonException {
        if (!"".equals(getValue("sAreaCode"))) {
            if (this.poBranchArea.getEditMode() == 1 && this.poBranchArea
                    .getAreaCode().equals(getValue("sAreaCode"))) {
                return this.poBranchArea;
            }
            this.poJSON = this.poBranchArea.openRecord((String) getValue("sAreaCode"));
            if ("success".equals(this.poJSON.get("result"))) {
                return this.poBranchArea;
            }
            this.poBranchArea.initialize();
            return this.poBranchArea;
        }
        this.poBranchArea.initialize();
        return this.poBranchArea;
    }

}
