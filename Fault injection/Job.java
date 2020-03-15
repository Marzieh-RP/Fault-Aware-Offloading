/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PerformanceMeasure;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;

import org.moeaframework.util.tree.Lambda;

/**
 *
 * This class represents job that needs to be processed by a processor.
 */
public class Job {
    //olivia
    private static Hashtable<Integer, ArrayList<Job>> jobs = new Hashtable<Integer, ArrayList<Job>>();
    private int dependencyLevel;
    private VM_Data allocatedResource = null;
    private static boolean processed = false;
    private double dc;
    private double ov;
    //marzieh added
    private double lambdaF;
    private double lambdamVm1;
    private double lambdamVm2;
    private double lambdaVm1Vm2;
    private double linkRepair;
    private int VmID;
    //olivia
    
    private String name;
    
    // type of job (computation or transmission)
    private boolean isComputation;
    
    //when the job can start execution
    //olivia
    //private double timeOfArrival;
    private ICallback timeOfArrival;
    //olivia
    
    //the time when the job actually starts execution on a processor
    private double startTime = 0.0;
    
    //when the job's processing is complete
    private double endTime = 0.0;
    
    //time needed to process this job
    private double processingTime;
    
    //the processor allocated to run this job
    private Processor runsOn = null;
    //marzieh added
	private static ArrayList<Double> VmOneDownTimes;
	private static ArrayList<Double> VmTwoDownTimes;
	private static ArrayList<Double> LinkLocalVM1DownTimes;
	private static ArrayList<Double> LinkLocalVM2DownTimes;
	private static ArrayList<Double> LinkVM1VM2;
	private static ArrayList<Double> mobilityfails;
	


	private static ArrayList<Double> VmOneDownTimes_duration;
	private static ArrayList<Double> VmTwoDownTimes_duration;
	private static ArrayList<Double> LinkLocalVM1DownTimes_duration;
	private static ArrayList<Double> LinkLocalVM2DownTimes_duration;
	private static ArrayList<Double> LinkVM1VM2_duration;
	private static ArrayList<Double> mobilityfails_duration;
	

	private String linkname;
    /* olivia
    public Job(String n, boolean t, double p, double toa) {
        name = n;
        isComputation = t;
        processingTime = p;
        timeOfArrival = toa;
    }
    public Job(){
        
    }
    */
    public Job (){
        
    }
    
    //returns indexes that overlap
    //array--> this arraylist has start time of failures 
    //starttime is  the start time of the job without failure
    //Processing time is the processing time of the job 
    // overlapindexes gets the array which contains list of failure and an array which has duration of failue
    // and start time and processing time of the job since
    // at every time stamp only one job is on the vm
    public static ArrayList<Integer> overlapindexes (ArrayList<Double> array, ArrayList<Double> duration, double starttime, double processingtime)
    {
    	// results save the index of starttimes that overlap  i.e those failure that affect the job
    	ArrayList<Integer> result = new ArrayList<Integer>();

    	
    	for (int i=0;i<array.size();i++)
    	{
             // if vm failure occurs before the time that the job starts but last until the time 
    		 //that job starts
    		
    		if (array.get(i) == starttime)
    			continue; // this is a corner case which happens as we move the start time forward, so being exactly on time of failure is an artifact of that.
    						
    		if (array.get(i)<starttime)
    		{     //here we check that whether the failure lasts until the start time of the job or not
    		// in other word does it affect the job or not?
    			if (array.get(i)+duration.get(i) >starttime)
    				result.add(i);

    		}
    		else
    		{
    			//if fault happens somewhere during the time of processing of the job
    			if (starttime+processingtime>array.get(i))
    				result.add(i);

    		}
    	}

    	return result;
    	 	
    }
    
