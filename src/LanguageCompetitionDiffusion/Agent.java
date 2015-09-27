package LanguageCompetitionDiffusion;

import java.util.ArrayList;

import LanguageCompetitionDiffusion.Transmission;
import LanguageCompetitionDiffusion.Feature;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.query.space.grid.MooreQuery;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

public class Agent {
	
	private int numberOfAgentTypes = 3;
	private String id;
	private int type;
	private int oldType;
	private int speakingX;
	private int speakingY;
	private double densityX;
	private double densityY;
	private int neighborCount;
	private boolean imitated;
	public int speakersX;
	public int speakersY;
	public int speakersXY;
	public double similarityIndex;
	public int numFeatures;
	public ArrayList<Feature> features;

	public Agent(String id, int agentType) {
		if (agentType == 0) {
			this.id = id;
			// The agent is randomly a type from 1 to numberOfAgentTypes
			// Language mapping goes as: Language X = 1, Language Y = 2, Bilinguals speaking XY = 3
			this.type = RandomHelper.nextIntFromTo(1, numberOfAgentTypes);
		}
		else {
			this.id = id;
			this.type = agentType;
		}
		// Build features and populate features and traits according to parameters
		Parameters p = RunEnvironment.getInstance().getParameters();
		this.numFeatures = (Integer)p.getValue("numFeatures");
		int numFeatureTraits = (Integer)p.getValue("numFeatureTraits");
		int randomFeatures = (Integer)p.getValue("randomFeatures");
		ArrayList<Feature> features = new ArrayList<Feature>();
		
		// Language is always first ArrayList object/feature
		features.add(new Feature(0, this.type, true));

		// Randomly populate all remaining features
	    for (int i = 1; i < numFeatures; i++) {
	    	int value = RandomHelper.nextIntFromTo(1, numFeatureTraits);
	    	boolean active = (randomFeatures == 0) ? true : (RandomHelper.nextIntFromTo(0, 1) == 0);
	    	features.add(new Feature(i, value, active));
	    }
	    
	    this.features = features;
	    this.imitated = false;
	    this.similarityIndex = 0;

	}
	
	// Schedule the step method for agents.
	@ScheduledMethod(start = 0, interval = 1, priority = 1)
	public void step() {
		//System.out.println("STEP === Agent ID: " + this.id);
		
		this.imitated = false;
		
		// Get the context and grid, query Moore neighbors in grid
		Context<Object> context = (Context)ContextUtils.getContext(this);
		Grid<Object> grid = (Grid)context.getProjection("Grid");
		MooreQuery<Object> query = new MooreQuery<Object>(grid, this);
		
		// Get a random neighbor agent, Moore query always returns 8 results for fully populated grids
    	int i = 1;
		while (this.imitated == false && i <= 1) {
	    	int randomAgent = RandomHelper.nextIntFromTo(1, 8);
			for (Object result : query.query()){
				Agent neighbor = isAgent(result);
				if (neighbor != null) {
					if (i == randomAgent) {
						if (this.canImitate(this.features, neighbor.features)) {
							this.imitated = this.imitate(query, neighbor.features);
							//System.out.println("Imitated: " + this.imitated);
							break;
						}
						
						//System.out.println("Neighbor ID: " + neighbor.id);
						//System.out.println("Agent: " + this.features);
						//System.out.println("Neighbor: " + neighbor.features);
	
					}
					i++;
				}
			}
		}
	}
	
	private boolean canImitate(ArrayList<Feature> agentFeatures, ArrayList<Feature> neighborFeatures) {
		double similarCount = 0;
		double activeFeaturesAgent = 0;
		double affinityIndex = 0;
	    double roulette = RandomHelper.nextDoubleFromTo(0, 1);
	    
	    // Compute affinityIndex
		for (int i = 0; i < agentFeatures.size(); i++) {
			if (agentFeatures.get(i).getActive()) {
				activeFeaturesAgent++;
				if (neighborFeatures.get(i).getActive() == true && 
					neighborFeatures.get(i).getValue() == agentFeatures.get(i).getValue()) {
					similarCount++;
				}
			}
			// special case for bilinguals, we consider XY similar trait with both Y and X
			if (i == 0 &&  (neighborFeatures.get(i).getValue() == 3 || agentFeatures.get(i).getValue() == 3)) {
				similarCount++;
			}
		}
		affinityIndex = similarCount / activeFeaturesAgent;

		//System.out.println("Agent features:    " + agentFeatures);
		//System.out.println("Neighbor features: " + neighborFeatures);
		//System.out.println("Active features: " + activeFeaturesAgent);
		//System.out.println("Similar features: " + similarCount);
		//System.out.println("Affinity index: " + affinityIndex);
		//System.out.println("Roulette: " + roulette);

		return roulette < affinityIndex ? true : false;
	}
	
