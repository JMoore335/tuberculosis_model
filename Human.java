package EpiVal;

import java.util.LinkedList;
import java.util.Random;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.Schedule;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;

public class Human {

	public String id;
	public int state;
	public double day;
	public boolean toBeKilled;
	public int toBeChanged;
	
	public Human(String id){
		
		Parameters p = RunEnvironment.getInstance().getParameters();
		this.id = id;
		// 0->Susceptible, 1->Incubating(Not used in SIR), 2->Infected, 3-> Recovered, 4->Dead
		this.state = 0;
		this.day = 0;
		this.toBeKilled = false;
		this.toBeChanged = this.state;
	}
	
	// Step function - at every time tick agents run it - You can add priority to the agents after shuffle if you wish. Since we remove the dead population, 
	// we can not run it without shuffle!!! 
	@ScheduledMethod(start = 1, interval = 1, shuffle = true)
	public void step(){

		// Get parameters
		Parameters p = RunEnvironment.getInstance().getParameters();
		double pMortality = (Double)p.getValue("pMortality");
		double pTransmission = (Double)p.getValue("pTransmission");
		double timeMultiplier = (Double)p.getValue("timeMultiplier");
		double tDie = (Double)p.getValue("tDie")*timeMultiplier;
		double tRecover = (Double)p.getValue("tRecover")*timeMultiplier;
		int width = (Integer)p.getValue("width");
		int height = (Integer)p.getValue("height");
		int whichDistribution = (Integer)p.getValue("Distribution");
		
		// Asynch Update. Before the disease spread starts they get infected.
		int Asynch = (Integer)p.getValue("Asynch");
			
		// Get the Context
		Context context = ContextUtils.getContext(this);
		Grid grid = (Grid) context.getProjection("grid");
		
   		// Disease Spread Algorithm
		if (this.state == 2&&this.day>0){ // If Infected
	
	   		double encounter = (Double)p.getValue("encounterRate");
	   		encounter = encounter/timeMultiplier;
	   		// Encounters are distributed according to Poisson
	   		if (whichDistribution == 1){
	   				
		   		double temp = RandomHelper.createPoisson(encounter).nextInt();
		   		encounter = (int)temp;	
	   		}
	   		// Time to recovery is distributed here 
	   		else if (whichDistribution == 2){
	   			//constant
	   			encounter = encounter/timeMultiplier;
	   		}
	   		else if (whichDistribution == 3){
   				
		   		double temp = RandomHelper.createPoisson(encounter).nextInt();
		   		encounter = (int)temp;	
	   		}
	   		// *********** Here make the encounter rate Poisson or Exponential ************
	   		// Get random encounters from the context
			Human toBeSelected = null;
			LinkedList<Human> encounterList = new LinkedList<Human>();
			int t = 0;
			// Encounter people in the context, which is initially Grid with dimensions of 1.
			while (t != encounter){
				
				int x = RandomHelper.nextIntFromTo(0, width-1);
				int y = RandomHelper.nextIntFromTo(0, height-1);
				Object o = grid.getRandomObjectAt(x,y);
				if ( o != null && o instanceof Human){ 
					
					t++;
   					toBeSelected = (Human)o;
   			
   					// That person is not already infected or flagged
   					if (toBeSelected.state == 0 && toBeSelected.state == toBeSelected.toBeChanged){					
   						
   						encounterList.add(toBeSelected);
   					}				
   				}	
			}		
			
			// Decrement the day - Count-down process to die or recover
			this.day = this.day-1;
			
   			// Infection Process
   			Human toBeInfected = null;
   			for (int i = 0; i < encounterList.size(); i++){
   				
   				toBeInfected = (Human)encounterList.get(i);
   				
   				double r1 = RandomHelper.nextDoubleFromTo(0, 1);
   				// Transmission
   		   		if (r1 < pTransmission){
   		   			// Infect the contacted person
   		   			toBeInfected.toBeChanged = 2;
   		   			
   		   			// Asynch update, immediately change the state of the agent
   		   			if(Asynch == 1){
   					
   		   				toBeInfected.state = toBeInfected.toBeChanged;
   		   			}
   		   				
   		   			// If the selected agent has already been active at the current time tick
   		   			if(whichDistribution == 1){
		   				toBeInfected.day = tRecover;
		   			}
		   			else if(whichDistribution == 2|whichDistribution == 3){
		   				toBeInfected.day = RandomHelper.createPoisson(tRecover).nextInt();
		   			}  				
   		   			double r2 = RandomHelper.nextDoubleFromTo(0, 1);
   			   		if (r2 < pMortality) {
   			   			// Dead
   			   			toBeInfected.toBeKilled = true;	
   			   			// Different distributions
   			   			if(whichDistribution == 1){
   			   				toBeInfected.day = tDie;
   			   			}
   			   			else if(whichDistribution == 2|whichDistribution == 3){
   			   				toBeInfected.day = RandomHelper.createPoisson(tDie).nextInt();
   			   			}
   			   				
   			   		}
   		   		}				
   			}
		}
		
		// Kill or Recover at the end of the time tick - only for asynch update...
		if(Asynch == 1){
			
			if (this.state == 2 && this.day < 0.01){
						
				this.toBeChanged = 3;
				this.state = 3;
				this.day = 0;
				if (this.toBeKilled == true){
							
					this.toBeChanged = 4;
					this.state = 4;
					this.day = 0;
					context.remove(this);
				}				
			}
		}
	}
}
