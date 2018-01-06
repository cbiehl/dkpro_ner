package ner;

import org.dkpro.tc.features.window.WindowFeatureExtractor;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class TheRealWindowFeatureExtractor extends WindowFeatureExtractor<Token>{

	@Override
	protected String getFeatureName() {
		return "TheRealContextWordsFeature";
	}

	@Override
	protected String getFeatureValue(Token t) {
		return t.getCoveredText();
	}

	@Override
	protected Class<Token> getTargetType() {
		return Token.class;
	}
	
}
