package PerformanceMeasure;

import java.util.function.Function;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

public class Callback implements ICallback {
    Function<String, Double> function;
    
    public Callback(Function<String, Double> func) {
        function = func;
    }
    public Double execute(){
        return function.apply(null);
    }
}
