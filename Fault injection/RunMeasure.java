/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PerformanceMeasure;

import java.io.BufferedReader;
/**
 *
 
 Original simulator by Ismail Sheikh 
 Enhanced by Marzieh Ranjbar Pirbasti to enable fault-aware modelling
 
 */
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.moeaframework.Executor;
import org.moeaframework.algorithm.NSGAII;
import org.moeaframework.analysis.plot.Plot;
import org.moeaframework.core.Algorithm;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.NondominatedSortingPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.comparator.ChainedComparator;
import org.moeaframework.core.comparator.CrowdingComparator;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.operator.GAVariation;
import org.moeaframework.core.operator.RandomInitialization;
import org.moeaframework.core.operator.TournamentSelection;
import org.moeaframework.core.operator.real.PM;
import org.moeaframework.core.operator.real.SBX;
import org.moeaframework.core.variable.EncodingUtils;
import java.util.concurrent.TimeUnit;

//import org.moeaframework.algorithm.single.AggregateObjectiveComparator;
//import org.moeaframework.algorithm.single.GeneticAlgorithm;
//import org.moeaframework.algorithm.single.LinearDominanceComparator;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.operator.TwoPointCrossover;
import org.moeaframework.core.operator.permutation.PMX;
import org.moeaframework.core.operator.real.PCX;
import org.moeaframework.core.operator.real.UM;
import org.moeaframework.core.operator.subset.SSX;
import org.moeaframework.util.TypedProperties;




public class RunMeasure {
    /*
     *Global variable of the RunMeasure Object
     */

	//these values should be updated if we want to experiment with different repair times etc
	
	//VM repair time
	public static double vmrepair_tbd = 60.0;
	
	// ov is checkpointing overhead
	public static double ov=0.05;
	
	//cost of disconnecting due to mobility
    public static double dc=1 ;
	 
	//link repair time    
	public static double linkRepair=1;
	    
	
	public static ArrayList<Double> readfile(String filename)
	{
		ArrayList<Double> result = new ArrayList<Double>();
        try {
        	String line ="";
            FileReader fileReader =  new FileReader(filename);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
            	if (line.compareTo("") == 0) break;
                double temp = Double.parseDouble(line);
                result.add(temp);
            }   
            bufferedReader.close();         
        }
        
        catch(Exception e) {
            System.out.println("couldn't open the "+filename+" text file.");
            System.exit(-2);
        }
        return result;
	}
    private static ArrayList<Problem_Model> solutionModel_List = new ArrayList<Problem_Model>();
    private static ArrayList<VM_Data> VM = new ArrayList<VM_Data>();
    
    public static final double computation_Energy_charge = .9;
    public static final double transmission_Energy_charge = 1.3;
    public static final double idle_Energy_charge = .3;
    public static final double wireless_charges = 0;
    public static final double allowable_bandwidth = 1024; // in MBs (1Mb)
    public static final double overlimit_charges = 0.3; // 30c/ MB overlimit
    public static final double wireless_speed = 1; // 50Mbps
    public static String folder_name;
    public static final String tabs = "\t";
    
