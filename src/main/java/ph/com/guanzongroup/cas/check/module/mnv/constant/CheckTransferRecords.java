/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.cas.check.module.mnv.constant;

import ph.com.guanzongroup.cas.inv.warehouse.t4.constant.*;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;

/**
 *
 * @author Maynard
 */
public class CheckTransferRecords {

    public static final String MOBILE_PHONE_REPORT = "CheckTransferMP";
    public static final String MOTORCYCLE_REPORT = "CheckTransferMC";
    public static final String CAR_REPORT = "CheckTransferCar";
    public static final String HOSPITALITY_REPORT = "CheckTransferMonarch";
    public static final String LOS_PEDRITOS_REPORT = "CheckTransferLP";
    public static final String GENERAL_REPORT = "CheckTransfer";
    public static final String APPLIANCE_REPORT = "CheckTransferAppliance";

    public static final String CheckPaymentRecord() {
        String lsSQL = "SELECT"
                + "  a.sTransNox,"
                + "  a.sBranchCd,"
                + "  a.sIndstCdx,"
                + "  a.dTransact,"
                + "  a.sBankIDxx,"
                + "  a.sBnkActID,"
                + "  a.sCheckNox,"
                + "  a.dCheckDte,"
                + "  a.sPayorIDx,"
                + "  a.sPayeeIDx,"
                + "  a.nAmountxx,"
                + "  a.sRemarksx,"
                + "  a.sSourceCd,"
                + "  a.cLocation,"
                + "  a.cIsReplcd,"
                + "  a.cReleased,"
                + "  a.cPayeeTyp,"
                + "  a.cDisbMode,"
                + "  a.cClaimant,"
                + "  a.sAuthorze,"
                + "  a.cIsCrossx,"
                + "  a.cIsPayeex,"
                + "  a.cTranStat,"
                + "  a.cProcessd,"
                + "  a.cPrintxxx,"
                + "  a.dPrintxxx,"
                + "  b.sBankName sBankName,"
                + "  c.sActNumbr sActNumbr,"
                + "  c.sActNamex sActNamex"
                + " FROM Check_Payments a"
                + "  LEFT JOIN Banks b ON a.sBankIDxx = b.sBankIDxx"
                + "  LEFT JOIN Bank_Account_Master c ON a.sBnkActID = c.sBnkActID";

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
