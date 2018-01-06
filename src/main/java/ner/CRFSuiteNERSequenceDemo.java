/**
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package ner;

import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.INCLUDE_PREFIX;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.Lab;
import org.dkpro.lab.task.BatchTask.ExecutionPolicy;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.features.entityrecognition.NEFeatureExtractor;
import org.dkpro.tc.features.length.NrOfChars;
import org.dkpro.tc.features.style.InitialCharacterUpperCase;
import org.dkpro.tc.features.style.IsSurroundedByChars;
import org.dkpro.tc.features.window.LemmaTextExtractor;
import org.dkpro.tc.features.window.POSExtractor;
import org.dkpro.tc.features.window.WindowFeatureExtractor;
import org.dkpro.tc.ml.ExperimentCrossValidation;
import org.dkpro.tc.ml.ExperimentTrainTest;
import org.dkpro.tc.ml.crfsuite.CRFSuiteAdapter;
import org.dkpro.tc.ml.report.BatchCrossValidationReport;
import org.dkpro.tc.ml.report.BatchTrainTestReport;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordPosTagger;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;

/**
 * Example for NER as sequence classification.
 */
public class CRFSuiteNERSequenceDemo implements Constants {

	public static final String LANGUAGE_CODE = "de";
	public static final int NUM_FOLDS = 2;
	public static final String corpusFilePathTrain = "src/main/resources/data/train";
	public static final String corpusFilePathDev = "src/main/resources/data/test";
	public static final String corpusFilePathTest = "TODO!!!"; // TODO

	public static File outputFolder = null;

	public static void main(String[] args) throws Exception {
		// Suppress mallet logging output
		System.setProperty("java.util.logging.config.file", "src/main/resources/logging.properties");

		// This is used to ensure that the required DKPRO_HOME environment variable is
		// set.
		// Ensures that people can run the experiments even if they haven't read the
		// setup
		// instructions first :)
		// Don't use this in real experiments! Read the documentation and set DKPRO_HOME
		// as
		// explained there.
		DemoUtils.setDkproHome(CRFSuiteNERSequenceDemo.class.getSimpleName());

		CRFSuiteNERSequenceDemo demo = new CRFSuiteNERSequenceDemo();
		// demo.runCrossValidation(getParameterSpace());
		demo.runTrainTest(getParameterSpace());
	}

	// ##### CV #####
	protected void runCrossValidation(ParameterSpace pSpace) throws Exception {
		ExperimentCrossValidation batch = new ExperimentCrossValidation("NamedEntitySequenceDemoCV",
				CRFSuiteAdapter.class, NUM_FOLDS);
		batch.setPreprocessing(getPreprocessing());
		batch.setParameterSpace(pSpace);
		batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
		batch.addReport(BatchCrossValidationReport.class);

		// Run
		Lab.getInstance().run(batch);
	}

	// ##### Train Test #####
	protected void runTrainTest(ParameterSpace pSpace) throws Exception {
		ExperimentTrainTest batch = new ExperimentTrainTest("NER_DW_CB_TrainTest", CRFSuiteAdapter.class);
		batch.setPreprocessing(getPreprocessing());
		batch.setParameterSpace(pSpace);
		batch.addReport(BatchTrainTestReport.class);
		batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);

		// Run
		Lab.getInstance().run(batch);
	}

	public static ParameterSpace getParameterSpace() throws ResourceInitializationException {

		CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(NERDemoReader.class,
				NERDemoReader.PARAM_LANGUAGE, "en", NERDemoReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
				NERDemoReader.PARAM_PATTERNS, INCLUDE_PREFIX + "*");

		CollectionReaderDescription readerDev = CollectionReaderFactory.createReaderDescription(NERDemoReader.class,
				NERDemoReader.PARAM_LANGUAGE, "en", NERDemoReader.PARAM_SOURCE_LOCATION, corpusFilePathDev,
				NERDemoReader.PARAM_PATTERNS, INCLUDE_PREFIX + "*");
		
//		CollectionReaderDescription readerTest = CollectionReaderFactory.createReaderDescription(NERDemoReader.class,
//				NERDemoReader.PARAM_LANGUAGE, "en", NERDemoReader.PARAM_SOURCE_LOCATION, corpusFilePathTest,
//				NERDemoReader.PARAM_PATTERNS, INCLUDE_PREFIX + "*.txt");

		Map<String, Object> dimReaders = new HashMap<String, Object>();
		dimReaders.put(DIM_READER_TRAIN, readerTrain);
		dimReaders.put(DIM_READER_TEST, readerDev);
		//dimReaders.put(DIM_READER_TEST, readerTest);

		Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(DIM_FEATURE_SET, new TcFeatureSet(
				TcFeatureFactory.create(NrOfChars.class),
				TcFeatureFactory.create(InitialCharacterUpperCase.class),
				TcFeatureFactory.create(OrthographyFeatureExtractor.class),
				TcFeatureFactory.create(AffixFeatureExtractor.class),
				TcFeatureFactory.create(TheRealWindowFeatureExtractor.class, TheRealWindowFeatureExtractor.PARAM_NUM_PRECEEDING, 3, TheRealWindowFeatureExtractor.PARAM_NUM_FOLLOWING, 3)));

		ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
				Dimension.create(DIM_LEARNING_MODE, Constants.LM_SINGLE_LABEL),
				Dimension.create(DIM_FEATURE_MODE, Constants.FM_SEQUENCE), dimFeatureSets);

		return pSpace;
	}

	protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException {
		return createEngineDescription(NoOpAnnotator.class); //(createEngineDescription(StanfordSegmenter.class, StanfordSegmenter.PARAM_LANGUAGE, "en"), createEngineDescription(StanfordPosTagger.class, StanfordPosTagger.PARAM_LANGUAGE, "en"));
	}

}
