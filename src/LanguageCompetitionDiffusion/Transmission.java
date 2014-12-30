package LanguageCompetitionDiffusion;

public class Transmission {
	
	private double probability;
	private int targetLanguage;

	public Transmission(int targetLanguage, double probability) {
		this.setTargetLanguage(targetLanguage);
		this.setProbability(probability);
	}

	public int getTargetLanguage() {
		return targetLanguage;
	}

	public void setTargetLanguage(int targetLanguage) {
		this.targetLanguage = targetLanguage;
	}

	public double getProbability() {
		return probability;
	}

	public void setProbability(double probability) {
		this.probability = probability;
	}
	
	@Override
	public String toString() {
	    return "Target language: " + this.getTargetLanguage() + 
	           ", Probability: " + this.getProbability();
	}

}
