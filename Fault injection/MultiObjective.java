/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PerformanceMeasure;

 /**
 *
 
 Original simulator by Ismail Sheikh 
 Enhanced by Marzieh Ranjbar Pirbasti to enable fault-aware modelling
 
 */

// in this file I have just added some new vaiables to pass them to job
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.util.Pair;
import org.apache.commons.lang3.ArrayUtils;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.problem.AbstractProblem;

public class MultiObjective  {

    private final ArrayList<Problem_Model> solutionModel_List;
    private final ArrayList<VM_Data> VM_List;
    public final Map<String, VM_Data> linkNameTolinkVMmap;
    private final ArrayList<VM_Data> Jobs = new ArrayList<VM_Data>();
    private int[] taskArray;
    
    private static final int numberOfVariable = Problem_Model.getTaskId();
    static int count = 0;
    private int numobj = 0;
    public double total_rt = 0;
    public double total_e = 0;
    private double dc=0;
    private double ov=0;
    private double lambdaF=0;
    private double lambdamVm1=0;
    private double lambdamVm2=0;
    private double lambdaVm1Vm2=0;
    private double linkRepair=0;
    //marzieh added
    public  ArrayList<Double> vm1d = new ArrayList<Double>();
    public  ArrayList<Double> vm2d = new ArrayList<Double>();
    public  ArrayList<Double> lvm1d = new ArrayList<Double>();
    public  ArrayList<Double> lvm2d = new ArrayList<Double>();
    public ArrayList<Double> vm1vm2d = new ArrayList<Double>();
    
    public ArrayList<Double> mobilityfails = new ArrayList<Double>();
    
