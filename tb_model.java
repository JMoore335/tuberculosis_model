package tb_model;

import repast.simphony.context.Context;
import repast.simphony.context.space.graph.NetworkFactoryFinder;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.IAction;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.parameter.ParameterConstants;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.GridDimensions;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.space.grid.RandomGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;

public class tb_model implements ContextBuilder<Object>{

	
	public Context<Object> build(Context<Object> context) {	
	
	Singleton.getSingleton();
	Singleton.totalRun++;
	Singleton.startTime = System.currentTimeMillis();
	// Here assign the parameter values
	Parameters p = RunEnvironment.getInstance().getParameters();
	int population = (Integer)p.getValue("population");
	int initialInfectious = (Integer)p.getValue("initialInfectious");
	int width = (Integer)p.getValue("width");
	int height = (Integer)p.getValue("height");
	double pMortality = (Double)p.getValue("pMortality");
	double timeMultiplier = (Double)p.getValue("timeMultiplier");
	double tDie = (Double)p.getValue("tDie")*timeMultiplier;
	double tRecover = (Double)p.getValue("tRecover")*timeMultiplier;
	int whichDistribution = (Integer)p.getValue("Distribution");
		
	// Create a new 2D grid on which the agents will move.Multi-occupancy 
	Grid<Object> grid = GridFactoryFinder.createGridFactory(null).createGrid("grid", context, 	
			new GridBuilderParameters<Object>(new WrapAroundBorders(), 
				new RandomGridAdder<Object>(), true, width, height));
	// Create the social Network - Kept here in case it is needed to output
		//Network<Object> sNetwork = NetworkFactoryFinder.createNetworkFactory(null).createNetwork(
			//	"socialNetwork", context, false);
		
	// Create the initial agents and add to the context. Their location is randomly assigned. 
	
	// Infected Population
	for(int i=0; i<initialInfectious; i++){

		Human human = new Human("Human-"+ i);
		context.add(human);
		human.toBeChanged = 2;
		human.state = 2;
		double r1 = RandomHelper.nextDoubleFromTo(0, 1);
			
		if (r1 < pMortality) {
			// Dead
			human.toBeKilled = true;			
			// Different distributions
	   		if(whichDistribution == 1){
	   			human.day = tDie;
	   		}
	   		else if(whichDistribution == 2|whichDistribution == 3){
	   			human.day = RandomHelper.createPoisson(tDie).nextInt();
	   		}
		}
		else {
			if(whichDistribution == 1){
	   			human.day = tRecover;
	   		}
	   		else if(whichDistribution == 2|whichDistribution == 3){
	   			human.day = RandomHelper.createPoisson(tRecover).nextInt();
	   		}
		}
	}
	
	// Susceptible Population
	for(int i=initialInfectious; i<population; i++){

		Human human = new Human("Human-"+ i);
		context.add(human);
	} 
	
	// Create the Output Object that is used to print-out the output.
	Output output = new Output();
	// As a note, this method adds the agent on a random location on the Grid.
	context.add(output);
	// Then we can move the agent on a desired cell.
	grid.moveTo(output, 0,0);
	return context;			
	}
}
