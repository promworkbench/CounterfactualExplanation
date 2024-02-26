package org.processmining.CounterfactualRecommendation.parameters;

public enum LowOrHeigh {
	 LOWER("Lower"), HEIGHER("Heigher");

		private final String name;

		private LowOrHeigh(final String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}
}
