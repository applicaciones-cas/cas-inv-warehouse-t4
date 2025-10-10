package ph.com.guanzongroup.cas.purchasing.module.mnv.constant;

import ph.com.guanzongroup.cas.check.module.mnv.constant.*;
import ph.com.guanzongroup.cas.inv.warehouse.t4.constant.*;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;

/**
 *
 * @author Maynard
 */
public class POCancellationRecords {

    public static final String PurchaseOrder() {
        String lsSQL = "SELECT"
                + "  a.sTransNox,"
                + "  a.sBranchCd,"
                + "  a.sIndstCdx,"
                + "  a.sCategrCd,"
                + "  a.dTransact,"
                + "  a.sCompnyID,"
                + "  a.sDestinat,"
                + "  a.sSupplier,"
                + "  a.sAddrssID,"
                + "  a.sContctID,"
                + "  a.sReferNox,"
                + "  a.sTermCode,"
                + "  a.nDiscount,"
                + "  a.nAddDiscx,"
                + "  a.nTranTotl,"
                + "  a.nAmtPaidx,"
                + "  a.cWithAddx,"
                + "  a.nDPRatexx,"
                + "  a.nAdvAmtxx,"
                + "  a.nNetTotal,"
                + "  a.sRemarksx,"
                + "  a.dExpected,"
                + "  a.cEmailSnt,"
                + "  a.nEmailSnt,"
                + "  a.cPrintxxx,"
                + "  a.nEntryNox,"
                + "  a.sInvTypCd,"
                + "  a.cPreOwned,"
                + "  a.cProcessd,"
                + "  a.cTranStat,"
                + "  b.nQuantity,"
                + "  b.nReceived,"
                + "  b.nCancelld,"
                + "  c.sBranchNm xDestinat,"
                + " FROM PO_Master a"
                + " LEFT JOIN PO_Detail b ON a.sTransNox = b.sTransNox"
                + " LEFT JOIN Branch c ON a.sDestinat = c.sBranchCd";

        return lsSQL;
    }
}
