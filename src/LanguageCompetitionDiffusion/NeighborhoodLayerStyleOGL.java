package LanguageCompetitionDiffusion;

import java.awt.Color;

import repast.simphony.util.ContextUtils;
import repast.simphony.valueLayer.ValueLayer;
import repast.simphony.visualizationOGL2D.ValueLayerStyleOGL;


public class NeighborhoodLayerStyleOGL implements ValueLayerStyleOGL {
    protected ValueLayer layer = null;

    @Override
    public void init(final ValueLayer layer) {
		if (null == layer) {
			throw new IllegalArgumentException(
					"Parameter layer cannot be null.");
		}

		if (this.layer != null) {
			throw new IllegalStateException(
					String.format("Food value layer should not be reinitialized with a new ValueLayer instance."));
		}

		this.layer = layer;    
	}

    @Override
    public float getCellSize() {
            return 10.0f;
    }

    @Override
    public Color getColor(final double... coordinates) {
            final double index = layer.get(coordinates);
            final double ratio = (index * 255) / 2;
    		
    	    if (index > 0) {
	    	    final int red = 255 - (int) ratio;
	    	    final int green = 255 - (int) ratio;
	    	    final int blue = 255 - (int) ratio;
	    	    return new Color(red, green, blue);
    	    }
    	    else {
        	    return new Color(255, 255, 255);
    	    }
    	    
    }
}