    public  ArrayList<Double> vm1d_duration = new ArrayList<Double>();
    public  ArrayList<Double> vm2d_duration = new ArrayList<Double>();
    public  ArrayList<Double> lvm1d_duration = new ArrayList<Double>();
    public  ArrayList<Double> lvm2d_duration = new ArrayList<Double>();
    public ArrayList<Double> vm1vm2d_duration = new ArrayList<Double>();
    public ArrayList<Double> mobilityfails_duration = new ArrayList<Double>();
    //private static boolean first_population=true;
    public Map<Double, Pair<Double, Double> > mymap = new LinkedHashMap <Double, Pair<Double, Double> >();
    
    
    

    
    public MultiObjective(ArrayList<Problem_Model> SM_List, ArrayList<VM_Data> vm_List, Map<String, VM_Data> linkNameTolinkVMmap, int numobj, 
    		double dc, double ov, double lambdaf, double lambdamVM1, double lambdamVM2, double lambdaVM1VM2, double LinkRepair,
    		ArrayList<Double> VmOneDownTimes, ArrayList<Double> VmTwoDownTimes,
    		 ArrayList<Double>  LinkLocalVM1DownTimes, ArrayList<Double> LinkLocalVM2DownTimes,  ArrayList<Double> LinkLocalVM1VM2,
    		 ArrayList<Double> VmOneDownTimes_duration, ArrayList<Double> VmTwoDownTimes_duration,
       		 ArrayList<Double>  LinkLocalVM1DownTimes_duration, ArrayList<Double> LinkLocalVM2DownTimes_duration,  ArrayList<Double> LinkLocalVM1VM2_duration,
       		ArrayList<Double> mymobilityfails,  ArrayList<Double> mymobilityfails_duration) {
        super();
        this.solutionModel_List = SM_List;
        this.VM_List = vm_List;
        this.linkNameTolinkVMmap = linkNameTolinkVMmap;
        this.numobj = numobj;
        //marzieh added
        this.dc=dc;
        this.ov=ov;
        this.lambdaF=lambdaf;
        this.lambdamVm1=lambdamVM1;
        this.lambdamVm2=lambdamVM2;
        this.lambdaVm1Vm2=lambdaVM1VM2;
        this.linkRepair=LinkRepair;
        this.vm1d = VmOneDownTimes;
        this.vm2d = VmTwoDownTimes;
        this.lvm1d = LinkLocalVM1DownTimes;
        this.lvm2d = LinkLocalVM2DownTimes;
        this.vm1vm2d = LinkLocalVM1VM2;
        this.mobilityfails = mymobilityfails;
        
        this.vm1d_duration = VmOneDownTimes_duration;
        this.vm2d_duration = VmTwoDownTimes_duration;
        this.lvm1d_duration = LinkLocalVM1DownTimes_duration;
        this.lvm2d_duration = LinkLocalVM2DownTimes_duration;
        this.vm1vm2d_duration = LinkLocalVM1VM2_duration;
        this.mobilityfails_duration = mymobilityfails_duration;
    }
    // Returns number of variables in the system
    public static int getNumberOfVariable() {
        return numberOfVariable;
    }
    //Return list of Solution Model
    public ArrayList<Problem_Model> getSolutionModel_List() {
        return solutionModel_List;
    }
    //returns list of VMs in the model
    public ArrayList<VM_Data> getVM_List() {
        return VM_List;
    }
    //returns list of configuration tasks
    public int[] getTaskArray() {
        return taskArray;
    }
    // Create new solutions in the population
   
    
    //Generate send receive and execute jobs based on their execution and communication times
    public void generateJob() {
        total_rt = 0;
        /*taskArray[0]= 2; taskArray[1]= 2; taskArray[2]= 1; taskArray[3]= 2; 
        taskArray[4]= 1; taskArray[5]= 2; taskArray[6]= 2; taskArray[7]= 0;
        taskArray[8]= 1; taskArray[9]= 2; taskArray[10]= 0; taskArray[11]= 0;
        taskArray[12]= 2; taskArray[13]= 0; taskArray[14]= 0; */
        
        ArrayList<Job> end_node_jobs = new ArrayList<Job>();
        ArrayList<Job> totalJobs = new ArrayList<Job>();
        for (int curr = 0; curr < solutionModel_List.size(); curr++) {
            int levelOfTask = solutionModel_List.get(curr).getLevel();
            ArrayList<Job> receiveJobs = new ArrayList<Job>();
            int i = 0;
            VM_Data vm = VM_List.get(taskArray[solutionModel_List.get(curr).getNodeId()]);
            for (int dependentNode : solutionModel_List.get(curr).getDepend_Node_id()) {
                int currNodeId = taskArray[solutionModel_List.get(curr).getNodeId()];
                int depNodeId = taskArray[solutionModel_List.get(dependentNode).getNodeId()];
                if (currNodeId != depNodeId) {
                    String linkVmName;
                    if(currNodeId < depNodeId)
                        linkVmName = VM_List.get(currNodeId).getVm_Name() + VM_List.get(depNodeId).getVm_Name();
                    else
                        linkVmName = VM_List.get(depNodeId).getVm_Name() + VM_List.get(currNodeId).getVm_Name();
                    
                    VM_Data linkVm = linkNameTolinkVMmap.get(linkVmName);
                    Job sentJob = linkVm.get_sent_Job(solutionModel_List.get(dependentNode).getNodeId() + " - " + solutionModel_List.get(curr).getNodeId());
                    
                    //Callback c = new Callback(sentJob::getEndTime);
                    //int index = solutionModel_List.get(solutionModel_List.get(curr).getDepend_Node_id()[i]).getNext_Node_id().indexOf(curr);
                    //Job newReceiveJob = new Job(sentJob.getDependencyLevel() + 1, "Received if ( " + solutionModel_List.get(dependentNode).getNodeId() + " - " + solutionModel_List.get(curr).getNodeId()
                    //        + " ),  VM_Name =" + vm.getVm_Name() + " with processors =" + vm.getNum_Processor(), false, solutionModel_List.get(dependentNode).getCommunication_data()[index], c, vm);
                    //receiveJobs.add(newReceiveJob);
                    //vm.addJobs(newReceiveJob);
                    receiveJobs.add(sentJob);
                }
                else {
                    receiveJobs.add(totalJobs.get(dependentNode));
                }
                i++;
            }
            /*if (receiveJobs.isEmpty()) {
                if (solutionModel_List.get(curr).isDependency()) {
                    for (int id : solutionModel_List.get(curr).getDepend_Node_id()) {
                        receiveJobs.add(totalJobs.get(id));
                    }
                }
            }*/
            
            
            
            TaskExecuteCallback tec = new TaskExecuteCallback(Job::getMaxEndTime, receiveJobs);
            Job newExeJob = new Job((2 * levelOfTask - 1), "Execute" + solutionModel_List.get(curr).getNodeId(), true,
                    solutionModel_List.get(curr).getExecutionTime() / (VM_List.get(taskArray[curr]).getVm_Speed()), tec, vm, "",
                    dc, ov,0, 0, 0,0,0, VM_List.get(taskArray[curr]).getVm_id(),vm1d, vm2d,
           		 lvm1d, lvm2d,  vm1vm2d,vm1d_duration, vm2d_duration,
           		 lvm1d_duration, lvm2d_duration,  vm1vm2d_duration, mobilityfails, mobilityfails_duration);
            
            if (solutionModel_List.get(curr).getNext_Node_id().isEmpty()) {
                end_node_jobs.add(newExeJob);
            }
            totalJobs.add(newExeJob);
            vm.addJobs(newExeJob);
            Callback c = new Callback(newExeJob::getEndTime);

            i = 0;
            for (int dependentNode : solutionModel_List.get(curr).getNext_Node_id()) {
                int currNodeId = taskArray[solutionModel_List.get(curr).getNodeId()];
                int nextNodeId = taskArray[solutionModel_List.get(dependentNode).getNodeId()];
                if (currNodeId != nextNodeId) {
                    String linkVmName;
                    if(currNodeId < nextNodeId)
                        linkVmName = VM_List.get(currNodeId).getVm_Name() + VM_List.get(nextNodeId).getVm_Name();
                    else
                        linkVmName = VM_List.get(nextNodeId).getVm_Name() + VM_List.get(currNodeId).getVm_Name();
                    
                    VM_Data linkVm = linkNameTolinkVMmap.get(linkVmName);
                    
                    Job newSendJob = new Job(newExeJob.getDependencyLevel() + 1, "Send ( " + solutionModel_List.get(curr).getNodeId() + " - " + solutionModel_List.get(dependentNode).getNodeId() + " ) VM_Name =" + linkVm.getVm_Name()
                    + " with processors =" + linkVm.getNum_Processor(), false, solutionModel_List.get(curr).getCommunication_data()[i], c, linkVm,linkVmName, 
                    dc,ov, lambdaF,lambdamVm1,lambdamVm2, lambdaVm1Vm2,linkRepair,VM_List.get(taskArray[curr]).getVm_id(),vm1d, vm2d,
              		 lvm1d, lvm2d,  vm1vm2d,vm1d_duration, vm2d_duration,
               		 lvm1d_duration, lvm2d_duration,  vm1vm2d_duration,mobilityfails, mobilityfails_duration);
                    linkVm.addJobs(newSendJob);
                   
                }
                i++;
            }
        }
        int maxTaskLevel = Problem_Model.maxLevel;
        //Job.processJobs((3 * maxTaskLevel - 2));
        Job.processJobs((2 * maxTaskLevel) - 1);
 
        for (Job elem : end_node_jobs) {
            if (total_rt < elem.getEndTime(null)) { 
                total_rt = elem.getEndTime(null);
            }
        }
    }
    // Evaluate each solution in the population for energy, cost and response time
   