	private boolean imitate(MooreQuery<Object> query, ArrayList<Feature> neighborFeatures) {

		// deal with language first!
		
		// initialize group variables
		speakingX = 0;      // number of neighbors speaking X
		speakingY = 0;		// number of neighbors speaking Y
		densityX = 0;
		densityY = 0;
		neighborCount = 0;	// number of neighbors
		
		// initialize transmission probabilities variables
		// common to vertical and horizontal models
		double pXtoX = 0;
		double pYtoY = 0;
		double pXYtoY = 0;  
		// vertical model specific
		double pXYtoX = 0;
		double pXYtoXY = 0;
		// horizontal model specific
		double pXtoXY = 0; 
		double pYtoXY = 0;
		
		// an array to store the transmission objects
		// each transmission has fields: targetLanguage and probability 
		ArrayList<Transmission> transmissions = new ArrayList<Transmission>();
		
		// get model parameters
		Parameters p = RunEnvironment.getInstance().getParameters();
		double statusX = (Double)p.getValue("statusX");
		//System.out.println("statusX: " + statusX);

		double statusY = 1 - statusX;
		double volatility = (Double)p.getValue("volatility");
		double cXYtoX = (Double)p.getValue("cXYtoX");
		double cXYtoY = (Double)p.getValue("cXYtoY");
		double cXtoXY = (Double)p.getValue("cXtoXY");
		double cYtoXY = (Double)p.getValue("cYtoXY");
		double mortalityRate = (Double)p.getValue("mortalityRate");
		int neighborhoodType = (Integer)p.getValue("neighborhoodType");
		int totalAgents = (Integer)p.getValue("initialNumAgents");
		
		// compute probability to adopt Vertical transmission model based on global mortality rate 8.37/1000
		// true adopts vertical model, false adopts horizontal model
		int mortalityPeak = ((int) (1000 / mortalityRate) - 1);
		boolean verticalModel = (RandomHelper.nextIntFromTo(0, mortalityPeak) == 0);

		// initialize reporting variables
		speakersX = this.getSpeakersX();
		speakersY = this.getSpeakersY();
		speakersXY = this.getSpeakersXY();
		
		// Moore neighborhood
		if (neighborhoodType == 1) {
			// Check Moore neighbors and sum speaker types
			for (Object result : query.query()){
				Agent agent = isAgent(result);
				if (agent != null) {
					if (agent.getType() == 1) {
						speakingX++;
					}
					if (agent.getType() == 2) {
						speakingY++;
					}
					neighborCount++;
				}
			}
			
			// compute local densities
			if (neighborCount > 0) {
				densityX = (double) speakingX / neighborCount;
				densityY = (double) speakingY / neighborCount;
			}

		}
		
		// total population based neighborhood, similar to fully connected network
		if (neighborhoodType == 2)  {
			densityX = (double) speakersX / totalAgents;
			densityY = (double) speakersY / totalAgents;
		}


		/* Transmission probabilities for VModel:
		* pXtoX
		* pYtoY
		* pXYtoX
		* pXYtoY 
		* pXYtoXY
		* Transmission probabilities for HModel
		* pXtoX = 0;
		* pXtoXY = 0;
		* pYtoY = 0;
		* pYtoXY = 0;
		* pXYtoXY = 1;
		* compute possible transmissions for each agent type based on spoken language and transmission model
		* 
		*/
		double xPowA = Math.pow(densityX, volatility);
		double yPowA = Math.pow(densityY, volatility);
		
		switch (this.getType()) {
			// agent speaks X
			case 1: 
				if (verticalModel) {
					pXtoX = 1;
					transmissions.add(new Transmission(1, pXtoX));
				}
				else {
					pXtoXY = cXtoXY * statusY * yPowA;
					pXtoX = 1 - pXtoXY;
					transmissions.add(new Transmission(3, pXtoXY));
					transmissions.add(new Transmission(1, pXtoX));
				}
				break;
			// agent speaks Y
			case 2:
				if (verticalModel) {
					pYtoY = 1;
					transmissions.add(new Transmission(2, pYtoY));
				}
				else {					
					pYtoXY = cYtoXY * statusX * xPowA;
					pYtoY = 1 - pYtoXY;					
					transmissions.add(new Transmission(3, pYtoXY));
					transmissions.add(new Transmission(2, pYtoY));
				}
				break;
			// agent speaks XY
			case 3:
				if (verticalModel) {
					pXYtoX = cXYtoX * statusX * xPowA;
					pXYtoY = cXYtoY * statusY * yPowA;
					pXYtoXY = 1 - pXYtoX - pXYtoY;
					transmissions.add(new Transmission(1, pXYtoX));
					transmissions.add(new Transmission(2, pXYtoY));
					transmissions.add(new Transmission(3, pXYtoXY));
				}
				else {
					pXYtoXY = 1;
					transmissions.add(new Transmission(3, pXYtoXY));
				}
				break;
		}
		
		
		// before operating transmission archive existing type for debugging
		this.setOldType(this.getType());
		
		// get the transmission to be operated based on roulette wheel selection if needed
		if (transmissions.size() == 1) {
			// execute transmission directly, there is no stream to weight
			this.setType(transmissions.get(0).getTargetLanguage());
			this.features.get(0).setValue(transmissions.get(0).getTargetLanguage());
		}
		else {
			// extract transmission via roulette procedure
			int candidateLanguage = Agent.selectRouletteWheel(transmissions);
			if (candidateLanguage != 99) {
				this.setType(candidateLanguage);
				this.features.get(0).setValue(candidateLanguage);
			}
		}
		
		/*
		if (this.getOldType() == 3 && this.getType() != 3) {
			System.out.println("densityY: " + densityY);
			System.out.println("volatility: " + volatility);
			System.out.println("yPowA: " + yPowA);
			System.out.println("Start language: " + this.getOldType());
			System.out.println(transmissions);
			System.out.println("End language: " + this.getType());
			System.out.println("X: " + speakersX + " Y: " + speakersY + " XY: " + speakersXY);
		}
		*/
		
		//System.out.println("Start language: " + this.getOldType());
		//System.out.println(transmissions);
		//System.out.println("End language: " + this.getType());
		
		// imitate 1 random active feature, key 0 is language and is already processed
		ArrayList<Integer> activeFeatures = new ArrayList<Integer>();
		for (int i = 1; i < neighborFeatures.size(); i++) {
			if (neighborFeatures.get(i).getActive() && neighborFeatures.get(i).getValue() != this.features.get(i).getValue()) {
				activeFeatures.add(i);
			}
		}
		
		if (activeFeatures.size() > 0) {
			int roulette = RandomHelper.nextIntFromTo(1, activeFeatures.size()); 
			int selectedFeatureId = activeFeatures.get(roulette - 1);
			//System.out.println("Selected feature: " + selectedFeatureId);

			this.features.set(selectedFeatureId, neighborFeatures.get(selectedFeatureId));
			//System.out.println("Imitated features: " + this.features);
		}
		
		// calculate neighborhood similarity
		int similarityIndexCounter = 0;
		for (int i = 0; i < this.features.size(); i++) {
			for (Object result : query.query()){
				Agent agent = isAgent(result);
				if (agent != null) {
					if (this.features.get(i).getValue() == agent.features.get(i).getValue()) {
						similarityIndexCounter++;
					}
				}
			}
		}
		
		this.similarityIndex = (double) similarityIndexCounter / ((double) this.numFeatures * 8);
		final NeighborhoodCell cell = getNeighborhoodCell();
		cell.setSimilarityIndex(this.similarityIndex);
		
		return true;
	}
	
