package tb_model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.Schedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridDimensions;
import repast.simphony.util.ContextUtils;

public class Output {
	
	public static BufferedWriter outputFile;
	private Schedule mySchedule;
	double [] count;
	double endSim ;
	int timeSlice;
	double timeMultiplier;
	
	public Output(){
		
		Parameters p = RunEnvironment.getInstance().getParameters();
		Schedule schedule= (Schedule) RunEnvironment.getInstance().getCurrentSchedule();
		this.mySchedule = schedule;
		// Seven here is the number of columns in data output
		this.count = new double [6];
		this.endSim = (Double) p.getValue("endSim");
		this.timeSlice = 0;
		this.timeMultiplier = (Double)p.getValue("timeMultiplier");	
	}
	@ScheduledMethod(start = 1, interval = 1, shuffle = false, priority = ScheduleParameters.LAST_PRIORITY)
	public void step(){
		
		this.timeSlice++;
		// Synch update takes place here...
		Parameters p = RunEnvironment.getInstance().getParameters();
		
		int Asynch = (Integer)p.getValue("Asynch");
		Context context = ContextUtils.getContext(this);
		Grid grid = (Grid) context.getProjection("grid");
		if(Asynch == 0){
			
			Human h = null;
			for (Object o: grid.getObjects()){
	    	
				if (o instanceof Human){
	    		
					h = (Human)o;
					// Synchronous state change
					if (h.state != h.toBeChanged){
						
						h.state = h.toBeChanged;
					}
					
					// Kill or Recover
					if (h.state == 2 && h.day < 0.01){
	    						
						h.toBeChanged = 3;
						h.state = 3;
							h.day = 0;
		    			if (h.toBeKilled == true){
	    							
		    				h.toBeChanged = 4;
		    				h.state = 4;
		    				h.day = 0;
		    				context.remove(h);
		    			}				
					}
				}
			}
		}
		// Determines the increments between the outputting process
		if (this.timeSlice == this.timeMultiplier){
			
			Record();
			try {
				printFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.timeSlice = 0;
		}
		
		// End Simulation
		if (count[2] == 0 && this.mySchedule.getTickCount() > 10*this.timeMultiplier){
			
			try {
				outputFile.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//System.out.println(System.currentTimeMillis()-Singleton.startTime+"	"+p.getValueAsString("timeMultiplier")+"	"+p.getValueAsString("population")+"	"+p.getValueAsString("pMortality")
				//	+"	"+p.getValueAsString("tRecover")+"	"+p.getValueAsString("encounterRate")+"	"+p.getValueAsString("initialInfectious"));
			RunEnvironment.getInstance().endRun();
		}
		RunEnvironment.getInstance().endAt(this.endSim*this.timeMultiplier);		
	}
	public void Record(){
		
		Context context = ContextUtils.getContext(this);
	    Grid grid = (Grid) context.getProjection("grid");
	    Parameters p = RunEnvironment.getInstance().getParameters();
	    int population = (Integer)p.getValue("population");
	    double time = this.mySchedule.getTickCount();
	    int susceptible = 0;
	    int recovered = 0;
	    int infected = 0;
	    int dead = 0;
	    Human h = null;
	    for (Object o: grid.getObjects()){
	    	
	    	if (o instanceof Human){
	    		
	    		h = (Human)o;
	    		if (h.state == 0){
	    		
	    			susceptible = susceptible+1;
	    		}
	    		else if (h.state == 1){
	    		
	    		}
	    	
	    		else if (h.state == 2){
	    			
	    			infected = infected+1;
	    		}
	    		else if (h.state == 3){
	    			
	    			recovered = recovered+1;
	    		}
	    		else if (h.state == 4){
				
	    			dead = dead+1;
	    		}
	    	}
	    }
	    count[0] = time/this.timeMultiplier;
	    count[1] = susceptible;
	    count[2] = infected;
	    count[3] = recovered;
	    count[4] = population - infected - recovered - susceptible;
	    count[5] = population;
	}
	
	// Outputting in a file
	public void printFile() throws IOException{
		
		Parameters p = RunEnvironment.getInstance().getParameters();
		//Create the file
		if (this.mySchedule.getTickCount() <= this.timeMultiplier) {
			try 
				{	
					String Model = "";
					double pm = (Double)p.getValue("pMortality");
					double tr = (Double)p.getValue("tRecover");
					double tm = (Double)p.getValue("tDie");
					double tp = (Double)p.getValue("pTransmission");
					double er = (Double)p.getValue("encounterRate");
					int iniD = 0;
					int iniR = 0;
					int iniRe = 0;
					int iniI = (Integer)p.getValue("initialInfectious");
					int iniS = (Integer)p.getValue("population")-iniI;
					int seed = (Integer)p.getValue("randomSeed");
					
					
					if ((Integer)p.getValue("Asynch") == 1){
						
						Model = "TimeA";
					}
					else {
						Model = "TimeS";
					}
					String fileName = Model+"_res_"+pm+"_"+tr+"_"+tm+"_"+tp+"_"+er+"_"+iniD+"_"+iniR+"_"+iniRe+"_"+iniI+"_"+iniS+"_"+seed;
					String path = "/Users/ozi/Desktop/Data/";	
					fileName=path.concat(fileName); 
					String ext=".txt";
					fileName=fileName.concat(ext);
					File f = new File(fileName); 
					outputFile = new BufferedWriter(new FileWriter(f));
					} catch (IOException e) {System.out.println("Error during reading/writing");} 
		}
		// Do not print the total number
		for (int i = 0; i < this.count.length-1; i++){
				
			try {
				outputFile.write(this.count[i]+" ");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		outputFile.newLine();
		if (mySchedule.getTickCount() == this.endSim*this.timeMultiplier){
			
			outputFile.close();
		}		
	}
}
