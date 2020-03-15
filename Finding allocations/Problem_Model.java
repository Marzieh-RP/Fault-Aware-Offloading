/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PerformanceMeasure;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

 /**
 *
 
 Original simulator by Ismail Sheikh 
 Enhanced by Marzieh Ranjbar Pirbasti to enable fault-aware modelling
 
 */
public class Problem_Model {

    public static int taskId = 0;
    private int nodeId;
    private double executionTime;
    private boolean dependency;
    private int[] depend_Node_id;
    private int[] next_Node_id;
    private double[] communication_data;
    private double lambda;
    private int typeoffaultandrecovery;
    
    //olivia: start
    private int level;
    public static int maxLevel;
    //olivia: end

    public Problem_Model(int nodeId, double executionTime, boolean dependency, int[] depend_Node_id, int[] next_Node_id, double[] communication_data) {

        this.nodeId = nodeId;
        this.executionTime = executionTime;
        this.dependency = dependency;
        this.depend_Node_id = depend_Node_id;
        this.next_Node_id = next_Node_id;
        this.communication_data = communication_data;
        Problem_Model.taskId++;

    }

    public double[] getCommunication_data() {
        return communication_data;
    }
    
    //olivia: start
    public void setLevel(int i) {
        level = i;
        if(maxLevel < i)
            maxLevel = i;
    }
    public int getLevel() {
        return level;
    }
    //olivia: end

    public List<Integer> getNext_Node_id() {
        return IntStream.of(next_Node_id).boxed().collect(Collectors.toList());
    }
    public String get_String_communication_data(){
        String temp_com= "";
        for (double temp : getCommunication_data()) {
            temp_com += temp + ", ";
        }
        return temp_com.substring(0, temp_com.length()-2);
    }
    public int[] getDepend_Node_id() {
        return depend_Node_id.clone();
    }

    public int getNodeId() {
        return nodeId;
    }

    public static int getTaskId() {
        return taskId;
    }

    public double getExecutionTime() {
    	//regular
        return executionTime;

    }

    public boolean isDependency() {
        return dependency;
    }

    @Override
    public String toString() {
        return "Node{" + "nodeId= " + nodeId + ", ExecutionTime= " + String.format ("%.4f", executionTime) + ", Communication_Data: " + get_String_communication_data()+ '}';
    }

}
