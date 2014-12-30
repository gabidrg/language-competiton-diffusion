package LanguageCompetitionDiffusion;

public class Feature {
	
	private int id; // feature type: (1) language, (2, numFeatures) other cultural features
	private int value;
	private boolean active;
	
	public Feature(int id, int value, boolean active) {
		this.setId(id);
		this.setValue(value);
		this.setActive(active);
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
	
	public boolean getActive() {
		return active;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
	
	@Override
	public String toString() {
	    return "Feature id: " + this.getId() + 
	           ", Value: " + this.getValue() +
	           ", Active: " + this.getActive();
	}
	
}
