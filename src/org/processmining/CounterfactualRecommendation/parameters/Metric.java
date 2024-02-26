package org.processmining.CounterfactualRecommendation.parameters;

public enum Metric {
	 L1("L1"), MAD_L1("mad-l1"), L2("L2"), NUMATT("numberAtt"), C_ATT("classAtt");

		private final String name;

		private Metric(final String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}
	}

