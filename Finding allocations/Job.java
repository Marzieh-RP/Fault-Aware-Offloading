/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PerformanceMeasure;

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
    private double lambdaF;
    private double lambdamVm1;
    private double lambdamVm2;
    private double lambdaVm1Vm2;
    private double linkRepair;
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
    public Job(int dependencyLevel, String n, boolean t, double p, ICallback c, VM_Data r, String linkname,
    		double DC, double OV, double Lambdaf, double LambdamVM1, double LambdamVM2, double LambdaVM1VM2, double LinkeRepair ) {
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
        
        int maxfaults = 5;
        //the type of fault tolerance mechanism is defined in vm so we look at vm mechanism type to calculate response time
        if (isComputation) {//exe
        if (r.mechanismFaultandRecovery == 0)
        	processingTime = p;
        else if (r.mechanismFaultandRecovery == 1)
		{

			// ASSUMING ONLY ONE:
			//processingTime = p + (1-Math.exp(-1*r.lambda*p)) * (0.5*p +r.mttr);
			// general equation:
			processingTime = p;
			for (int i=1;i<=maxfaults;i++)
				processingTime += Math.pow(1-Math.exp(-1*r.lambda*p),i) * (0.5*p +r.mttr);
		}
		
		
        
        //2 for checkpointing with 2 checkpoints
        else if (r.mechanismFaultandRecovery == 2)
        {
			// ASSUMING ONLY ONE:
			//processingTime = p + (1-Math.exp(-1*r.lambda*p)) * (0.5*p +r.mttr);
        	
			//processingTime = p + ov + (1-Math.exp(-1*r.lambda* (p/2 +ov))) * (p/4 +  ov/2 + r.mttr) + (1-Math.exp(-1*r.lambda* (p/2))) * (p/4 + r.mttr);
			
        	processingTime = p+ov;
        	for (int i=1;i<=maxfaults;i++)
			{// failure in first half
			processingTime+= Math.pow(1-Math.exp(-1*r.lambda* (p/2 +ov)),i) * (0.25*p + 0.5 * ov + r.mttr);
			//failure in 2nd 
			processingTime+= Math.pow(1-Math.exp(-1*r.lambda* (p/2)),i) * (0.25*p + r.mttr);
			}
        }
        	/*
        	 processingTime=
        	( Math.exp((-1*r.lambda)*(p+2*ov)))*(p+2*ov)+
        	(1-Math.exp(-1*r.lambda*(p/2+2*ov)))*( r.mttr+3*p/2+2*ov)+
        	(Math.exp((-1*r.lambda)*(p/2+ov)))*(1-Math.exp((-1*r.lambda)*p/2))*( 3*p/2+r.mttr+2*ov);*/




        	
        //3 for checkpointing with 4 checkpoints
        
        else if (r.mechanismFaultandRecovery == 3)
        {

        	processingTime = p + 3*ov;
			//one failure at most: 
			//processingTime = p + 3 * ov + 3* (Math.exp(-1*r.lambda* (p/4 +ov))) * (p/8 + 0.5 * ov + r.mttr) + (1-Math.exp(-1*r.lambda* (p/4))) * (p/8 + r.mmtr);
			
			//general case:
			for (int i=1;i<=maxfaults;i++)
			{
			processingTime+= 3 * (Math.pow(1-Math.exp(-1*r.lambda* (p/4 +ov)),i) * (0.5*p/4 + 0.5 * ov + r.mttr));
			processingTime+= Math.pow(1-Math.exp(-1*r.lambda* (p/4)),i) * (p/8 + r.mttr);
			}
			
        }
        }
        else {//communication (Link)
        	processingTime = p;
        	if (linkname.equals("LocalVM1"))
        	{
        		processingTime = p;
    			for (int i=1;i<=maxfaults;i++)
    				processingTime += Math.pow(1-Math.exp(-1*lambdaF*p),i)* (0.5*p+dc) + Math.pow(1-Math.exp(-1*lambdamVm1*p),i) * (0.5*p+linkRepair);
    				
        	//(Math.exp(-1*lambdaF*p))*(Math.exp(-1*lambdamVm1*p))*p+
        	//(1-Math.exp(-1*lambdaF*p))*(Math.exp(-1*lambdamVm1*p))*(2*p+dc) + 
        	//(Math.exp(-1*lambdaF*p))*(1-Math.exp(-1*lambdamVm1*p))*(2*p+linkRepair) + 
        	//(1-Math.exp(-1*lambdaF*p))*(1-Math.exp(-1*lambdamVm1*p))*(2*p+dc+linkRepair);
        	}
        	else if (linkname.equals("LocalVM2"))
        	{
        		
        		processingTime = p;
    			for (int i=1;i<=maxfaults;i++)
    				processingTime += Math.pow(1-Math.exp(-1*lambdaF*p),i)* (0.5*p+dc) + Math.pow(1-Math.exp(-1*lambdamVm2*p),i) * (0.5*p+linkRepair);
        	}
        	/*
        		processingTime = 
        	(Math.exp(-1*lambdaF*p))*(Math.exp(-1*lambdamVm2*p))*p+
        	(1-Math.exp(-1*lambdaF*p))*(Math.exp(-1*lambdamVm2*p))*(2*p+dc) + 
        	(Math.exp(-1*lambdaF*p))*(1-Math.exp(-1*lambdamVm2*p))*(2*p+linkRepair) + 
        	(1-Math.exp(-1*lambdaF*p))*(1-Math.exp(-1*lambdamVm2*p))*(2*p+Math.max(dc, linkRepair));
        	*/
        	// only has link failure
        	else if (linkname.equals("VM1VM2"))
        	{
        		processingTime = p;
    			for (int i=1;i<=maxfaults;i++)
    				processingTime += Math.pow(1-Math.exp(-1*lambdaVm1Vm2*p),i) * (0.5*p+linkRepair);
        	}     		
        		// we won't have lambdaf in this case since no mobility
        		/*
        		processingTime =
            (Math.exp(-1*lambdaVm1Vm2*p))*p+
            (Math.exp(-1*lambdaVm1Vm2*p))*(2*p+dc) + 
            (1-Math.exp(-1*lambdaVm1Vm2*p))*(2*p+linkRepair) + 
            (1-Math.exp(-1*lambdaVm1Vm2*p))*(2*p+dc+linkRepair);
            */
        	else
        		
        	{
        		//this should never happen
        		processingTime = 10000000;        		
        	}
        }
        
        ArrayList<Job> list = jobs.get(dependencyLevel);
        if (list == null) {
            list = new ArrayList<Job>();
            list.add(this);
            jobs.put(dependencyLevel, list);
        }
        else {
            list.add(this);
        }
    }
    
    private static ArrayList<Job> getJobs(int dependencyLevel) {
        return jobs.get(dependencyLevel);
    }
    
    public static void printJobs(int maxLevel) {
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
                        Processor p = r.getFreeProcessor(toa);
                        j.setProcessor(p); j.setStartTime(toa);
                        p.setBusy(j.getType(), j.getStartTime(null), j.getEndTime(null));         
                    }
                    else {
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
