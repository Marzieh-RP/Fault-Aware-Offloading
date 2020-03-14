//package TheRandomWorkflow;

/**
 *
 * @author Marzieh Ranjbar Pirbasti
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.text.NumberFormat;
import java.io.*;

public class RandomWorkflow {
	
	private static boolean isreachablefrom(int source, int destination, boolean[][] array, int size)
	{
		if (array[destination][source] == true)
			return true;
		else {
			for (int i=0;i<size;i++)
			{
				if (source ==i)
					continue;
				if (array[i][source] == true)
					if (isreachablefrom(i,destination,array,size) == true)
						return true;
			}
		}
		return false;
	}
	//Create Random workflow graph for given number of nodes with computation times and communication times.
	public static void main(String args[]) throws IOException {
		//number of nodes
	int numOfNodes = 10;

		// range for computation times
		double rangeMinComp = 200.0;
		double rangeMaxComp = 200.0;

		// range for communication times
		double rangeMinComm = 10;
		double rangeMaxComm = 50;


		double probabiltiyofdependence = 4.0;

		Random r1 = new Random(78787878);


		// generate random computation time for the nodes
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(3);

		File file = new File("workflow.txt");
		FileWriter fr = new FileWriter(file);

		fr.write("strcallGraph_executionGraph = \"ExecutionGraph\";\n\n");
		fr.write("execute_Nodes_together = new ArrayList(Arrays.asList(new int[]{0, 0}, new int[]{1}, new int[]{2}));\n\n");

		fr.write("double[] response_Time = {");   

		for(int i = 0; i < numOfNodes; i++) {			
			double randomValue = rangeMinComp + (rangeMaxComp - rangeMinComp) * r1.nextDouble();
			System.out.println("Computation time for node " + i + ": " + nf.format(randomValue));
			fr.write(nf.format(randomValue));
			if(i != (numOfNodes-1))
				fr.write(", ");
		}
		fr.write("};\n\n");

		boolean[] is_Node_dependent = new boolean[numOfNodes];
		boolean[][] next_Node_id = new boolean[numOfNodes][numOfNodes];
		double[][] communication_data = new double[numOfNodes][numOfNodes];
		boolean[][] dependent_Node_id = new boolean[numOfNodes][numOfNodes];

		boolean[] connected_to_any = new boolean[numOfNodes];
		for (int i=0;i<numOfNodes;i++)
			connected_to_any[i] = false;

		//generate edges and the communication times
		for(int i = 0; i < numOfNodes - 1; i++) {
			for(int j = i+1; j < numOfNodes  - 1; j++) {
				if(r1.nextDouble() < probabiltiyofdependence) {// there is an edge between node i and j
					//System.out.println("edge: from node " + i + " to node " + j);				
					double randomValue = rangeMinComm + (rangeMaxComm - rangeMinComm) * r1.nextDouble();
					//System.out.println("Communication time from node " + j + "to node " + j + ": " + nf.format(randomValue));
					is_Node_dependent[j] = true;
					connected_to_any[i] = true;
					next_Node_id[i][j] = true;
					communication_data[i][j] = Double.parseDouble(nf.format(randomValue));
					dependent_Node_id[j][i] = true;

				}
			}
		}

		//remove redundant edges:

		for(int i = 0; i < numOfNodes - 1; i++) {
			for(int j = i+1; j < numOfNodes  - 1; j++) {		
				for(int k = j+1; k < numOfNodes  - 1; k++) {

					if (next_Node_id[j][k] == true && next_Node_id[i][k] == true && isreachablefrom(i,j,dependent_Node_id,numOfNodes))
					{
						next_Node_id[i][k] = false;
						communication_data[i][k]=0;
						dependent_Node_id[k][i] = false;
					}

				}
			}
		}		



		
		//last node:
		//anything that's not connected to something else must be connected to i
		for (int i=0; i < numOfNodes-1; i++)
			if (connected_to_any[i] == false)
			{
				is_Node_dependent[numOfNodes-1] = true;
				//System.out.println("edge: from node " + i + " to node " + j);				
				double randomValue = rangeMinComm + (rangeMaxComm - rangeMinComm) * r1.nextDouble();
				//System.out.println("Communication time from node " + j + "to node " + j + ": " + nf.format(randomValue));
				connected_to_any[i] = true;
				next_Node_id[i][numOfNodes-1] = true;
				communication_data[i][numOfNodes-1] = Double.parseDouble(nf.format(randomValue));
				dependent_Node_id[numOfNodes-1][i] = true;

			}


		fr.write("boolean[] is_Node_dependent = {");
		for(int i = 0; i < numOfNodes; i++) {
			fr.write(""+ is_Node_dependent[i]);
			if(i != (numOfNodes-1)) fr.write(", ");
		}
		fr.write("};\n\n");

		fr.write("int[][] next_Node_id = {");
		for(int i = 0; i < numOfNodes; i++) {
			fr.write("new int[]{");
			int count = 0;
			for(int j = 0; j < numOfNodes; j++) {
				if(next_Node_id[i][j] == true) {
					if(count > 0) fr.write(", ");
					fr.write("" + j);
					count++;
				}
			}
			fr.write("}");
			if(i != (numOfNodes-1)) fr.write(", ");
		}
		fr.write("};\n\n");

		fr.write("double[][] communication_data ={");
		for(int i = 0; i < numOfNodes; i++) {
			fr.write("new double[]{");
			int count = 0;
			for(int j = 0; j < numOfNodes; j++) {
				if(communication_data[i][j] > 0.0) {
					if(count > 0) fr.write(", ");
					fr.write("" + communication_data[i][j]);
					count++;
				}
			}
			if(count == 0) fr.write("0.0");
			fr.write("}");
			if(i != (numOfNodes-1)) fr.write(", ");
		}
		fr.write("};\n\n");

		int[] level = new int[numOfNodes];
		for(int i = 0; i < numOfNodes; i++)
			level[i] = 1;

		fr.write("int[][] dependent_Node_id = {");
		for(int i = 0; i < numOfNodes; i++) {
			fr.write("new int[]{");
			int count = 0;
			for(int j = 0; j < numOfNodes; j++) {
				if(dependent_Node_id[i][j] == true) {
					if(count > 0) fr.write(", ");
					fr.write("" + j);
					count++;
					if(level[i] < (level[j] + 1))
						level[i] = level[j] + 1;
				}
			}
			fr.write("}");
			if(i != (numOfNodes-1)) fr.write(", ");
		}
		fr.write("};\n\n");

		fr.write("if ((Node_id.length == response_Time.length) && (Node_id.length == is_Node_dependent.length) && (Node_id.length == next_Node_id.length) && (Node_id.length == dependent_Node_id.length) && (Node_id.length == communication_data.length)) {\n");
		fr.write("    for (int i = 0; i < Node_id.length; i++) {\n");
		fr.write("        solutionModel_List.add(new Problem_Model(Node_id[i], response_Time[i], is_Node_dependent[i], dependent_Node_id[i], next_Node_id[i], communication_data[i]));\n");
		fr.write("    }\n");
		fr.write("} else {\n");       
		fr.write("    throw new IllegalArgumentException(\"Input Sizes are not equal\");\n");
		fr.write("}\n\n");

		for(int i = 0; i < numOfNodes; i++) {
			fr.write("solutionModel_List.get(" + i + ").setLevel(" + level[i] + ");\n");
		}

		fr.close();
	}

}
