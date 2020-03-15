/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PerformanceMeasure;

/**
 *
 * This class represents a processor
 */
public class Processor {

    //time this processor spends for processing "computation" jobs
    private double totalComputationTime = 0.0;

    //time this processor spends for processing "transmission" jobs
    private double totalTransmissionTime = 0.0;

    private String name;
    private double total_Energy = 0;
    private double total_Cost = 0;

    //the time it starts processing a job
    private double start = 0.0;

    //the time when this processor is done with the processing 
    //and is free again
    private double end = 0.0;

    private double processor_Idle_Time = 0.0;

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }

    public Processor(String n) {
        name = n;
    }

    public double getTotal_Energy() {
        return this.getTotalComputationTime() * RunMeasure.computation_Energy_charge + this.getTotalTransmissionTime() * RunMeasure.transmission_Energy_charge + this.getProcessor_Idle_Time() * RunMeasure.idle_Energy_charge;
    }

    public double getTotal_Cost() {
        return total_Cost;
    }

    public double getTotalComputationTime() {
        return totalComputationTime;
    }

    public double getProcessor_Idle_Time() {
        return processor_Idle_Time;
    }

    public void setProcessor_Idle_Time(double processor_Idle_Time) {
        this.processor_Idle_Time = processor_Idle_Time;
    }

    public double getTotalTransmissionTime() {
        return totalTransmissionTime;
    }

    public double getStart() {
        return start;
    }

    public double getEnd() {
        return end;
    }

    public String getName() {
        return name;
    }

    //Returns the time when the processor will be free
    public double getTimeWhenItIsFree() {
        return end;
    }

    //Returns true if the processor is free at time t
    public boolean isFree(double t) {
        if (t >= end) {
            return true;
        }
        return false;
    }

    //Sets the processor to busy from time s to time e
    public void setBusy(boolean isComputation, double s, double e) {
        start = s;
        end = e;
        if (true == isComputation) {
            totalComputationTime = totalComputationTime + (end - start);
        } else {
            totalTransmissionTime = totalTransmissionTime + (end - start);
        }
    }

    public String toString() {
        return name + ": Computation = " + this.getTotalComputationTime() + ", Transmission = " + this.getTotalTransmissionTime() + ", Idle = " + this.getProcessor_Idle_Time()
                + " Consumed Energy = " + this.getTotal_Energy();
    }
}
