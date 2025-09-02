/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.cas.inv.warehouse.t4.constant;

import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;

/**
 *
 * @author Maynard
 */
public class DeliveryStockIssuanceRecord {

    public static final String MOBILE_PHONE_REPORT = "InventoryIssuanceMP";
    public static final String MOTORCYCLE_REPORT = "InventoryIssuanceMC";
    public static final String CAR_REPORT = "InventoryIssuanceCar";
    public static final String HOSPITALITY_REPORT = "InventoryIssuanceMonarch";
    public static final String LOS_PEDRITOS_REPORT = "InventoryIssuanceLP";
    public static final String GENERAL_REPORT = "InventoryIssuance";
    public static final String APPLIANCE_REPORT = "InventoryIssuanceAppliance";

    public static final String StockRequestRecord() {
        String lsSQL = "SELECT"
                + "  d.sClustrID,"
                + "  a.sTransNox,"
                + "  a.dtransact,"
                + "  a.sBranchCd,"
                + "  e.sBranchNm,"
                + "  a.sIndstCdx,"
                + "  a.sCategrCd,"
                + "  a.sTransNox,"
                + "  b.nQuantity,"
                + "  b.nApproved,"
                + "  b.nCancelld,"
                + "  b.nIssueQty,"
                + "  b.nOrderQty,"
                + "  b.nReceived"
                + " FROM Inv_Stock_Request_Master a"
                + "  LEFT JOIN Inv_Stock_Request_Detail b ON a.sTransNox = b.sTransNox"
                + "  LEFT JOIN Branch_Others c ON a.sBranchCD = c.sBranchCd"
                + "  LEFT JOIN Branch_Cluster d ON c.sClustrID = d.sClustrID"
                + "  LEFT JOIN Branch e ON a.sBranchCD = e.sBranchCd";

        return lsSQL;
    }

    public static String getJasperReport(String psIndustryCode) {
        switch (psIndustryCode) {
            case "01":
                return MOBILE_PHONE_REPORT;
            case "02":
                return MOTORCYCLE_REPORT;
            case "03":
                return CAR_REPORT;
            case "04":
                return HOSPITALITY_REPORT;
            case "05":
                return LOS_PEDRITOS_REPORT;
            case "06":
                return GENERAL_REPORT;
            case "07":
                return APPLIANCE_REPORT;
            default:
                return GENERAL_REPORT;
        }
    }

}
