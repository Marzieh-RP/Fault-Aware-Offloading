/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PerformanceMeasure;

import java.util.ArrayList;
import java.util.Collections;

 /**
 *
 
 Original simulator by Ismail Sheikh 
 Enhanced by Marzieh Ranjbar Pirbasti to enable fault-aware modelling
 
 */
public class VM_Data {

    public static int totalVMCount = 0;
    private String vm_Name;
    private int vm_id;
    private double vm_Speed;
    private double vm_Cost;
    //private int[] vm_communication;
    private boolean vm_used;
    private boolean process_used;
    private int num_Processor;
    private ArrayList<Processor> processors = new ArrayList<Processor>();
    public ArrayList<Job> vm_Jobs = new ArrayList<Job>();
    public double mttr;
    public double lambda;
    public int mechanismFaultandRecovery;

    //public VM_Data(String name, int id, double cost, double speed, int[] communication, int num_Processor) {
    public VM_Data(String name, int id, double cost, double speed, int num_Processor, double repair, double lambdaa,int mechanism) {
        totalVMCount++;
        this.vm_Name = name;
        this.vm_id = id;
        this.vm_Speed = speed;
        this.vm_Cost = cost;
        //this.vm_communication = communication;
        this.process_used = false;
        this.num_Processor = num_Processor;
        this.vm_used = false;
        this.mttr = repair;
        this.lambda = lambdaa;
        this.mechanismFaultandRecovery = mechanism;
        Resource_Initialize();
    }
    
    public void removeAllJobs(){
        Job.removeAllJobs();
        vm_Jobs.clear();
        Resource_Initialize();
    }
    public double getVM_total_communication_time(){
        double total_communication_time = 0.0;
        for(Processor p: processors){
           total_communication_time = total_communication_time + p.getTotalTransmissionTime();
        }
        return total_communication_time;
    }
    public boolean isVm_used() {
        return vm_used;
    }

    public void setVm_used(boolean vm_used) {
        this.vm_used = vm_used;
    }

    //olivia: start
    public Job get_sent_Job(String job) {
        for (Job j : vm_Jobs) {
            if (j.getName().contains(job) && j.getName().contains("Send")) {
                return j;
            }
        }
        return null;
    }
    //olivia: end

    public void set_VM_Processor_Idle_Time(double RT) {
        for (Processor p : processors) {
            p.setProcessor_Idle_Time(RT - p.getTotalComputationTime() - p.getTotalTransmissionTime());;
        }
    }

    public void addJobs(Job job) {
        vm_Jobs.add(job);
    }

    public int getTotalJobs() {
        return vm_Jobs.size();
    }

    public boolean isProcess_used() {
        return process_used;
    }

    public void setProcess_used(boolean process_used) {
        this.process_used = process_used;
    }

    public void Resource_Initialize() {
        processors.removeAll(processors);
        for (int i = 0; i < getNum_Processor(); i++) {
            processors.add(new Processor("( " + getVm_Name() + "-processor-" + i + " )"));
        }
    }

    //Returns a processor that is free at time t (starts looking from index 0)
    public Processor getFreeProcessor(double time) {
        for (Processor p : processors) {
            if (p.isFree(time)) {
                return p;
            }
        }
        return null;
    }

    public double get_VM_Energy() {
        double energy = 0;
        for (Processor p : processors) {
          energy = energy+p.getTotal_Energy();
          //System.out.println(p.toString());
        }
        return energy;
    }

    public double get_VM_Cost() {
        return this.vm_Cost;
    }

    public void printAllJobs() {
        for (Job job : vm_Jobs) {
            System.out.println(job.oneLinetoString());
            //System.out.println(job.toString());

        }
        System.out.println("\n");
    }

    //Returns the minimum time when a processor will be free
    public double getTimeForAvailableProcessor() {
        ArrayList<Double> freeTimes = new ArrayList<Double>(processors.size());
        for (int i = 0; i < processors.size(); i++) {
            Processor p = processors.get(i);
            freeTimes.add(p.getTimeWhenItIsFree());
        }
        return Collections.min(freeTimes);
    }

    public String getVm_Name() {
        return vm_Name;
    }

    public static int getTotalVMCount() {
        return totalVMCount;
    }

    public int getNum_Processor() {
        return num_Processor;
    }

    public int getVm_id() {
        return vm_id;
    }

    public void setNum_Processor(int num_Processor) {
        this.num_Processor = num_Processor;
        Resource_Initialize();
    }

    /*public int[] getVm_communication() {
        return vm_communication;
    }*/

    public void setVm_id(int vm_id) {
        this.vm_id = vm_id;
    }

    public double getLast_Job_End_Time() {
        //olivia: start
        //return vm_Jobs.get(vm_Jobs.size() - 1).getEndTime();
        return vm_Jobs.get(vm_Jobs.size() - 1).getEndTime(null);
        //olivia: end
    }

    /*public void setVm_communication(int[] vm_communication) {
        this.vm_communication = vm_communication;
    }*/

    public double getVm_Speed() {
        return vm_Speed;
    }

    public double getVm_Cost() {
        return vm_Cost;
    }

    public void setVm_Speed(double vm_Speed) {
        this.vm_Speed = vm_Speed;
    }

    public void setVm_Cost(double vm_Cost) {
        this.vm_Cost = vm_Cost;
    }

    public String processors_toString() {
        String result = "";
        for (Processor p : processors) {
            result = result + p.toString() + "\n";
        }
        return result;
    }

    @Override
    public String toString() {
        return "VM_Information:{" +vm_Name+ ", vm_id= " + vm_id + ", vm_Speed= " + vm_Speed +", vm_Processor= " +num_Processor+ ", vm_Cost= $" + vm_Cost + '}';
    }

}
