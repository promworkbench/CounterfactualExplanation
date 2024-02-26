package org.processmining.CounterfactualRecommendation.parameters;

public enum ClassificationMethod {
	SEM("sem"), LWL("Locally-weighted learning"), RT("Regression tree"), NN("Nural network");

	private final String name;

	private ClassificationMethod(final String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}
}