//these are for initialization
    public static double lambdaF=0;
    public static double lambdaMVm1=0;
    public static double lambdaMVm2=0;
    public static double LambdaVm1Vm2=0;
    public static int allocationinput=-1;

    /*
     execute_Nodes_together = first element in the array will be vm ID, all other elements will be procssed in first element VM id
     Example
     public static ArrayList<int[]> execute_Nodes_together = new ArrayList(Arrays.asList(new int[]{1, 3, 4}, new int[] {2, 5, 6}));
     in the above initialization, Node 3 and 4 will be processed in VM1 and Node 5 and 6 will be processed in VM 2
     */
    public static ArrayList<int[]> execute_Nodes_together;
    /*
     Number_Of_Resources = 0 -> Run the execution with LOCAL ONLY
     Number_Of_Resources = 1 -> Run the execution with LOCAL AND 1 VM
     Number_Of_Resources = 2 -> Run the execution with LOCAL AND 2 VM
     core_of_Each_Resource = 1 -> All sources will have 1 processors
     core_of_Each_Resource = 2 -> All sources will have 2 processors
     core_of_Each_Resource = 4 -> All sources will have 4 processors
     core_of_Each_Resource = 8 -> All sources will have 8 processors
     callGraph_executionGraph = False -> ExecutionGraph, True -> CallGraph
     */
    private static final int core_of_Each_Resource = 1;
    private static final int Number_Of_Resources = 2;
    private static final double local_cost = 0 * core_of_Each_Resource;
    private static final double vm1_cost = .01 * core_of_Each_Resource;
    private static final double vm2_cost = .02 * core_of_Each_Resource;
    /*
     *callGraph_executionGraph:
     *Execution Graph: starts from base and moves to the leaves
     *Call_Graph: starts from leave and moves to the node. 
     */
    private static final boolean callGraph_executionGraph = false;
    //private static final int starting_Population_Size = 1000;
    //private static final int number_of_iteration = 10;
    private static final boolean print_output_file = false;
    private static Solution bestAllocation;

    public static void main(String[] args) {
        /*
         Problem Configuration:
         Node_id is dependent on the task graph. The task graph has 15 different nodes in this example
         */
    	
    //	lambdaMVm1=Double.parseDouble(args[0]);
    //	lambdaMVm1=Double.parseDouble(args[0]);
    //	LambdaVm1Vm2=Double.parseDouble(args[0]);
    //	lambdaF = Double.parseDouble(args[1]);
    	
        //System.out.println("Change Flag 'print_output_file=false' to redirect the ouptut to the console");
        
        
        //int[] Node_id = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};
        int[] Node_id = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        String strcallGraph_executionGraph = "";
        if (callGraph_executionGraph) { /*
            //execute_Nodes_together arraylist refer to number of task need to be forces to VM. 
            // First element refer to VM to be execute the task
            // Remaining elements are the node ids of the tasks
            //e.g new int[]{0, 0, 1} Task 0 and 1 will be executed in local mobile device (0)
            execute_Nodes_together = new ArrayList(Arrays.asList(new int[]{0, 0, 1}, new int[]{1}, new int[]{2}));
            strcallGraph_executionGraph = "Call_Graph";
            double[] response_Time = {1555.3, 137.8, 1464, 35.9, 80.7, 722.2, 37.2, 75.2, 516.5, 192, 77.7, 516.5, 2.2, 33, 68.6};
            boolean[] is_Node_dependent = {false, true, true, true, true, true, true, true, true, true, true, true, true, true, true};
            int[][] next_Node_id = {new int[]{1, 2}, new int[]{3, 4}, new int[]{5}, new int[]{6}, new int[]{7}, new int[]{8, 9}, new int[]{}, new int[]{10}, new int[]{11}, new int[]{}, new int[]{},
            new int[]{12, 13, 14}, new int[]{}, new int[]{}, new int[]{}};
            double[][] communication_data = {new double[]{0.29, 0.2}, new double[]{600, 1024.2}, new double[]{10206}, new double[]{0.0}, new double[]{675.2}, new double[]{10204, 10206}, new double[]{0},
            new double[]{0.0}, new double[]{19806}, new double[]{0.0}, new double[]{0.0}, new double[]{3.0, 12000, 12003}, new double[]{0.0}, new double[]{0.0}, new double[]{0.0}};
            int[][] dependent_Node_id = {new int[]{}, new int[]{0}, new int[]{0}, new int[]{1}, new int[]{1}, new int[]{2}, new int[]{3}, new int[]{4}, new int[]{5}, new int[]{5}, new int[]{7},
            new int[]{8}, new int[]{11}, new int[]{11}, new int[]{11}};
            //converting communitication time from ms to seconds
            for (int j = 0; j < communication_data.length; j++) {
                for (int i = 0; i < communication_data[j].length; i++) {
                    communication_data[j][i] = communication_data[j][i] / 1000.0;
                }
            }
            //Converting response time from milli-sec to seconds
            for (int i = 0; i < response_Time.length; i++) {
                response_Time[i] = response_Time[i] / 1000.0;
            }
            // Verify and validate the input, no missing parameter 
            if ((Node_id.length == response_Time.length) && (Node_id.length == is_Node_dependent.length) && (Node_id.length == next_Node_id.length) && (Node_id.length == dependent_Node_id.length)
                    && (Node_id.length == communication_data.length)) {
                for (int i = 0; i < Node_id.length; i++) {
                    solutionModel_List.add(new Problem_Model(Node_id[i], response_Time[i], is_Node_dependent[i], dependent_Node_id[i], next_Node_id[i], communication_data[i]));
                }
            } else {
                throw new IllegalArgumentException("Input Sizes are not equal");
            }
            
            //set levels to each tasks in the tasksgraph
             
            solutionModel_List.get(0).setLevel(1);
            solutionModel_List.get(1).setLevel(2);
            solutionModel_List.get(2).setLevel(2);
            solutionModel_List.get(3).setLevel(3);
            solutionModel_List.get(4).setLevel(3);
            solutionModel_List.get(5).setLevel(3);
            solutionModel_List.get(6).setLevel(4);
            solutionModel_List.get(7).setLevel(4);
            solutionModel_List.get(8).setLevel(4);
            solutionModel_List.get(9).setLevel(4);
            solutionModel_List.get(10).setLevel(5);
            solutionModel_List.get(11).setLevel(5);
            solutionModel_List.get(12).setLevel(6);
            solutionModel_List.get(13).setLevel(6);
            solutionModel_List.get(14).setLevel(6); */
        } else { /*
            //execute_Nodes_together arraylist refer to number of task need to be forces to VM. 
            // First element refer to VM to be execute the task
            // Remaining elements are the node ids of the tasks
            //e.g new int[]{0, 0, 1} Task 0 and 1 will be executed in local mobile device (0)
            strcallGraph_executionGraph = "ExecutionGraph";
            execute_Nodes_together = new ArrayList(Arrays.asList(new int[]{0, 13, 14}, new int[]{1}, new int[]{2}));
            double[] response_Time = {68.6, 33, 2.2, 516.6, 77.7, 192, 516.5, 75.2, 37.2, 722.2, 80.7, 35.9, 1464, 137.8, 1555.3};
            boolean[] is_Node_dependent = {false, false, false, true, false, false, true, true, false, true, true, true, true, true, true};
            int[][] next_Node_id = {new int[]{3}, new int[]{3}, new int[]{3}, new int[]{6}, new int[]{7}, new int[]{9}, new int[]{9}, new int[]{10}, new int[]{11},
            new int[]{12}, new int[]{13}, new int[]{13}, new int[]{14}, new int[]{14}, new int[]{}};

            double[][] communication_data = {new double[]{12003}, new double[]{12000}, new double[]{3.0}, new double[]{19806}, new double[]{0.0},
            new double[]{10206}, new double[]{10204}, new double[]{675.2}, new double[]{0.0}, new double[]{10206}, new double[]{1024.2}, new double[]{600},
            new double[]{0.2}, new double[]{0.29}, new double[]{0.0}};
            int[][] dependent_Node_id = {new int[]{}, new int[]{}, new int[]{}, new int[]{0, 1, 2}, new int[]{}, new int[]{}, new int[]{3}, new int[]{4},
            new int[]{}, new int[]{5, 6}, new int[]{7}, new int[]{8}, new int[]{9}, new int[]{10, 11}, new int[]{12, 13}};

            //Converting communication time from milli-sec to seconds
            for (int j = 0; j < communication_data.length; j++) {
                for (int i = 0; i < communication_data[j].length; i++) {
                    communication_data[j][i] = communication_data[j][i] / 1000.0;
                }
            }
            for (int i = 0; i < response_Time.length; i++) {
                response_Time[i] = response_Time[i] / 1000.0;
            }
            //Converting response time from milli-sec to seconds
            if ((Node_id.length == response_Time.length) && (Node_id.length == is_Node_dependent.length) && (Node_id.length == next_Node_id.length) && (Node_id.length == dependent_Node_id.length)
                    && (Node_id.length == communication_data.length)) {
                for (int i = 0; i < Node_id.length; i++) {
                    solutionModel_List.add(new Problem_Model(Node_id[i], response_Time[i], is_Node_dependent[i], dependent_Node_id[i], next_Node_id[i], communication_data[i]));
                }
            } else {
                throw new IllegalArgumentException("Input Sizes are not equal");
            }
            
            //set levels to each tasks in the tasksgraph
             
            solutionModel_List.get(0).setLevel(1);
            solutionModel_List.get(1).setLevel(1);
            solutionModel_List.get(2).setLevel(1);
            solutionModel_List.get(3).setLevel(2);
            solutionModel_List.get(4).setLevel(2);
            solutionModel_List.get(5).setLevel(3);
            solutionModel_List.get(6).setLevel(3);
            solutionModel_List.get(7).setLevel(3);
            solutionModel_List.get(8).setLevel(3);
            solutionModel_List.get(9).setLevel(4);
            solutionModel_List.get(10).setLevel(4);
            solutionModel_List.get(11).setLevel(4);
            solutionModel_List.get(12).setLevel(5);
            solutionModel_List.get(13).setLevel(5);
            solutionModel_List.get(14).setLevel(6); */
        	
        	/* Olivia added */
        	//BEGIN Random Workflow Graph
        	
        	strcallGraph_executionGraph = "ExecutionGraph";

        	execute_Nodes_together = new ArrayList(Arrays.asList(new int[]{0, 0}, new int[]{1}, new int[]{2}));

        	double[] response_Time = {3.613, 0.983, 3.339, 3.894, 3.097, 3.119, 1.191, 2.441, 3.405, 2.627, 2.49};

        	boolean[] is_Node_dependent = {false, false, true, true, true, true, true, true, true, true, true};

        	int[][] next_Node_id = {new int[]{2, 7, 8}, new int[]{2, 5, 6, 10}, new int[]{3, 4, 5, 8, 9, 10}, new int[]{4, 5, 6, 7, 9}, new int[]{7, 8}, new int[]{6, 8, 9}, new int[]{8, 10}, new int[]{8}, new int[]{}, new int[]{10}, new int[]{}};

        	double[][] communication_data ={new double[]{3.613, 0.983, 3.339}, new double[]{3.894, 3.097, 3.119, 1.191}, new double[]{2.441, 3.405, 2.627, 2.49, 4.955, 4.462}, new double[]{1.745, 2.353, 2.851, 3.227, 3.928}, new double[]{0.765, 0.616}, new double[]{2.072, 1.441, 3.127}, new double[]{3.66, 2.435}, new double[]{0.769}, new double[]{0.0}, new double[]{0.158}, new double[]{0.0}};

        	int[][] dependent_Node_id = {new int[]{}, new int[]{}, new int[]{0, 1}, new int[]{2}, new int[]{2, 3}, new int[]{1, 2, 3}, new int[]{1, 3, 5}, new int[]{0, 3, 4}, new int[]{0, 2, 4, 5, 6, 7}, new int[]{2, 3, 5}, new int[]{1, 2, 6, 9}};

        	if ((Node_id.length == response_Time.length) && (Node_id.length == is_Node_dependent.length) && (Node_id.length == next_Node_id.length) && (Node_id.length == dependent_Node_id.length) && (Node_id.length == communication_data.length)) {
        	    for (int i = 0; i < Node_id.length; i++) {
        	        solutionModel_List.add(new Problem_Model(Node_id[i], response_Time[i], is_Node_dependent[i], dependent_Node_id[i], next_Node_id[i], communication_data[i]));
        	    }
        	} else {
        	    throw new IllegalArgumentException("Input Sizes are not equal");
        	}

        	solutionModel_List.get(0).setLevel(1);
        	solutionModel_List.get(1).setLevel(1);
        	solutionModel_List.get(2).setLevel(2);
        	solutionModel_List.get(3).setLevel(3);
        	solutionModel_List.get(4).setLevel(4);
        	solutionModel_List.get(5).setLevel(4);
        	solutionModel_List.get(6).setLevel(5);
        	solutionModel_List.get(7).setLevel(5);
        	solutionModel_List.get(8).setLevel(6);
        	solutionModel_List.get(9).setLevel(5);
        	solutionModel_List.get(10).setLevel(6);



        	
        	//END Random Workflow Graph*/
        }
        for (int[] array_values : execute_Nodes_together) {
            if (new HashSet<>(IntStream.of(array_values).boxed().collect(Collectors.toList())).retainAll(IntStream.of(Node_id).boxed().collect(Collectors.toList()))) {
                throw new IllegalArgumentException("Invalid Node for Node execution together List, Node is out of range in Array -> " + Arrays.toString(array_values));
            }
        }
        
        //Marzieh added code for getting faults and their duration from cmd 
        
        //Setting Vm1 and VM2 down Times
        
        //10K milliseconds
        ArrayList<Double> VmOneDownTimes=new ArrayList<Double>();
        ArrayList<Double> VmTwoDownTimes=new ArrayList<Double>();
        ArrayList<Double> LinkLocalVM1DownTimes = new ArrayList<Double>();
        ArrayList<Double> LinkLocalVM2DownTimes = new ArrayList<Double>();
        ArrayList<Double> LinkVM1VM2 = new ArrayList<Double>();
        
        ArrayList<Double> mobfails = new ArrayList<Double>();
        
        ArrayList<Double> VmOneDownTimes_duration=new ArrayList<Double>();
        ArrayList<Double> VmTwoDownTimes_duration=new ArrayList<Double>();
        ArrayList<Double> LinkLocalVM1DownTimes_duration = new ArrayList<Double>();
        ArrayList<Double> LinkLocalVM2DownTimes_duration = new ArrayList<Double>();
        ArrayList<Double> LinkVM1VM2_duration = new ArrayList<Double>();
        
        ArrayList<Double> mobfails_duration = new ArrayList<Double>();
        
        int vm1downcounts = Integer.parseInt(args[0]);
        int vm2downcounts = Integer.parseInt(args[1]);
        
        //VmTwoDownTimes.add(0.0);
        

        
        VmOneDownTimes = readfile("failsvm1");
        if (VmOneDownTimes.size() != vm1downcounts)
        {
        	System.out.println("error passing information1.");
        	System.out.println(VmOneDownTimes.size());
        	System.out.println(vm1downcounts);
        	System.exit(-3);

        }
        VmOneDownTimes_duration = readfile("failsvm1d");
        
        //for (int i=0;i<vm1downcounts;i++)
        //VmOneDownTimes.add(Double.parseDouble(args[2+i]));
        //for (int i=0;i<vm1downcounts;i++)
        //VmOneDownTimes_duration.add(Double.parseDouble(args[2+vm1downcounts+i]));
        
        VmTwoDownTimes = readfile("failsvm2");
        if (VmTwoDownTimes.size() != vm2downcounts)
        {
        	System.out.println("error passing information2.");
        	System.out.println(VmTwoDownTimes.size());
        	System.out.println(vm2downcounts);
        	System.exit(-3);

        }
        VmTwoDownTimes_duration = readfile("failsvm2d");
        
        //VmTwoDownTimes.add(0.0);
       // for (int i=0;i<vm2downcounts;i++)
        //VmTwoDownTimes.add(Double.parseDouble(args[2*vm1downcounts+2+i]));
        //for (int i=0;i<vm2downcounts;i++)
        //VmTwoDownTimes_duration.add(Double.parseDouble(args[2*vm1downcounts+2+vm2downcounts+i]));
        
        //if running test for link failures

        //int linklocalvm1downcounts = Integer.parseInt(args[2+2*vm1downcounts+2*vm2downcounts]);
        //int linklocalvm2downcounts = Integer.parseInt(args[3+2*vm1downcounts+2*vm2downcounts]);
        //int linkvm1vm2downcounts = Integer.parseInt(args[4+2*vm1downcounts+2*vm2downcounts]);
        //int mobilitydowncounts = Integer.parseInt(args[5+2*vm1downcounts+2*vm2downcounts]);

        int linklocalvm1downcounts = Integer.parseInt(args[2]);
        int linklocalvm2downcounts = Integer.parseInt(args[3]);
        int linkvm1vm2downcounts = Integer.parseInt(args[4]);
        int mobilitydowncounts = Integer.parseInt(args[5]);
        
        
        
        LinkLocalVM1DownTimes = readfile("faillvm1");
        LinkLocalVM1DownTimes_duration = readfile("faillvm1d");
        LinkLocalVM2DownTimes = readfile("faillvm2");
        LinkLocalVM2DownTimes_duration = readfile("faillvm2d");
        
        LinkVM1VM2 = readfile("failvm1vm2");
        LinkVM1VM2_duration = readfile("failvm1vm2d");
        mobfails = readfile("failsmob");
        mobfails_duration = readfile("failsmobd");
        
        /*
        for (int i=0;i<linklocalvm1downcounts;i++)
        LinkLocalVM1DownTimes.add(Double.parseDouble(args[6+2*vm1downcounts+2*vm2downcounts+i]));
        for (int i=0;i<linklocalvm1downcounts;i++)
        LinkLocalVM1DownTimes_duration.add(Double.parseDouble(args[6+2*vm1downcounts+2*vm2downcounts+linklocalvm1downcounts+i]));        
        
        for (int i=0;i<linklocalvm2downcounts;i++)
        LinkLocalVM2DownTimes.add(Double.parseDouble(args[6+2*vm1downcounts+2*vm2downcounts+i+2*linklocalvm1downcounts]));
        for (int i=0;i<linklocalvm2downcounts;i++)
        LinkLocalVM2DownTimes_duration.add(Double.parseDouble(args[6+2*vm1downcounts+2*vm2downcounts+2*linklocalvm1downcounts+i+linklocalvm2downcounts]));        
        
        for (int i=0;i<linkvm1vm2downcounts;i++)
        LinkVM1VM2.add(Double.parseDouble(args[6+2*vm1downcounts+2*vm2downcounts+2*linklocalvm1downcounts+i+2*linklocalvm2downcounts]));
        for (int i=0;i<linkvm1vm2downcounts;i++)
        LinkVM1VM2_duration.add(Double.parseDouble(args[6+2*vm1downcounts+2*vm2downcounts+2*linklocalvm1downcounts+i+2*linklocalvm2downcounts+linkvm1vm2downcounts]));        
        
        for (int i=0;i<mobilitydowncounts;i++)
        mobfails.add(Double.parseDouble(args[6+2*vm1downcounts+2*vm2downcounts+2*linklocalvm1downcounts+i+2*linklocalvm2downcounts+2*linkvm1vm2downcounts]));
        for (int i=0;i<mobilitydowncounts;i++)
        mobfails_duration.add(Double.parseDouble(args[6+2*vm1downcounts+2*vm2downcounts+2*linklocalvm1downcounts+i+2*linklocalvm2downcounts+2*linkvm1vm2downcounts+mobilitydowncounts]));        
*/

        //int mechfailureinput = Integer.parseInt(args[6+2*vm1downcounts+2*vm2downcounts+2*linklocalvm1downcounts+2*linklocalvm2downcounts+2*linkvm1vm2downcounts+2*mobilitydowncounts]);        
        //allocationinput = Integer.parseInt(args[7+2*vm1downcounts+2*vm2downcounts+2*linklocalvm1downcounts+2*linklocalvm2downcounts+2*linkvm1vm2downcounts+2*mobilitydowncounts]);        

        int mechfailureinput = Integer.parseInt(args[6]);        
        allocationinput = Integer.parseInt(args[7]);        

        
        /*
         *VM Configuration: 
         */
        //Initializing default local only configuration
        String[] vm_Name = new String[]{"Local"};
        int[] vm_id = new int[]{0};
        double[] vm_cost = new double[]{local_cost};
        double[] vm_speed = new double[]{1};
        double[] mttr_array = new double[] {0};
        double[] lambda_array = new double[] {0};
        int[] mech_array = new int[] {0};
        //int[][] vm_communication = new int[][]{new int[]{0}};
        int[] num_of_Processors = new int[]{core_of_Each_Resource};
        String temp = "Local_Only";
        String config = "Configuration: Running Local ONLY";
        if (Number_Of_Resources == 2) {
            //Initializing local with 2 VMs
            vm_Name = new String[]{"Local", "VM1", "VM2"};
            vm_id = new int[]{0, 1, 2};
            vm_cost = new double[]{local_cost, vm1_cost, vm2_cost};
            vm_speed = new double[]{1, 2, 4};
            mttr_array = new double[] {0,vmrepair_tbd,vmrepair_tbd};
            //failure rate of vm
            lambda_array = new double[] {0,0,0}; 
            // with mech array we set the mechanism,
            // 1 means restart 
            //2 means checkpointing with 2 checkpoints
            //3 means checkpointing with 4 checkpoints
            mech_array = new int[] {0,mechfailureinput,mechfailureinput};
           
            
            //vm_communication = new int[][]{new int[]{0, 1, 1}, new int[]{1, 0, 1}, new int[]{1, 1, 0}};
            num_of_Processors = new int[]{core_of_Each_Resource, core_of_Each_Resource, core_of_Each_Resource};
            temp = "Local_2VM";
            config = "Configuration: Running Local with 2 VM's";
        } else if (Number_Of_Resources == 1) {
            //Initializing local with 1 VMs
            vm_Name = new String[]{"Local", "VM_1"};
            vm_id = new int[]{0, 1};
            vm_cost = new double[]{local_cost, vm1_cost};
            vm_speed = new double[]{1, 2};
            mttr_array = new double[] {0,1};
            lambda_array = new double[] {0, 0.1}; 
            // with mech array we set the mechanism,
            // 1 means restart 
            //2 means checkpointing with 2 checkpoints
            //3 means checkpointing 4 checkponts
            mech_array = new int[] {0,0};
            //vm_communication = new int[][]{new int[]{0, 1}, new int[]{1, 0}};
            num_of_Processors = new int[]{core_of_Each_Resource, core_of_Each_Resource};
            temp = "Local_1VM";
            config = "Configuration: Running Local with 1 VM's";

        }
        //System.out.println(config);
        /*
         *Redirect Console output to log file using switch print_output_file variable
         */
        String path = "";
        try {
            path = new File("").getCanonicalFile().getParent();
        } catch (IOException ex) {
            Logger.getLogger(RunMeasure.class.getName()).log(Level.SEVERE, null, ex);
        }
        folder_name = path + "\\Queuing_Network\\" + strcallGraph_executionGraph + "\\" + core_of_Each_Resource + "-core\\" + temp;
        //System.out.println(folder_name);
        new File(folder_name).mkdirs();
        try {
            if (print_output_file) {
                System.setOut(new PrintStream(new FileOutputStream(folder_name + "\\System_Output.txt")));
            }
        } catch (Exception e) {
            System.out.println("Failed to write Output to text file");
        }
        /*
         * Vefiry and Validate VM initialization data
         */
        Map<String, VM_Data> linkNameTolinkVMmap = new LinkedHashMap <String, VM_Data>(); 
        if ((vm_Name.length == vm_cost.length) && (vm_id.length == vm_cost.length) && (vm_id.length == vm_speed.length) && (vm_id.length == num_of_Processors.length)) {
            for (int i = 0; i < vm_id.length; i++) {
                //VM.add(new VM_Data(vm_Name[i], vm_id[i], vm_cost[i], vm_speed[i], vm_communication[i], num_of_Processors[i]));
                VM.add(new VM_Data(vm_Name[i], vm_id[i], vm_cost[i], vm_speed[i], num_of_Processors[i], mttr_array[i], lambda_array[i], mech_array[i]));
                int count = 0;

                for (int j = i+1; j < vm_id.length; j++) { 
                   String linkName = vm_Name[i]+vm_Name[j];
                    linkNameTolinkVMmap.put(linkName, new VM_Data(linkName, count, 0.0, wireless_speed, 10,mttr_array[i], lambda_array[i],mech_array[i]));
                }
            }
        } else {
            throw new IllegalArgumentException("Input Array Sizes are not equal");
        }

        /*
         *Start Execution of the problem
         */
        //System.out.println("Execution_Setup:");
        //System.out.println(tabs + config);
        MultiObjective tsmp_Multi = new MultiObjective(solutionModel_List, VM, linkNameTolinkVMmap, 1,
        		dc,ov, lambdaF, lambdaMVm1,lambdaMVm2, LambdaVm1Vm2, linkRepair, VmOneDownTimes, VmTwoDownTimes,
        		LinkLocalVM1DownTimes, LinkLocalVM2DownTimes,LinkVM1VM2,
        		VmOneDownTimes_duration, VmTwoDownTimes_duration,
        		LinkLocalVM1DownTimes_duration, LinkLocalVM2DownTimes_duration,LinkVM1VM2_duration,
        		mobfails, mobfails_duration);
        runExecution(tsmp_Multi, 1, "", null);

    }
    