    public void evaluate(int []  allocation) {
        //Reinitalize the problem 
        count++;
        String var="";
    
     //   allocation= new int[numberOfVariable];,
        taskArray = new int[numberOfVariable];
        for(int j=0; j<numberOfVariable;j++)
        {
        	taskArray[j]=allocation[j];
        }
    /* taskArray[0] = 2;
        taskArray[1] = 2;
        taskArray[2] = 2;
        taskArray[3] = 2;
        taskArray[4] = 1;
        taskArray[5] = 2;
        taskArray[6] = 2;
        taskArray[7] = 0;  
        taskArray[8] = 1; 
        taskArray[9] = 2;
        taskArray[10] = 0;
        taskArray[11] = 0;
        taskArray[12] = 2;
        taskArray[13] = 0;
        taskArray[14] = 0;
        */
        for (int i = 0; i < numberOfVariable; i++) {
            var+=taskArray[i]+", ";
            VM_List.get(taskArray[i]).setVm_used(true);   
        }
        

        generateJob();
        double energy = 0;
        double cost = 0;
        double wirelesscost = 0;

        for (VM_Data vm : VM_List) {
            vm.set_VM_Processor_Idle_Time(total_rt);
            if (vm.getVm_Name().contains("Local")) {
                energy += vm.get_VM_Energy();
            }
            if (vm.getVm_id() == 0) {
                double comunication_time = vm.getVM_total_communication_time();
                double bandwidth_use = RunMeasure.wireless_speed * comunication_time;
                if ((bandwidth_use) > RunMeasure.allowable_bandwidth) {
                    wirelesscost += (bandwidth_use - RunMeasure.allowable_bandwidth) * RunMeasure.overlimit_charges;
                }
            }
            if (vm.isVm_used() && (vm.getVm_id() != 0)) {
                cost += total_rt * vm.get_VM_Cost();
            }
        }
        for (VM_Data vm : linkNameTolinkVMmap.values()) {
            if (vm.getVm_Name().contains("Local")) {
                energy += vm.get_VM_Energy();
            }
        }
        wirelesscost += RunMeasure.wireless_charges;
        cost += wirelesscost;
     
      
        total_e = energy;

        
        for (VM_Data vm : VM_List) {
            vm.setVm_used(false);
            vm.removeAllJobs();
        }
        
        for (VM_Data linkvm : linkNameTolinkVMmap.values()) {
            linkvm.setVm_used(false);
            linkvm.removeAllJobs();
        }
    }

}
