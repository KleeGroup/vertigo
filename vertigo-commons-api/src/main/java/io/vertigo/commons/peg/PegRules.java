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
package io.vertigo.commons.peg;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import io.vertigo.commons.peg.PegRule.Dummy;
import io.vertigo.commons.peg.PegWordRule.Mode;

/**
 * Factory of all PeRules.
 * @author pchretien
 */
public final class PegRules {

	private PegRules() {
		//no constructor for factory class
	}

	/**
	 * Named a Rule.
	 * @param innerRule Rule to name
	 * @param ruleName Rule name
	 * @return Named rule
	 */
	public static <R> PegRule<R> named(final PegRule<R> innerRule, final String ruleName) {
		return new PegGrammarRule<>(innerRule, ruleName);
	}

	/**
	 * @param rule Inner rule
	 * @return Optional rule
	 */
	public static <R> PegRule<Optional<R>> optional(final PegRule<R> rule) {
		return new PegOptionalRule<>(rule);
	}

	/**
	 * @param term Terminal
	 * @return Term rule
	 */
	public static PegRule<String> term(final String term) {
		return new PegTermRule(term);
	}

	/**
	 * @param rules rules list
	 * @return sequence rule of inner rules
	 */
	public static PegRule<List<Object>> sequence(final PegRule<?>... rules) {
		return sequence(Arrays.asList(rules));
	}

	/**
	 * @param rules rules list
	 * @return sequence rule of inner rules
	 */
	public static PegRule<List<Object>> sequence(final List<PegRule<?>> rules) {
		return new PegSequenceRule(rules);
	}

	/**
	 * @param rules the list of rules to test
	 * @return choice rule of inner rules
	 */
	public static PegRule<PegChoice> choice(final PegRule<?>... rules) {
		return choice(Arrays.asList(rules));
	}

	/**
	 * @param rules the list of rules to test
	 * @return choice rule of inner rules
	 */
	public static PegRule<PegChoice> choice(final List<PegRule<?>> rules) {
		return new PegChoiceRule(rules);
	}

	/**
	 * @param rule Rule to repeat
	 * @param untilEnd If text should be parsed entirely
	 * @return zeroOrMore rule
	 */
	public static <R> PegRule<List<R>> zeroOrMore(final PegRule<R> rule, final boolean untilEnd) {
		return new PegManyRule<>(rule, true, untilEnd);
	}

	/**
	 * @param rule Rule to repeat
	 * @param untilEnd If text should be parsed entirely
	 * @return oneOrMore rule
	 */
	public static <R> PegRule<List<R>> oneOrMore(final PegRule<R> rule, final boolean untilEnd) {
		return new PegManyRule<>(rule, false, untilEnd);
	}

	/**
	 * @param blanks list of char to skip
	 * @return Rule to match any blank char
	 */
	public static PegRule<Dummy> skipBlanks(final String blanks) {
		return new PegWhiteSpaceRule(blanks);
	}

	/**
	 * @param emptyAccepted Si les mots vides sont acceptés
	 * @param checkedChars Liste des caractères vérifiés
	 * @param mode Indique le comportement du parseur : si les caractères vérifiés sont les seuls acceptés, sinon les seuls rejetés, et si l'echappement est autorisé
	 * @param readableExpression Expression nommée
	 * @return Word rule (capture a word)
	 */
	public static PegRule<String> word(final boolean emptyAccepted, final String checkedChars, final Mode mode, final String readableExpression) {
		return new PegWordRule(emptyAccepted, checkedChars, mode, readableExpression);
	}

	/**
	 * @param <O> Result object
	 * @param innerRule InnerRule
	 * @return Rule to ensure innerRull match whole text
	 */
	public static <O> PegRule<O> parseAll(final PegRule<O> innerRule) {
		return new PegParseAllRule<>(innerRule);
	}

	/**
	 * @param rootRule Root rule to start with
	 * @return Html railroad diagram
	 */
	public static String namedRulesAsHtml(final PegRule<?> rootRule) {
		final PegRulesHtmlRenderer pegRulesHtmlRenderer = new PegRulesHtmlRenderer();
		return pegRulesHtmlRenderer.render(rootRule);
	}

}
