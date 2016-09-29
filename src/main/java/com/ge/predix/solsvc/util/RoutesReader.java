package com.ge.predix.solsvc.util;

import org.json.*;
import com.opencsv.CSVReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;


/**
 * Created by franspaco on 28/09/16.
 */
public class RoutesReader {
    String routesFile;
    String revenuesFile;
    String airportsFile;

    public RoutesReader(String routesFile, String revenuesFile, String airportsFile){
        this.revenuesFile = revenuesFile;
        this.routesFile = routesFile;
        this.airportsFile = airportsFile;
    }

    public JSONObject getEarnings(int route, int step){
        JSONObject routes = getRoutes();
        JSONObject revenues = getRevenues();

        JSONArray steps = routes.getJSONArray(String.valueOf(route));


        int lastStep = (step - 1) % (steps.length()-1);
        int nextStep = lastStep + 1;

        String lastIATA = steps.getString(lastStep);
        String nextIATA = steps.getString(nextStep);

        double revenue = revenues.getJSONObject(lastIATA).getDouble(nextIATA);
        JSONObject out = new JSONObject();
        out.put("revenue", revenue);
        out.put("cost", getAirpots().getJSONObject(lastIATA).getDouble("cost"));
        return out;
    }

    public JSONObject getRoutes(){
        JSONObject out = new JSONObject();
        List lines;
        try {
            //System.out.println("Reading routes.");
            CSVReader reader = new CSVReader(new FileReader(routesFile), ',');
            lines = reader.readAll();

            int routeNo = ((String[])lines.get(0)).length;

            for(int i = 0; i < routeNo; i++){
                for (int j = 1; j < lines.size(); j++) {
                    String next = ((String[])lines.get(j))[i];
                    if(!next.isEmpty() && next != null) {
                        out.accumulate(String.valueOf(i+1), next);
                    }
                }
            }
            //System.out.println("Read.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return out;
    }

    public JSONObject getRevenues(){
        JSONObject out = new JSONObject();
        List lines;
        try {
            //System.out.println("Reading revenues.");
            CSVReader reader = new CSVReader(new FileReader(revenuesFile), ',');
            lines = reader.readAll();
            String[] iatasDest = (String[])lines.get(0);

            for (int i = 1; i < lines.size(); i++){
                JSONObject temp = new JSONObject();
                String[] costs = (String[])lines.get(i);
                for(int j = 1; j < costs.length; j++){
                    temp.put(iatasDest[j], costs[j]);
                }
                out.put(costs[0], temp);
            }

            //System.out.println("Read.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return out;
    }

    public JSONObject getAirpots(){
        JSONObject out = new JSONObject();
        List lines;
        try {
            //System.out.println("Reading airports.");
            CSVReader reader = new CSVReader(new FileReader(airportsFile), ',');
            lines = reader.readAll();
            lines.remove(0);

            for (int i = 0; i < lines.size(); i++ ){
                String[] line = (String[])lines.get(i);
                JSONObject airpt = new JSONObject();
                airpt.put("city", line[1]);
                airpt.put("name", line[0]);
                double cost = 0;
                try{
                    cost = Double.parseDouble(line[3]);
                }catch (NumberFormatException ex){

                }
                airpt.put("cost", cost);

                out.put(line[2], airpt);
            }

            //System.out.println("Read.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return out;
    }
}
