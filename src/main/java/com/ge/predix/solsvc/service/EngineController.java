package com.ge.predix.solsvc.service;

import com.opencsv.CSVReader;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import com.ge.predix.solsvc.util.*;
/**
 * Created by franspaco on 27/09/16.
 * "/home/franspaco/Documents/DATA/engines.csv"
 */
@RestController
@CrossOrigin
public class EngineController {
    EngineReader ER;
    public EngineController() {
        super();
        ER = new EngineReader("DATA/engines.csv");
    }


    @SuppressWarnings("nls")
    @RequestMapping(value = "/engines/raw", method = RequestMethod.GET)
    public String enginesRaw(){
        return String.format(ER.getRaw().toString());
    }

    @SuppressWarnings("nls")
    @RequestMapping(value = "/engines/summary", method = RequestMethod.GET)
    public String enginesSummary(){
        return String.format(ER.getEngineData(false, false).toString());
    }

    @SuppressWarnings("nls")
    @RequestMapping(value = "/engines/all", method = RequestMethod.GET)
    public String enginesAll(){
        return String.format(ER.getEngineData(true, true).toString());
    }

    @SuppressWarnings("nls")
    @RequestMapping(value = "/engines/stats", method = RequestMethod.GET)
    public String getStats(){
        return String.format(ER.getStats().toString());
    }

    @SuppressWarnings("nls")
    @RequestMapping(value = "/engines/status", method = RequestMethod.GET)
    public String enginesStatus(){
        return String.format(ER.getEngineData(true, false).toString());
    }

    @SuppressWarnings("nls")
    @RequestMapping(value = "/engines/failures", method = RequestMethod.GET)
    public String enginesFailures(){
        return String.format(ER.getFailureData().toString());
    }

    @RequestMapping(value = "/engines/{engineID}", method = RequestMethod.GET)
    public String getEngine(@PathVariable(value="engineID") String id) {
        return String.format(ER.getSingleEngine(id).toString());
    }

    @RequestMapping(value = "/engines/{engineID}/{fmin}/{fmax}", method = RequestMethod.GET)
    public String getEnginePredix(@PathVariable(value="engineID") String id, @PathVariable(value="fmin") int fmin, @PathVariable(value="fmax") int fmax) {
        return String.format(ER.getEnginePred(id, fmin, fmax).toString());
    }
}
