package ner;

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;


public class AffixFeatureExtractor extends FeatureExtractorResource_ImplBase implements FeatureExtractor {
	public static final String PREFIX_FEATURE = "prefix";
	public static final String SUFFIX_FEATURE = "suffix";
	
	public static final String PARAM_PREFIX_LENGTH = "prefixLength";
	@ConfigurationParameter(name = PARAM_PREFIX_LENGTH, mandatory = false, defaultValue = "2")
    private int prefixLength;
	
	public static final String PARAM_SUFFIX_LENGTH = "suffixLength";
	@ConfigurationParameter(name = PARAM_SUFFIX_LENGTH, mandatory = false, defaultValue = "2")
    private int suffixLength;

	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget classificationUnit) throws TextClassificationException {
		String token = classificationUnit.getCoveredText();
		HashSet<Feature> featureSet = new HashSet<Feature>();
		
		if(token.length() < prefixLength || token.length() < suffixLength) {
			featureSet.add(new Feature(PREFIX_FEATURE, token));
			featureSet.add(new Feature(SUFFIX_FEATURE, token));

		}else {
			String prefix = token.substring(0, prefixLength);
			String suffix = token.substring(token.length() - suffixLength, token.length());
			
			featureSet.add(new Feature(PREFIX_FEATURE, prefix));
			featureSet.add(new Feature(SUFFIX_FEATURE, suffix));
		}
		
		return featureSet; //new Feature(AFFIX_FEATURE, TODO).asSet();
	}
}