    //used for checkpointing.
    //0 for the first quarter,
    //1 for the 2nd quarter, 
    //2 for the 3rd quarter,
    //3 for the last quarter.
    public static int whereisoverlap(double startfailure, double repairtime, double starttime, double processingtime)
    {
    	if (startfailure < (startfailure+ processingtime)/4)
    		return 0;
    	if (startfailure < (startfailure+ processingtime)/2)
    		return 0;
    	if (startfailure < 3*(startfailure+ processingtime)/4)
    		return 0;
    	if (startfailure < (startfailure+ processingtime))
    		return 0;
    	
    	return -1;//this should never happen
    }    
    
    //for link
    //can also be used for restart, but we use a more complex code below to work for both restart and checkpointing
    public static double overheadfail(ArrayList<Double> downtimetouse,ArrayList<Double> durationtouse,double starttime,double processingtime)
    {
    	
    	double time = starttime;
    	ArrayList<Integer> overlaps = new ArrayList<Integer>();
    	//get the indexes of failures that overlap with this task.
	overlaps = overlapindexes(downtimetouse, durationtouse, time, processingtime);

	//first we shift the start time then check whether next fault will overlap with this new start time
	// continue until all faults are processed
	while (overlaps.isEmpty() == false)
	{   
	

		//obtain the corresponding duration of failure for the first index and then see when does that failure end.
		//this is the "new" start time assuming the first failure finished.
		time = downtimetouse.get(overlaps.get(0))+durationtouse.get(overlaps.get(0));

		//with the new start time(moved forward because of failure) obtain a new set of overlapping indexes.
		overlaps = overlapindexes(downtimetouse, durationtouse, time, processingtime);
		//System.out.println(t);
	}
	//the amount of time start time had to be pushed forward due to failures is the overhead.
	return time-starttime;
    }
    
    
    public Job(int dependencyLevel, String n, boolean t, double p, ICallback c, VM_Data r, String linkname,
    		double DC, double OV, double Lambdaf, double  LambdamVM1, double LambdamVM2, double LambdaVM1VM2, double LinkeRepair,int VMID,
    		ArrayList<Double> VmOneDownTime, ArrayList<Double> VmTwoDownTime,
   		 ArrayList<Double>  LinkLocalVM1DownTime, ArrayList<Double> LinkLocalVM2DownTime,  ArrayList<Double> LinkVM1VM2s
   		 ,    		ArrayList<Double> VmOneDownTime_duration, ArrayList<Double> VmTwoDownTime_duration,
   		 ArrayList<Double>  LinkLocalVM1DownTime_duration, ArrayList<Double> LinkLocalVM2DownTime_duration,  ArrayList<Double> LinkVM1VM2s_duration,
   		ArrayList<Double> mobilityfails1, ArrayList<Double> mobilityfails_duration1) {
       
    	this.dependencyLevel = dependencyLevel;
        this.name = n;
        this.isComputation = t;
        this.timeOfArrival = c;
        this.allocatedResource = r;
        this.dc=DC;
        this.ov=OV;
        this.lambdaF=Lambdaf;
        this.lambdamVm1=LambdamVM1;
        this.lambdamVm2=LambdamVM2;
        this.lambdaVm1Vm2=LambdaVM1VM2;
        this.linkRepair=LinkeRepair;
        this.VmID=VMID;
        
        
        
        this.linkname= linkname;
        
        
        VmOneDownTimes = VmOneDownTime;
        VmTwoDownTimes = VmTwoDownTime;
        LinkLocalVM1DownTimes = LinkLocalVM1DownTime;
        LinkLocalVM2DownTimes = LinkLocalVM2DownTime;
        LinkVM1VM2 = LinkVM1VM2s;
        mobilityfails = mobilityfails1;
        mobilityfails_duration = mobilityfails_duration1;
     
        VmOneDownTimes_duration = VmOneDownTime_duration;
        VmTwoDownTimes_duration = VmTwoDownTime_duration;
        LinkLocalVM1DownTimes_duration = LinkLocalVM1DownTime_duration;
        LinkLocalVM2DownTimes_duration = LinkLocalVM2DownTime_duration;
        LinkVM1VM2_duration = LinkVM1VM2s_duration;
        processingTime = p;
        
        if (isComputation) {
        if (r.mechanismFaultandRecovery == 2)
        	processingTime += ov;

        if (r.mechanismFaultandRecovery == 3)
        	processingTime += 3*ov;}
        
        

        
        
        
        
        
        
        
        
        
        
        
        //the type of fault tolerance mechanism is defined in vm so we look at vm mechanism type to calculate response time

        
        ArrayList<Job> list = jobs.get(dependencyLevel);
        if (list == null) {
            list = new ArrayList<Job>();
            list.add(this);
            jobs.put(dependencyLevel, list);
        }
        else {
            list.add(this);
        }
    } // end of job constructor
    
