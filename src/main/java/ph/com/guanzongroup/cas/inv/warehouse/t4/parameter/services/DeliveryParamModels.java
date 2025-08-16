package ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.services;

import org.guanzon.appdriver.base.GRiderCAS;
import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.model.Model_Branch_Area;
import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.model.Model_Branch_Cluster;
import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.model.Model_Branch_Cluster_Delivery;
import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.model.Model_Branch_Others;
import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.model.Model_Inventory_Supplier;

/**
 *
 * @author 12mnv
 */
public class DeliveryParamModels {

    private Model_Branch_Others poBranchOthers;
    private Model_Branch_Area poBranchArea;
    private Model_Branch_Cluster poBranchCluster;
    private Model_Branch_Cluster_Delivery poBranchClusterDelivery;
    private Model_Inventory_Supplier poSupplier;

    private final GRiderCAS poGRider;

    public DeliveryParamModels(GRiderCAS applicationDriver) {
        this.poGRider = applicationDriver;
    }

    public Model_Branch_Others BranchOthers() {
        if (this.poGRider == null) {
            System.err.println("BranchOthers.Barangay: Application driver is not set.");
            return null;
        }
        if (this.poBranchOthers == null) {
            this.poBranchOthers = new Model_Branch_Others();
            this.poBranchOthers.setApplicationDriver(this.poGRider);
            this.poBranchOthers.setXML("Model_Branch_Others");
            this.poBranchOthers.setTableName("Branch_Others");
            this.poBranchOthers.initialize();
        }
        return this.poBranchOthers;
    }

    public Model_Branch_Area BranchArea() {
        if (this.poGRider == null) {
            System.err.println("DeliveryParamModels.BranchArea: Application driver is not set.");
            return null;
        }
        if (this.poBranchArea == null) {
            this.poBranchArea = new Model_Branch_Area();
            this.poBranchArea.setApplicationDriver(this.poGRider);
            this.poBranchArea.setXML("Model_Branch_Area");
            this.poBranchArea.setTableName("Branch_Area");
            this.poBranchArea.initialize();
        }
        return this.poBranchArea;
    }

    public Model_Branch_Cluster BranchCluster() {
        if (this.poGRider == null) {
            System.err.println("DeliveryParamModels.BranchCluster: Application driver is not set.");
            return null;
        }
        if (this.poBranchCluster == null) {
            this.poBranchCluster = new Model_Branch_Cluster();
            this.poBranchCluster.setApplicationDriver(this.poGRider);
            this.poBranchCluster.setXML("Model_Branch_Cluster");
            this.poBranchCluster.setTableName("Branch_Cluster");
            this.poBranchCluster.initialize();
        }
        return this.poBranchCluster;
    }

    public Model_Branch_Cluster_Delivery BranchClusterDelivery() {
        if (this.poGRider == null) {
            System.err.println("DeliveryParamModels.BranchClusterDelivery: Application driver is not set.");
            return null;
        }
        if (this.poBranchClusterDelivery == null) {
            this.poBranchClusterDelivery = new Model_Branch_Cluster_Delivery();
            this.poBranchClusterDelivery.setApplicationDriver(this.poGRider);
            this.poBranchClusterDelivery.setXML("Model_Branch_Cluster_Delivery");
            this.poBranchClusterDelivery.setTableName("Branch_Cluster_Delivery");
            this.poBranchClusterDelivery.initialize();
        }
        return this.poBranchClusterDelivery;
    }

    public Model_Inventory_Supplier InventorySupplier() {
        if (this.poGRider == null) {
            System.err.println("DeliveryParamModels.InventorySupplier: Application driver is not set.");
            return null;
        }
        if (this.poSupplier == null) {
            this.poSupplier = new Model_Inventory_Supplier();
            this.poSupplier.setApplicationDriver(this.poGRider);
            this.poSupplier.setXML("Model_Inv_Supplier");
            this.poSupplier.setTableName("Inv_Supplier");
            this.poSupplier.initialize();
        }
        return this.poSupplier;
    }

}
