/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, Vertigo.io, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.dynamo.plugins.collections.lucene_6_6;

import java.util.Arrays;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.ElisionFilter;

/**
 * Classe d'analyse des chaïnes de caractères.
 * Gestion des mots vides en français et en anglais.
 * @author  pchretien
 */
final class DefaultAnalyzer extends Analyzer {
	private final CharArraySet stopWords;

	/**
	 * Constructor.
	 * @param useStopWord utilise les stopWord ou non
	 */
	DefaultAnalyzer(final boolean useStopWord) {
		this(useStopWord ? LuceneConstants.OUR_STOP_WORDS : new String[0]);
	}

	/** Builds an analyzer with the given stop words. */
	private DefaultAnalyzer(final String[] stopWords) {
		this.stopWords = StopFilter.makeStopSet(stopWords);
	}

	/**
	   * Creates a TokenStream which tokenizes all the text in the provided Reader.
	   *
	   * @return A TokenStream build from a StandardTokenizer filtered with
	   *         StandardFilter, StopFilter, FrenchStemFilter and LowerCaseFilter
	   */
	@Override
	protected TokenStreamComponents createComponents(final String fieldName) {
		/* initialisation du token */
		final Tokenizer source = new StandardTokenizer();
		//-----
		/* on retire les élisions*/
		final CharArraySet elisionSet = new CharArraySet(Arrays.asList(LuceneConstants.ELISION_ARTICLES), true);
		TokenStream filter = new ElisionFilter(source, elisionSet);
		/* on retire article adjectif */
		filter = new StopFilter(filter, stopWords);
		/* on retire les accents */
		filter = new ASCIIFoldingFilter(filter);
		/* on met en minuscule */
		filter = new LowerCaseFilter(filter);
		return new TokenStreamComponents(source, filter);
	}
}