    private static ArrayList<Job> getJobs(int dependencyLevel) {
        return jobs.get(dependencyLevel);
    }
    
    public void printJobs(int maxLevel) {
        //Process 
        if(processed == false)
            processJobs(maxLevel);
        int i = 1;
        while (i <= maxLevel) {
            ArrayList<Job> list = getJobs(i);
            if(list != null) {
                System.out.println("level-" + i + " jobs:");
                for(Job j1 : list) {
                    System.out.println(j1);
                }
                System.out.println();
            }
            i++;
        }
    }
    public static void removeAllJobs(){
        jobs.clear();
        processed = false;
        
    }
    public static void processJobs(int maxLevel) {
        //Process
        if(processed == true)
            return;
        
        int i = 1;     
        while (i <= maxLevel) {
            ArrayList<Job> list = getJobs(i);
            if(list != null) {              
                Collections.sort(list, new Comparator<Job>() {
                    @Override
                    public int compare(Job j1, Job j2) {
                        return (int) (j1.getTimeOfArrival() - j2.getTimeOfArrival());
                    }
                });

                for (Job j : list) {
                    VM_Data r = j.allocatedResource;
                    double t = r.getTimeForAvailableProcessor();
                    double toa = j.getTimeOfArrival();
                    
                    
                    if(toa >= t) {
                    	// we store the original time of arrival and move the toa to a time where there is no failure overlapping with this task
                    	// after that, we see how much the toa was changed, and we use the original toa but add the difference to the processing time.
                    	// this emulates the behaviour in case of a failure.
                    	double origtoa = toa;
                    	if (j.isComputation == false)
                    	{
                    	//link code here
                    	// using the function defined above to find new time of arrival
                    		if (j.linkname.contentEquals("LocalVM1"))
                    		toa+= overheadfail(LinkLocalVM1DownTimes, LinkLocalVM1DownTimes_duration ,toa  , j.processingTime );
                    		
                    		else if (j.linkname.contentEquals("LocalVM2"))
                    		toa+= overheadfail(LinkLocalVM2DownTimes, LinkLocalVM2DownTimes_duration ,toa  , j.processingTime );
                    	                    
                    		else if (j.linkname.contentEquals("VM1VM2"))
                    		toa+= overheadfail(LinkVM1VM2, LinkVM1VM2_duration ,toa  , j.processingTime );
                    	
                    		//mobility	
                    		toa+= overheadfail(mobilityfails, mobilityfails_duration,toa  , j.processingTime );
                    		
                    		}
                    		
                    	else
                    	{
                    	//if (j.isComputation == false)
                    	//System.out.println(r.getVm_id());
                    	///starttime = toa
                    	//processing time to change j.processingtime
                    	//to check if receive or not use j.iscomputation
                    	
                    	//if fault type == restart{
                    	
                    	ArrayList<Integer> overlaps = new ArrayList<Integer>();
                    	ArrayList<Double> downtimetouse = new ArrayList<Double>();
                    	ArrayList<Double> durationtouse = new ArrayList<Double>();
                    	
                    	overlaps.clear();
                    	downtimetouse.clear();
                    	durationtouse.clear();

                    	
                    	if (r.getVm_id() == 1)
                    		{
                    		downtimetouse = VmOneDownTimes;
                    		durationtouse =VmOneDownTimes_duration;
                    		}
                    	else if (r.getVm_id()==2)
                    	{		
                    		downtimetouse = VmTwoDownTimes;
                    		durationtouse = VmTwoDownTimes_duration;                  		
                    	}
                    	// which indexes of downtimes overlaps with this job
                    	overlaps = overlapindexes(downtimetouse, durationtouse, toa, j.processingTime);

                    	int newquarters = 0;
                    	while (overlaps.isEmpty() == false)
                    	{   
                    	
                    		//if checkpointing:
                    		if (r.mechanismFaultandRecovery == 3 || r.mechanismFaultandRecovery == 2)	
                    		newquarters += whereisoverlap(downtimetouse.get(overlaps.get(0)), durationtouse.get(overlaps.get(0)), toa, j.processingTime * (1.0-newquarters/4.0));
                    		if (r.mechanismFaultandRecovery == 2)
                    		{
                    			if(newquarters == 1)
                    				newquarters = 0;
                    			if (newquarters == 3)
                    				newquarters = 2;
                    		}
                    		if (newquarters >= 4)
                    			break;
                    		//update new quarters and times with conditions
                    		toa = downtimetouse.get(overlaps.get(0))+durationtouse.get(overlaps.get(0));

                    		
                    		overlaps = overlapindexes(downtimetouse, durationtouse, toa, j.processingTime * (1.0-newquarters/4.0));
                    		//System.out.println(t);
                    	}
                    	}
                    	//this is the over head due to failure
                    	double addedoverheadbcsoffailure = toa-origtoa;
                    	//add the overhead to the processing time
                    	j.processingTime +=addedoverheadbcsoffailure;
                    	toa= origtoa;
              		
                    	//}
                    	//start processing job 
                        Processor p = r.getFreeProcessor(toa);
                        j.setProcessor(p); j.setStartTime(toa);
                        p.setBusy(j.getType(), j.getStartTime(null), j.getEndTime(null));         
                    }
                    else {
                    	double origt = t;
                    	//send/receive
                    	if (j.isComputation == false)
                    	{
                        	//link code here
                    		//we can extend this to give different fail times to different links if we want
                    		if (j.linkname.contentEquals("LocalVM1"))
                    		t+= overheadfail(LinkLocalVM1DownTimes, LinkLocalVM1DownTimes_duration ,t  , j.processingTime );
                    		
                    		else if (j.linkname.contentEquals("LocalVM2"))
                    		t+= overheadfail(LinkLocalVM2DownTimes, LinkLocalVM2DownTimes_duration ,t  , j.processingTime );
                    	                    
                    		else if (j.linkname.contentEquals("VM1VM2"))
                    		t+= overheadfail(LinkVM1VM2, LinkVM1VM2_duration ,t  , j.processingTime );
                    	
                    		//mobility	
                    		t+= overheadfail(mobilityfails, mobilityfails_duration,t  , j.processingTime );
                    		


                    	}
                    	else
                    	{
                    	
                    	//starttime t
                    	//processing time to change j.processingtime
                    	//to check if receive or not use j.iscomputation
 

                    	
                    	int quartersfinished=0;
                    	
                    	ArrayList<Integer> overlaps = new ArrayList<Integer>();
                    	ArrayList<Double> downtimetouse = new ArrayList<Double>();
                    	ArrayList<Double> durationtouse = new ArrayList<Double>();

                    	overlaps.clear();
                    	downtimetouse.clear();
                    	durationtouse.clear();
                    	
                    	if (r.getVm_id() == 1)
                    		{
                    		downtimetouse = VmOneDownTimes;
                    		durationtouse = VmOneDownTimes_duration;
                    		}
                    	else if (r.getVm_id()==2)
                    	{		
                    		downtimetouse = VmTwoDownTimes;
                    		durationtouse =VmTwoDownTimes_duration;                  		
                    	}
                    	
                    	overlaps = overlapindexes(downtimetouse, durationtouse, t, j.processingTime);
                    	
                    	int newquarters = 0;
                    	while (overlaps.isEmpty() == false)
                    	{   
                    		//if checkpointing:
                    		if (r.mechanismFaultandRecovery == 3 || r.mechanismFaultandRecovery ==2)	
                    		newquarters += whereisoverlap(downtimetouse.get(overlaps.get(0)), durationtouse.get(overlaps.get(0)), t, j.processingTime * (1.0-newquarters/4.0));
                    		if (r.mechanismFaultandRecovery == 2)
                    		{
                    			if(newquarters == 1)
                    				newquarters = 0;
                    			if (newquarters == 3)
                    				newquarters = 2;
                    		}
                    		if (newquarters >= 4)
                    			break;
                    		
                    		//update new quarters and times with conditions
                    		t = downtimetouse.get(overlaps.get(0))+durationtouse.get(overlaps.get(0));

                    		overlaps = overlapindexes(downtimetouse, durationtouse, t, j.processingTime * (1.0-newquarters/4.0));
                    		//System.out.println(t);
                    	}
                    	}
                    	
                    	double addedoverheadbcsoffailure = t-origt;
                    	
                    	j.processingTime +=addedoverheadbcsoffailure;
                    	t= origt;
                    	
                        Processor p = r.getFreeProcessor(t);
                        j.setProcessor(p); j.setStartTime(t);
                        p.setBusy(j.getType(), j.getStartTime(null), j.getEndTime(null));
                    }
                }
            }
            i++;
        }
        processed = true;
    }
    
