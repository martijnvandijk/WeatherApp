package net.martijnvandijk.weatherclient;

/**
 * Created by martijn on 6/22/17.
 */

public class SensorNode {
    SensorNode(String name, String sensorNodeID){
        this.name = name;
        this.sensorNodeID = sensorNodeID;
    }
    private String name;
    private String sensorNodeID;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSensorNodeID() {
        return sensorNodeID;
    }

    public void setSensorNodeID(String sensorNodeID) {
        this.sensorNodeID = sensorNodeID;
    }
}
