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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import io.vertigo.lang.Assertion;

/**
 * PegRenderer of Grammar as HTML.
 *
 * @author npiedeloup
 */
public final class PegRulesHtmlRenderer {

	private int depth = 1;
	private final Map<PegRule<?>, String> allRules = new LinkedHashMap<>();
	private final Map<Integer, Map<String, PegGrammarRule<?>>> namedRules = new LinkedHashMap<>();

	public String render(final PegRule<?> rootRule) {
		return obtainGrammar(rootRule).entrySet()
				.stream()
				.map(entry -> new StringBuilder()
						.append("<h1 id='").append(entry.getKey()).append("'>").append(entry.getKey()).append("</h1>\n")
						.append("<script>\n")
						.append("Diagram(\n\t")
						.append(entry.getValue())
						.append("\n).addTo();\n")
						.append("</script>\n\n")
						.toString())
				.collect(Collectors.joining());
	}

	/**
	 * @param rootRule Root rule to start with
	 * @return Map of rule HTML rendered grammar element by grammar element name
	 */
	public Map<String, String> obtainGrammar(final PegRule<?> rootRule) {
		detectGrammar(rootRule);
		final Map<String, String> rules = new LinkedHashMap<>();
		for (final Map<String, PegGrammarRule<?>> entry : namedRules.values()) {
			for (final PegGrammarRule<?> pegGrammar : entry.values()) {
				rules.put(pegGrammar.getRuleName(), readGramar(pegGrammar.getRule())); //On resoud les conflits de noms en conservant l'ordre des profondeurs.
			}
		}
		if (rules.isEmpty()) {
			for (final Entry<PegRule<?>, String> pegEntry : allRules.entrySet()) {
				rules.put("Rule-" + pegEntry.hashCode(), pegEntry.getValue());
			}
		}
		return rules;
	}

	private void detectGrammar(final PegRule<?> rule) {
		try {
			depth++;
			if (!namedRules.containsKey(depth)) {
				namedRules.put(depth, new LinkedHashMap<>());
			}
			if (rule instanceof PegChoiceRule) {
				choice((PegChoiceRule) rule);
			} else if (rule instanceof PegManyRule) {
				many((PegManyRule<?>) rule);
			} else if (rule instanceof PegGrammarRule) {
				grammar((PegGrammarRule<?>) rule);
				detectGrammar(((PegGrammarRule<?>) rule).getRule()); //on calcul la grammaire en dessous
			} else if (rule instanceof PegOptionalRule) {
				optional((PegOptionalRule<?>) rule);
			} else if (rule instanceof PegSequenceRule) {
				sequence((PegSequenceRule) rule);
			} else if (rule instanceof PegTermRule) {
				term((PegTermRule) rule);
			} else if (rule instanceof PegWhiteSpaceRule) {
				whiteSpace((PegWhiteSpaceRule) rule);
			} else if (rule instanceof PegWordRule) {
				word((PegWordRule) rule);
			} else if (rule instanceof AbstractRule) {
				detectGrammar(((AbstractRule<?, ?>) rule).getMainRule()); //on calcul la grammaire en dessous, avant de l'associer à cette règle
				populateGramar(rule, readGramar(((AbstractRule<?, ?>) rule).getMainRule()));
			} else {
				populateGramar(rule, rule.getExpression());
			}
		} finally {
			depth--;
		}
	}

	private void grammar(final PegGrammarRule<?> grammarRule) {
		namedRules.get(depth).put(grammarRule.getRuleName(), grammarRule);
		populateGramar(grammarRule, "NonTerminal('" + grammarRule.getRuleName() + "', '#" + grammarRule.getRuleName() + "')");
	}

	private void optional(final PegOptionalRule<?> rule) {
		populateGramar(rule, "Optional(" + readGramar(rule.getRule()) + ")");
	}

	private void term(final PegTermRule term) {
		populateGramar(term, term.getExpression());
	}

	private void sequence(final PegSequenceRule rule) {
		populateGramar(rule, rule.getRules().isEmpty() ? "Skip()"
				: "Sequence("
						+ rule.getRules().stream()
								.map(this::readGramar)
								.collect(Collectors.joining(", "))
						+ ")");
	}

	private void choice(final PegChoiceRule rule) {
		populateGramar(rule, rule.getRules().isEmpty() ? "Skip()"
				: "Choice(0,"
						+ rule.getRules().stream()
								.map(this::readGramar)
								.collect(Collectors.joining(", "))
						+ ")");
	}

	private void many(final PegManyRule<?> rule) {
		populateGramar(rule, (rule.isEmptyAccepted() ? "Zero" : "One") + "OrMore(" + readGramar(rule.getRule()) + ")");
	}

	private void whiteSpace(final PegWhiteSpaceRule rule) {
		populateGramar(rule, "' '");
	}

	private void word(final PegWordRule rule) {
		populateGramar(rule, "NonTerminal('" + rule.getExpression() + "')");
	}

	private void populateGramar(final PegRule<?> rule, final String expressionHtml) {
		Assertion.when(allRules.containsKey(rule)).check(() -> expressionHtml.equals(allRules.get(rule)), "{0} already knowned but different", rule.toString());
		allRules.put(rule, expressionHtml);
	}

	private String readGramar(final PegRule<?> rule) {
		final String result = allRules.get(rule);
		if (result == null) {
			detectGrammar(rule);
			return readGramar(rule);
		}
		return result;
	}

}
