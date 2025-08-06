/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.inv.warehouse.t4.validators;

import org.guanzon.appdriver.iface.GValidator;

public class DeliveryScheduleValidatorFactory {

    public static GValidator make(String industryId) {
        switch (industryId) {
            case "01": //Mobile Phone
                return new DeliverySchedule_MP();
            case "02": //Motorcycle
                return new DeliverySchedule_MC();
            case "03": //Vehicle
                return new DeliverySchedule_Vehicle();
            case "04": //Hospitality
                return new DeliverySchedule_Hospitality();
            case "05": //Los Pedritos
                return new DeliverySchedule_LP();
            case "": //General
                return new DeliverySchedule_General();
            case "07": //Appliances
                return new DeliverySchedule_Appliance();
            default:
                return null;
        }
    }

}
