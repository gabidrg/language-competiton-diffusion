package LanguageCompetitionDiffusion;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.util.ContextUtils;
import repast.simphony.valueLayer.GridValueLayer;

public class NeighborhoodCell {
    private final int x, y;
	private double similarityIndex = 1;

	public NeighborhoodCell(final int x, final int y) {
		this.x = x;
		this.y = y;
	}

	// Schedule the step method for agents.
	@ScheduledMethod(start = 0, interval = 1, priority = -1)
	public void stepNeighborhood() {
		final GridValueLayer neighborhoodLayer = (GridValueLayer) ContextUtils
                								.getContext(this).getValueLayer("neighborhoodLayer");
		System.out.println("Similarity: " + this.getSimilarityIndex());
		neighborhoodLayer.set(this.getSimilarityIndex(), x, y);
	}
	
	public double getSimilarityIndex() {
		return this.similarityIndex;
	}
	
	public void setSimilarityIndex(double similarityIndex2) {
		this.similarityIndex = similarityIndex2;
	}
	
}
