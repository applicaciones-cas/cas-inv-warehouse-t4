package ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.services;

import java.sql.SQLException;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.BranchArea;
import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.BranchCluster;
import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.BranchClusterDelivery;
import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.BranchOthers;

/**
 *
 * @author 12mnv
 */
public class DeliveryParamController {

    private GRiderCAS poGRider;
    private LogWrapper poLogWrapper;
    private BranchOthers poBranchOthers;
    private BranchArea poBranchArea;
    private BranchCluster poBranchCluster;
    private BranchClusterDelivery poBranchClusterDelivery;

    public DeliveryParamController(GRiderCAS applicationDriver, LogWrapper logWrapper) {
        this.poGRider = applicationDriver;
        this.poLogWrapper = logWrapper;
    }

    public BranchOthers BranchOthers() throws SQLException, GuanzonException {
        if (this.poGRider == null) {
            this.poLogWrapper.severe("DeliveryParamController.BranchOthers: Application driver is not set.");
            return null;
        }
        if (this.poBranchOthers != null) {
            return this.poBranchOthers;
        }
        this.poBranchOthers = new BranchOthers();
        this.poBranchOthers.setApplicationDriver(this.poGRider);
        this.poBranchOthers.setWithParentClass(false);
        this.poBranchOthers.setLogWrapper(this.poLogWrapper);
        this.poBranchOthers.initialize();
        this.poBranchOthers.newRecord();
        return this.poBranchOthers;
    }

    public BranchArea BranchArea() throws SQLException, GuanzonException {
        if (this.poGRider == null) {
            this.poLogWrapper.severe("DeliveryParamController.BranchArea: Application driver is not set.");
            return null;
        }
        if (this.poBranchCluster != null) {
            return this.poBranchArea;
        }
        this.poBranchArea = new BranchArea();
        this.poBranchArea.setApplicationDriver(this.poGRider);
        this.poBranchArea.setWithParentClass(false);
        this.poBranchArea.setLogWrapper(this.poLogWrapper);
        this.poBranchArea.initialize();
        this.poBranchArea.newRecord();
        return this.poBranchArea;
    }

    public BranchCluster BranchCluster() throws SQLException, GuanzonException {
        if (this.poGRider == null) {
            this.poLogWrapper.severe("DeliveryParamController.BranchCluster: Application driver is not set.");
            return null;
        }
        if (this.poBranchCluster != null) {
            return this.poBranchCluster;
        }
        this.poBranchCluster = new BranchCluster();
        this.poBranchCluster.setApplicationDriver(this.poGRider);
        this.poBranchCluster.setWithParentClass(false);
        this.poBranchCluster.setLogWrapper(this.poLogWrapper);
        this.poBranchCluster.initialize();
        this.poBranchCluster.newRecord();
        return this.poBranchCluster;
    }

    public BranchClusterDelivery BranchClusterDelivery() throws SQLException, GuanzonException {
        if (this.poGRider == null) {
            this.poLogWrapper.severe("DeliveryParamController.BranchClusterDelivery: Application driver is not set.");
            return null;
        }
        if (this.poBranchCluster != null) {
            return this.poBranchClusterDelivery;
        }
        this.poBranchClusterDelivery = new BranchClusterDelivery();
        this.poBranchClusterDelivery.setApplicationDriver(this.poGRider);
        this.poBranchClusterDelivery.setWithParentClass(false);
        this.poBranchClusterDelivery.setLogWrapper(this.poLogWrapper);
        this.poBranchClusterDelivery.initialize();
        this.poBranchClusterDelivery.newRecord();
        return this.poBranchClusterDelivery;
    }

}