// here I have removed the GA
    public static void runExecution(MultiObjective tsmp, int multi, String name, ArrayList<Double> arrayList) {
        long lStartTime = System.nanoTime();
        PRNG.setSeed(78787878);
        

   /*int [] allocation0= {2,2,1,2,1,2,2,0,1,2,0,0,2,0,0};
   int [] allocation1= {1,1,2,1,2,1,1,0,2,1,0,0,1,0,0};
   int [] allocation2= {0,0,2,0,1,0,0,0,1,0,0,0,0,0,0};  
   int [] allocation3= {0,0,2,0,2,0,0,0,2,0,0,0,0,0,0};
   int [] allocation4= {0,0,0,0,2,0,0,0,1,0,0,0,0,0,0};
   int [] allocation5= {0,0,0,0,0,0,0,0,2,0,0,0,0,0,0};
   int [] allocation6= {1,1,0,1,2,1,1,0,2,1,0,0,1,0,0};
   int [] allocation7= {2,2,0,2,1,2,2,0,1,2,0,0,2,0,0};
   int [] allocation8= {1,1,0,1,2,1,1,0,0,1,0,0,1,0,0};*/
        
   //Olivia: added
   
   int [] allocation0= {0,2,2,2,2,2,2,2,2,2,2};
   int [] allocation1= {0,1,2,2,1,2,2,2,2,2,1};
    
   
  /* int [] allocation9= {2,2,2,2,1,2,2,0,1,2,0,0,2,0,0};
   int [] allocation10= {0,0,0,0,1,0,0,0,1,0,0,0,0,0,0};

   int [] allocation11= {0,0,0,0,1,0,0,0,1,0,0,0,0,0,0};
    
   int [] allocation12= {2,2,0,2,1,2,2,0,0,2,0,0,2,0,0};    
   */
   
   
 // I changed the evaluate function
   // no longer needed solution function
   // evaluate now gets an allocaion and calulate energy and response time for that
   if (allocationinput == 0)
     tsmp.evaluate(allocation0); 
   else if (allocationinput == 1)
	   tsmp.evaluate(allocation1);
   /*else if (allocationinput == 2)
	   tsmp.evaluate(allocation2);
   else if (allocationinput == 3)
	   tsmp.evaluate(allocation3);
   else if (allocationinput == 4)
	   tsmp.evaluate(allocation4);

   else if (allocationinput == 5)
	   tsmp.evaluate(allocation5);
   else if (allocationinput == 6)
	   tsmp.evaluate(allocation6);
   else if (allocationinput == 7)
	   tsmp.evaluate(allocation7);
   else if (allocationinput == 8)
	   tsmp.evaluate(allocation8);
   else if (allocationinput == 9)
	   tsmp.evaluate(allocation9);   
   else if (allocationinput == 10)
	   tsmp.evaluate(allocation10);
   else if (allocationinput == 11)
	   tsmp.evaluate(allocation11);   
   else if (allocationinput == 12)
	   tsmp.evaluate(allocation12);
	   */
 
   else System.exit(-1);
        System.out.println(tsmp.total_rt + " "+tsmp.total_e);
        

     /*
        
            

            
            System.out.println("Run " + num + ":");
            System.out.println("========");
            System.out.println("Tasks:");
            for (int i = 0; i < Problem_Model.getTaskId(); i++) {
                System.out.println(tabs + solutionModel_List.get(i).toString());
            }
            System.out.println("VMs:");
            for (VM_Data vm : VM) {
                System.out.println(tabs + vm.toString());
            }
            System.out.println("Near-Optimal Solution of this run:");
            // Get Execution Results
            NondominatedPopulation result = algorithm.getResult();   
            
            for (Solution result1 : result) {
                double obj = result1.getObjective(0);
                String variables = "";
                for (int i = 0; i < result1.getNumberOfVariables(); i++) {
                    variables = variables + (i + "(" + (int) EncodingUtils.getInt(result1.getVariable(i)) + "), ");
                }
                System.out.println(tabs + variables);
                System.out.print("Mixed Cost function value is "+ obj + tabs + "\n");
                System.out.print("Response Time and energy pair are "+ tsmp.mymap.get(obj)+ "\n");
                System.out.print("Total response time is "+ tsmp.mymap.get(obj).getKey()+ "\n");
                System.out.print("Total eneregy is "+ tsmp.mymap.get(obj).getValue()+ "\n");
                System.out.println("\n\n\n\n");
                if(bestAllocation == null)
                    bestAllocation = result1; //assuming the result set has only one allocation
                else {
                    if(bestAllocation.getObjective(0) > result1.getObjective(0))
                        bestAllocation = result1;
                }
            }
        }
          
        
         Print Information about the Execution Configuration on the Console
         
        
        System.out.println("Tasks:");
        for (int i = 0; i < Problem_Model.getTaskId(); i++) {
            System.out.println(tabs + solutionModel_List.get(i).toString());
        }
        System.out.println("VMs:");
        for (VM_Data vm : VM) {
            System.out.println(tabs + vm.toString());
        }
        System.out.println("Best Allocation from " + numOfRuns + " runs:");
        // Get Execution Results
         
         
        /*result = algorithm.getResult();
        for (Solution result1 : result) {
            double[] obj = result1.getObjective(0);
            String variables = "";
            for (int i = 0; i < result1.getNumberOfVariables(); i++) {
                variables = variables + (i + "(" + (int) EncodingUtils.getInt(result1.getVariable(i)) + "), ");
            }
            System.out.println(tabs + variables);
            if (obj.length == 3) {
                System.out.println(tabs + "RT=" + obj[0] + " Seconds" + tabs + tabs + "Energy=" + obj[1] + " Joules" + tabs + tabs + "Cost=$" + obj[2] + "\n");
            } else {
                for (int i = 0; i < obj.length; i++) {
                    System.out.print("Mixed Cost function value is "+ obj[i] + tabs + "\n");
                    System.out.print("Response Time and energy pair are "+ tsmp.mymap.get(obj[i])+ "\n");
                    System.out.print("Total response time is "+ tsmp.mymap.get(obj[i]).getKey()+ "\n");
                    System.out.print("Total eneregy is "+ tsmp.mymap.get(obj[i]).getValue()+ "\n");
                }
                System.out.println("\n\n\n\n");
            }
        }
         
        
        String variables = "";
        for (int i = 0; i < bestAllocation.getNumberOfVariables(); i++) {
            variables = variables + (i + "(" + (int) EncodingUtils.getInt(bestAllocation.getVariable(i)) + "), ");
        }
       */
        
    } 

    public static double[] list2Array(ArrayList<Double> temp) {
        double[] tempArray = new double[temp.size()];
        for (int i = 0; i < tempArray.length; i++) {
            tempArray[i] = temp.get(i);
        }
        return tempArray;
    }

    public static double[] xaxis(int size) {
        double[] tempaxis = new double[size];
        for (int i = 0; i < size; i++) {
            tempaxis[i] = i;
        }
        return tempaxis;
    }

    public static String force_together() {
        String force_together = "";
        for (int[] array_values : execute_Nodes_together) {
            force_together += "{Forced_Nodes_in_VM_" + array_values[0] + ": ";
            boolean found_entry = false;
            for (int i = 1; i < array_values.length; i++) {
                found_entry = true;
                force_together += array_values[i] + ", ";
            }
            if (found_entry) {
                force_together = force_together.substring(0, force_together.length() - 2) + "}, ";
            } else {
                force_together += "No_Node Found}, ";
            }
        }
        return force_together.substring(0, force_together.length() - 2);
    }
}
