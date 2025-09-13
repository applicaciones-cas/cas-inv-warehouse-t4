/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.inv.warehouse.t4.validators;

import org.guanzon.appdriver.iface.GValidator;

public class InventoryClusterIssuanceValidatorFactory {

    public static GValidator make(String industryId) {
        switch (industryId) {
            case "01": //Mobile Phone
                return new InventoryStockClusterIssuance_MP();
            case "02": //Motorcycle
                return new InventoryStockClusterIssuance_MC();
            case "03": //Vehicle
                return new InventoryStockClusterIssuance_Vehicle();
            case "04": //Monarch
                return new InventoryStockClusterIssuance_Monarch();
            case "05": //Los Pedritos
                return new InventoryStockClusterIssuance_LP();
            case "06": //General
                return new InventoryStockClusterIssuance_General();
            case "07": //Appliances
                return new InventoryStockClusterIssuance_Appliance();

            case "": //Main Office
                return new InventoryStockClusterIssuance_General();
            default:
                return null;
        }
    }

}