    public static double getMaxEndTime(ArrayList<Job> jobs) {
        double maxEndTime = 0.0;
        for( Job j : jobs) {
            double t = j.getEndTime(null);
            if (t > maxEndTime)
                maxEndTime = t;
        }
        return maxEndTime;
    }

    public String getName() {
        return name;
    }
    
    public void setProcessor(Processor p) {
        runsOn = p;
    }

    public boolean isIsComputation() {
        return isComputation;
    }

    public void setIsComputation(boolean isComputation) {
        this.isComputation = isComputation;
    }

    public double getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(double processingTime) {
        this.processingTime = processingTime;
    }

    public Processor getRunsOn() {
        return runsOn;
    }

    public void setRunsOn(Processor runsOn) {
        this.runsOn = runsOn;
    }
    
    public void setStartTime(double s) {
        startTime = s;
        endTime = s + processingTime;
    }
    
    /* olivia
    public double getStartTime() {
        return startTime;
    }
    
    public double getEndTime() {
        return endTime;
    }
    
    public double getTimeOfArrival() {
        return timeOfArrival;
    }
    */
    //olivia
    public int getDependencyLevel() {
        return dependencyLevel;
    }
    
    public double getStartTime(String dummy) {
        return startTime;
    }
    
    public double getEndTime(String dummy) {
        return endTime;
    }
    