	static int selectRouletteWheel(ArrayList<Transmission> transmissions) {
	    int selectedLanguage = 99;
	    double roulette = RandomHelper.nextDoubleFromTo(0, 1);
		//System.out.println("Roulette: " + roulette);
	    double currentProbability = 0;
	    for (int i = 0; i < transmissions.size(); i++) {
	        currentProbability += transmissions.get(i).getProbability();            
	        if (roulette <= currentProbability) {
	        	selectedLanguage = transmissions.get(i).getTargetLanguage();
	    	    return selectedLanguage;        
	        }
	    }
	    return selectedLanguage;        
	}
	
	private int countSpeakers(int type) {
		@SuppressWarnings("unchecked")
		final Iterable<Agent> agents = RunState.getInstance().getMasterContext().getObjects(Agent.class);
		int speakers = 0;
			for (final Agent agent : agents) {
				if (agent instanceof Agent) {
					if (agent.getType() == type) {
						speakers++;
					}
				}
			}
		return speakers;
	}
	
	public int getType() {
		return type;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	public String getId() {
		return id;
	}
	
	public int getSpeakersX() {
		int speakersX = this.countSpeakers(1);
		return speakersX;
	}
	
	public int getSpeakersY() {
		int speakersX = this.countSpeakers(2);
		return speakersX;
	}
	
	public int getSpeakersXY() {
		int speakersX = this.countSpeakers(3);
		return speakersX;
	}

	public int getOldType() {
		return oldType;
	}

	public void setOldType(int oldType) {
		this.oldType = oldType;
	}
	
	public Grid<Object> getGrid() {
		@SuppressWarnings("unchecked")
		final Grid<Object> grid = (Grid<Object>) ContextUtils.getContext(this)
				.getProjection("Grid");

		if (null == grid) {
			throw new IllegalStateException("Cannot locate grid in context.");
		}

		return grid;
	}
	
	private NeighborhoodCell getNeighborhoodCell() {
        final GridPoint location = getGrid().getLocation(this);
        //System.out.println("Location: " + location);
        final Iterable<Object> objects = getGrid().getObjectsAt(
                        location.getX(), location.getY());

        for (final Object object : objects) {
        	if (object instanceof NeighborhoodCell) {
        		return (NeighborhoodCell) object; 
            }
        }

        return null;
	}
	
	private Agent isAgent(Object object) {
        if (object instanceof Agent) {
        	return (Agent) object;
        }
        return null;
	}
	
}
