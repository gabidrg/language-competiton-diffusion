package LanguageCompetitionDiffusion;

import repast.simphony.context.Context;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.RandomGridAdder;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;
import repast.simphony.valueLayer.GridValueLayer;

public class LanguageCompetitionDiffusionModel implements ContextBuilder<Object>{

	public Context<Object> build(Context<Object> context) {
		
		Parameters p = RunEnvironment.getInstance().getParameters();
		int numAgents = (Integer)p.getValue("initialNumAgents");
		int height = (Integer)p.getValue("worldHeight");
		int width = (Integer)p.getValue("worldWidth");
		int percentX = (Integer)p.getValue("percentX");
		int percentY = (Integer)p.getValue("percentY");
		int agentId = 0;
		
		// Create a new 2D torroidal, single occupancy grid on which the agents will live.
		final Grid<Object> grid = GridFactoryFinder
				.createGridFactory(null)
				.createGrid(
						"Grid", 
						context, 
						new GridBuilderParameters<Object>(
								new WrapAroundBorders(), 
								new SimpleGridAdder<Object>(), 
								true, 
								width, 
								height)
							);
		
		if ((percentX > 0 && percentY > 0) && (percentX < 100 && percentY < 100)) {
			int[] agentGroups = new int[3];
			agentGroups[0] = (numAgents * percentX) / 100;
			agentGroups[1] = (numAgents * percentY) / 100;
			agentGroups[2] = numAgents - agentGroups[0] - agentGroups[1];

				for (int k = 0; k < agentGroups.length; k++) {
					int l = 0;
					int agentType = k + 1;
					while (l < agentGroups[k]) {
						int x = RandomHelper.nextIntFromTo(0, width);
						int y = RandomHelper.nextIntFromTo(0, height);
						//System.out.println(grid.getObjectAt(x, y));
						while (grid.getObjectAt(x, y) == null) {
							Agent agent = new Agent("Agent-" + agentId, agentType);
							context.add(agent);
							grid.moveTo(agent, x, y);
							l++;
							agentId++;
						}
					}	
				}
		}
		else {
			for (int i = 0; i < width; ++i) {
	            for (int j = 0; j < height; ++j) {
	            	Agent agent = new Agent("Agent-" + agentId, 0);
	            	context.add(agent);
	            	agentId++;
                    grid.moveTo(agent, i, j);
	            }
			}
		}
		
		final GridValueLayer neighborhoodLayer = new GridValueLayer(
				"neighborhoodLayer", 
				true, 
				new WrapAroundBorders(), 
				width, 
				height);
		
		context.addValueLayer(neighborhoodLayer);
		
		for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
    				final NeighborhoodCell cell = new NeighborhoodCell(i, j);
                    context.add(cell);
                    grid.moveTo(cell, i, j);
                    neighborhoodLayer.set(cell.getSimilarityIndex(), i, j);
            }
		}
		
		//System.out.println("Layer: " + neighborhoodLayer.getName());
		
		return context;
	}
	
}

