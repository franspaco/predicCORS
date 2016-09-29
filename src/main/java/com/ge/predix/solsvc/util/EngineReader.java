package com.ge.predix.solsvc.util;

import com.opencsv.CSVReader;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.commons.math3.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by franspaco on 27/09/16.
 */
public class EngineReader {
    String engineFile;
    JSONObject summary = null;

    public EngineReader(String engineFile){
        this.engineFile = engineFile;
    }

    public List readFile(){
        List lines;
        try {
            System.out.println("Reading engines.");
            CSVReader reader = new CSVReader(new FileReader(engineFile), ',');
            lines = reader.readAll();
            System.out.println("Read.");
            lines.remove(0);

            //Erase NAs
            //Bad idea because cancellations are NAs
            /*
            for(int i = 0; i < lines.size(); i++){
                String [] line = (String[])lines.get(i);
                for(int j = 0; j < line.length;j++ ){
                    if(line[j] == "NA"){
                        lines.remove(i);
                        i--;
                    }
                }
            }
            */
            return lines;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject getRaw(){
        List lines = readFile();
        JSONObject out = new JSONObject();
        String [] line;

        for(int i = 0; i < lines.size(); i++){
            line = (String[])lines.get(i);
            JSONObject temp = new JSONObject();
            temp.put("flightID",              line[0]);
            temp.put("EngineTemperature",     line[1]);
            temp.put("EngineSerial",          line[2]);
            temp.put("EngineRoute",           line[3]);
            temp.put("EngineTemperatureTime", line[4]);
            temp.put("Cancellation",          line[5]);
            out.accumulate("entry", temp);
        }

        return out;
    }

    public JSONObject getFailureData(){
        JSONObject out = new JSONObject();
        JSONObject all = getEngineData(false, false);
        JSONArray engines = new JSONArray(all.get("engines").toString());

        for(int i = 0; i < engines.length(); i++){
            JSONObject temp = engines.getJSONObject(i);
            if(temp.getBoolean("failed")){
                out.accumulate("engines", temp);
            }
        }

        return out;
    }

    public JSONObject getEngineData(boolean status, boolean keys){
        //Status true for current status aka resets time when a cancellation takes place
        // and part is replaced

        //THIS WILL CAUSE PROBLEMS IF THE FILE IS CHANGES AND THE SERVER NOT RESTARTED
        /*
        if(summary != null)
            return summary;*/

        List lines = readFile();

        ArrayList<String> engines = new ArrayList<String>();
        ArrayList<Double> times = new ArrayList<Double>();
        ArrayList<Integer> timesTotal = new ArrayList<Integer>();
        ArrayList<Integer> cycles = new ArrayList<Integer>();
        ArrayList<Integer> cyclesTotal = new ArrayList<Integer>();
        ArrayList<Integer> routes = new ArrayList<Integer>();
        ArrayList<Boolean> failedYet = new ArrayList<Boolean>();


        for(int i = 0; i < lines.size(); i++){
            String [] line = (String[])lines.get(i);
            int indx = engines.indexOf(line[2]);
            int time = 0;
            int route = -1;
            double temp = 0;
            try{
                time = Integer.parseInt(line[4]);
                temp = Double.parseDouble(line[1]);
                route = Integer.parseInt(line[3]);
            }catch(Exception ex){

            }

            if(indx == -1){
                engines.add(line[2]);
                times.add(0.0);
                timesTotal.add(0);
                cycles.add(0);
                cyclesTotal.add(0);
                routes.add(route);
                failedYet.add(false);
                indx = engines.size() -1;
            }

            if(line[5].indexOf("1") != -1){
                failedYet.set(indx, Boolean.TRUE);
                if(status) {
                    times.set(indx, 0.0);
                    cycles.set(indx, 0);
                }
            }
            if (temp >= 263.0) {
                timesTotal.set(indx, timesTotal.get(indx) + time);
                if(status){
                    times.set(indx, times.get(indx) + time);
                }else {
                    if (!failedYet.get(indx)) {
                        times.set(indx, times.get(indx) + time);
                    }
                }
            }

            cycles.set(indx, cycles.get(indx) + 1);
            cyclesTotal.set(indx, cyclesTotal.get(indx) +1);
        }

        JSONObject out = new JSONObject();

        for(int i = 0; i < engines.size(); i++){
            JSONObject engine = new JSONObject();
            if(keys){
                engine.put("key", engines.get(i));
                engine.put("val", engines.get(i));
            }else {
                engine.put("engineSerial", engines.get(i));
                engine.put("route", routes.get(i));
                engine.put("time", times.get(i));
                engine.put("cycles", cycles.get(i));
                engine.put("timeTotal", timesTotal.get(i));
                engine.put("cyclesTotal", cyclesTotal.get(i));
                engine.put("failed", failedYet.get(i));
                engine.put("mpc", (double) timesTotal.get(i) / (double) cyclesTotal.get(i));
            }
            out.accumulate("engines", engine);
        }

        summary = out;
        return out;
    }

    public JSONObject getSingleEngine(String engID){
        JSONObject status = getEngineData(true, false);
        JSONArray engines = new JSONArray(status.get("engines").toString());

        for(int i = 0; i < engines.length(); i++){
            JSONObject temp = engines.getJSONObject(i);
            if(temp.getString("engineSerial").equals(engID)){
                return temp;
            }
        }

        return new JSONObject("{\"error\":\"engine serial not found\"}");

    }

    public JSONObject getStats(){
        JSONObject fails = getFailureData();
        JSONObject all = getEngineData(false, false);
        JSONArray enginesFails = new JSONArray(fails.get("engines").toString());
        JSONArray enginesAll = new JSONArray(all.get("engines").toString());
        JSONObject out = new JSONObject();

        double avgMPC = 0;

        for(int i = 0; i < all.length(); i++){
            JSONObject temp = enginesAll.getJSONObject(i);
            avgMPC += temp.getDouble("mpc");
        }
        avgMPC /= all.length();

        double sampleTimeAVG = 0;
        double stdev = 0;
        int n = enginesFails.length();

        for(int i = 0; i < n; i++){
            JSONObject temp = enginesFails.getJSONObject(i);
            sampleTimeAVG += temp.getDouble("time");
        }
        sampleTimeAVG /= ((double) enginesFails.length());

        for(int i = 0; i < n; i++){
            JSONObject temp = enginesFails.getJSONObject(i);
            double time = temp.getDouble("time");
            stdev += Math.pow(time-sampleTimeAVG, 2);
        }

        stdev /= (n - 1);

        stdev = Math.sqrt(stdev);

        out.put("n", n);
        out.put("sampleAVG", sampleTimeAVG);
        out.put("stdev", stdev);
        out.put("avgmpc", avgMPC);


        return out;
    }

    public JSONObject getEnginePred(String serial, int min, int max){
        RoutesReader rr = new RoutesReader("DATA/routes.csv", "DATA/revenues.csv", "DATA/airports.csv");
        JSONObject engine = getSingleEngine(serial);
        JSONObject stats = getStats();

        JSONObject cycles = new JSONObject();

        double mean = stats.getDouble("sampleAVG");
        double stdev = stats.getDouble("stdev");
        double c = 834222.875;

        NormalDistribution nd = new NormalDistribution(mean, stdev);

        for(int i = min; i <= max; i++){
            double x = engine.getDouble("time") + (double)i * stats.getDouble("avgmpc");
            double p = nd.cumulativeProbability(x);
            JSONObject revenue = rr.getEarnings(engine.getInt("route"), i);
            double g = revenue.getDouble("revenue");
            double cost = revenue.getDouble("cost");
            double Pmax = g/(g+c);

            //double index = 1 - 1/((cost/621500) + (p/Pmax));
            double index = (1 - Pmax/p)*(621500/cost);

            index *= 100;
            p *= 100;


            if(Math.abs(index) > 0.0) {
                JSONObject cycle = new JSONObject();
                cycle.put("prob", p);
                cycle.put("pmax", Pmax);
                //cycle.put("index", index);

                System.out.println(i + ", " + Pmax + ", " + x + ", " + p + ", " + index);
                cycles.put(String.valueOf(i), cycle);
            }
        }

        return cycles;
    }
}
