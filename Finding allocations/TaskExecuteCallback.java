/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package PerformanceMeasure;

import java.util.ArrayList;
import java.util.function.Function;

public class TaskExecuteCallback implements ICallback{
    Function<ArrayList<Job>, Double> function;
    ArrayList<Job> jobs;
    
    public TaskExecuteCallback(Function<ArrayList<Job>, Double> func, ArrayList<Job> jobs) {
        function = func;
        this.jobs = jobs;
    }
    public Double execute(){
        return function.apply(jobs);
    }

}
