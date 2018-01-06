package ner;

import java.util.Set;

import org.apache.uima.jcas.JCas;

import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;


public class OrthographyFeatureExtractor extends FeatureExtractorResource_ImplBase implements FeatureExtractor {

	public static final String ORTHOGRAPHY_STRING = "orthographyFeature";

	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget classificationUnit) throws TextClassificationException {
		
		String token = classificationUnit.getCoveredText();
		
		StringBuilder sb = new StringBuilder();
		for(char c : token.toCharArray()) {
			if(Character.isUpperCase(c)) {
				sb.append('X');
			}
			else if(Character.isDigit(c)) {
				sb.append('#');
			}
			else if(c == '-') {
				sb.append('-');
			}
			else {
				sb.append('x');
			}
		}

		return new Feature(ORTHOGRAPHY_STRING, sb.toString()).asSet();
	}
}