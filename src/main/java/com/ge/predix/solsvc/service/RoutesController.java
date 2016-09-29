package com.ge.predix.solsvc.service;

import com.ge.predix.solsvc.util.RoutesReader;
import org.springframework.web.bind.annotation.*;

/**
 * Created by franspaco on 28/09/16.
 */
@RestController
@CrossOrigin
public class RoutesController {
    RoutesReader RR;
    public RoutesController() {
        super();
        RR = new RoutesReader("DATA/routes.csv", "DATA/revenues.csv", "DATA/airports.csv");
    }

    @SuppressWarnings("nls")
    @RequestMapping(value = "/routes", method = RequestMethod.GET)
    public String getRoutes(){
        return String.format(RR.getRoutes().toString());
    }

    @SuppressWarnings("nls")
    @RequestMapping(value = "/revenues", method = RequestMethod.GET)
    public String getRevenues(){
        return String.format(RR.getRevenues().toString());
    }

    @SuppressWarnings("nls")
    @RequestMapping(value = "/airports", method = RequestMethod.GET)
    public String getAirports(){
        return String.format(RR.getAirpots().toString());
    }

    @SuppressWarnings("nls")
    @RequestMapping(value = "/revenues/{route}/{step}", method = RequestMethod.GET)
    public String getEarnings(@PathVariable(value="route") int route, @PathVariable(value="step") int step){
        return String.format(RR.getEarnings(route, step).toString());
    }
}
