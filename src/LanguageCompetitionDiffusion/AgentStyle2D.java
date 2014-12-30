package LanguageCompetitionDiffusion;

import java.awt.Color;

import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;

public class AgentStyle2D extends DefaultStyleOGL2D {
	
	@Override
	public Color getColor(Object o) {
		Agent agent = (Agent)o;
		if (agent.getType() == 1)	
			return Color.RED;
		else if ((agent.getType() == 2))
			return Color.BLUE;
		else if ((agent.getType() == 3))
			return Color.GREEN;
		return null;
	}
	
	@Override
	public float getScale(Object o) {
		return (float) 0.5;
	}

}