    public double getTimeOfArrival() {
        if (timeOfArrival != null)
            return timeOfArrival.execute();
        else return 0.0;
    }
    //olivia
    
    public boolean getType() {
        return isComputation;
    }
   public int getId() {
	   return VmID;
   }
    @Override
    public String toString() {
        return name + "\n\tRuning at = " + runsOn.getName() + "\n\tisComputation = " +isIsComputation()+"\n\tProcessingTime = "+ getProcessingTime() +"\n\tArrivalTime = "+
                getTimeOfArrival() + "\n\tStart_Time =  " + getStartTime(null) + "\n\tEnd Time = " + getEndTime(null) + "\n";

    }
    public String oneLinetoString() {
        //olivia
        //return name + ": Runing at = " + runsOn.getName() + "( isComputation = " +isIsComputation()+", ProcessingTime = "+ getProcessingTime() +", ArrivalTime = "+
        //        getTimeOfArrival() + ", Start_Time =  " + getStartTime() + ", End Time = " + getEndTime() + ")";
        return name + ": RunsOn " + runsOn.getName() + ", ArrivalTime = "+
                getTimeOfArrival() + ", Start_Time =  " + getStartTime(null) + ", Processing Time = " +getProcessingTime()+", End Time = " + getEndTime(null) + ") Level: " + getDependencyLevel();
        //olivia
    }
}
