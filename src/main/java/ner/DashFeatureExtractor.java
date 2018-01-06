package ner;

import java.util.Set;

import org.apache.uima.jcas.JCas;

import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;


public class DashFeatureExtractor extends FeatureExtractorResource_ImplBase implements FeatureExtractor {
	public static final String CONTAINS_DASH = "containsDash";

	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget classificationUnit)
			throws TextClassificationException {
		String token = classificationUnit.getCoveredText();

		if (token.contains("-")) {
			return new Feature(CONTAINS_DASH, true).asSet();
		} else {
			return new Feature(CONTAINS_DASH, false).asSet();
		}
	}
}