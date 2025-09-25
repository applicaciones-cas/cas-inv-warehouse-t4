/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.check.module.mnv.validator;

import ph.com.guanzongroup.cas.inv.warehouse.t4.validators.*;
import org.guanzon.appdriver.iface.GValidator;

public class CheckValidatorFactory {

    public static GValidator make(String industryId) {
        switch (industryId) {
            case "01": //Mobile Phone
                return new CheckTransfer_MP();
            case "02": //Motorcycle
                return new CheckTransfer_MC();
            case "03": //Vehicle
                return new CheckTransfer_Car();
            case "04": //Monarch
                return new CheckTransfer_Monarch();
            case "05": //Los Pedritos
                return new CheckTransfer_LP();
            case "06": //General
                return new CheckTransfer_General();
            case "07": //Appliances
                return new CheckTransfer_Appliance();

            case "": //Main Office
                return new CheckTransfer_General();
            default:
                return null;
        }
    }

}
