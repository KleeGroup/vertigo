package io.vertigo.dynamox.task;

import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.KDataType;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.task.metamodel.TaskAttribute;
import io.vertigo.kernel.lang.Assertion;

import java.util.Locale;
import java.util.Map;

/**
 * Ce processor permet de remplacer le Where XXX_ID in (YYY.ROWNUM.XXX_ID).
 * @author npiedeloup
 * @version $Id: WhereInPreProcessor.java,v 1.2 2014/01/20 19:01:40 pchretien Exp $
 */
final class WhereInPreProcessor {
	private static final String ROWNUM = ".ROWNUM.";
	private static final int NB_MAX_WHERE_IN_ITEM = 1000;
	private static final String STR_IN = "IN";
	private static final String STR_NOT_IN = "NOT IN";
	private static final char IN_CHAR = '#';

	private final Map<TaskAttribute, Object> parameterValuesMap;

	/**
	 * Contructeur.
	 * @param parameterValuesMap Valeur des paramètres
	 */
	WhereInPreProcessor(final Map<TaskAttribute, Object> parameterValuesMap) {
		Assertion.checkNotNull(parameterValuesMap);
		//---------------------------------------------------------------------
		this.parameterValuesMap = parameterValuesMap;
	}

	public String evaluate(final String sqlQuery) {
		//On commence par vérifier la présence des mot clés.
		if (containsKeywords(sqlQuery)) {
			return doEvaluate(sqlQuery);
		}
		return sqlQuery;
	}

	private boolean containsKeywords(final String sqlQuery) {
		//On vérifie la précense de tous les mots clés (.rownum., in, #)
		return sqlQuery.contains(ROWNUM) && sqlQuery.contains(STR_IN) && sqlQuery.indexOf(IN_CHAR) > -1;
	}

	private String doEvaluate(final String sqlQuery) {
		//TODO : prévoir une implémentation plus simple à base de regexp et de contruction du séquentiel du résultat plutot que par replaceAll
		String query = sqlQuery;
		final StringBuilder subQuery = new StringBuilder();
		final StringBuilder nameSearchTemp = new StringBuilder();
		for (final TaskAttribute attribute : parameterValuesMap.keySet()) {
			final Domain domain = attribute.getDomain();
			if (attribute.isIn() && domain.getDataType() == KDataType.DtList) {
				final String outParamName = attribute.getName();
				nameSearchTemp.setLength(0);
				nameSearchTemp.append(IN_CHAR);
				nameSearchTemp.append(outParamName);
				final int indexStart = query.indexOf(nameSearchTemp.toString());
				if (indexStart != -1) {
					final int indexEnd = query.indexOf(IN_CHAR, indexStart + 1);
					Assertion.checkState(indexEnd > 0, "La fin du paramètre est introuvable ({0})", nameSearchTemp.toString());
					final String strDtc = query.substring(indexStart, indexEnd + 1);
					Assertion.checkState(!strDtc.contains("rownum") && strDtc.contains("ROWNUM"), "Le mot clé ROWNUM est attendu en majuscule ({0})", strDtc);
					//--------------------------------------------------------------------------------------------------
					//TG : recherche parentheses + IN & NOT IN pour contraintre oracle (pas plus de 1000 elements dans clause IN)
					final int indexFirstParenthesis = query.lastIndexOf("(", indexStart + 1);
					int indexOfIn = lastIndexOfIgnoreCase(query, STR_IN, indexFirstParenthesis + 1);
					final int indexOfNotIn = lastIndexOfIgnoreCase(query, STR_NOT_IN, indexFirstParenthesis + 1);
					//Check si NOT IN ou IN
					boolean isNotIn = false;
					if (indexOfNotIn != -1 && indexOfIn - indexOfNotIn < 5) {
						indexOfIn = indexOfNotIn;
						isNotIn = true;
					}
					final int indexOfSecondSpace = query.lastIndexOf(" ", indexOfIn + 1);
					final int indexOfFirstSpace = query.lastIndexOf(" ", indexOfSecondSpace - 1);
					final String fkColumnName = query.substring(indexOfFirstSpace + 1, indexOfSecondSpace).trim();
					final int indexRownum = strDtc.indexOf(ROWNUM);
					final String fieldName = strDtc.substring(indexRownum + ROWNUM.length(), strDtc.length() - 1);

					//---------------------------------------------------------------------------------------------------
					final DtList<?> listObject = (DtList<?>) parameterValuesMap.get(attribute);
					if (listObject.isEmpty()) {
						//Liste vide 
						//-------------------------------------------------------------------------------------------------
						//TG : recherche parentheses + IN & NOT IN pour contraintre oracle (pas plus de 1000 elements dans clause IN)
						query = query.replace(query.substring(indexOfFirstSpace + 1, query.indexOf(")", indexEnd) + 1), isNotIn ? "1=1" : "1=2");
						final KDataType dataType = listObject.getDefinition().getField(fieldName).getDomain().getDataType();
						if (dataType == KDataType.Integer || dataType == KDataType.Long) {
							query = query.replace(strDtc, "-13371337");
						} else {
							query = query.replace(strDtc, "'SHOULD_NOT_MATCH'");
						}
					} else {
						//-------------------------------------------------------------------------------------------------
						//TG : recherche parentheses + IN & NOT IN pour contraintre oracle (pas plus de 1000 elements dans clause IN)
						if (listObject.size() > NB_MAX_WHERE_IN_ITEM) {
							//supprime la parenthese apres le IN
							String query1 = query.substring(0, indexFirstParenthesis);
							String query2 = query.substring(indexFirstParenthesis + 1, query.length());
							query = query1 + query2;
							//rajoute une parenthese avant la pram de recherche
							query1 = query.substring(0, indexOfFirstSpace + 1);
							query2 = query.substring(indexOfFirstSpace + 1, query.length());
							query = query1 + "(" + query2;
						}
						//---------------------------------------------------------------------------------------------------
						String separator = "";
						int index = 1;
						for (final DtObject dto : listObject) {
							if (listObject.size() > NB_MAX_WHERE_IN_ITEM) {
								if (index == 1) {
									subQuery.append("(");
								}
							}
							subQuery.append(separator);
							subQuery.append(IN_CHAR);
							subQuery.append(outParamName);
							subQuery.append(".");
							subQuery.append(String.valueOf(listObject.indexOf(dto)));
							subQuery.append(".");
							subQuery.append(fieldName);
							subQuery.append(IN_CHAR);
							separator = ",";
							//-------------------------------------------------------------------------------------------------
							//TG : recherche parentheses + IN & NOT IN pour contraintre oracle (pas plus de 1000 elements dans clause IN)
							if (listObject.size() > NB_MAX_WHERE_IN_ITEM) {
								if (index == listObject.size()) {
									subQuery.append(")");
								} else if (index % NB_MAX_WHERE_IN_ITEM == 0) {
									subQuery.append(isNotIn ? ") AND " : ") OR ");
									subQuery.append(fkColumnName);
									subQuery.append(isNotIn ? " NOT IN (" : " IN (");
									separator = "";
								}
							}
							//-------------------------------------------------------------------------------------------------
							index++;
						}
						query = query.replace(strDtc, subQuery.toString());
						subQuery.setLength(0);
					}
				}
			}
		}
		return query;
	}

	private int lastIndexOfIgnoreCase(final String string, final String searchString, final int fromIndex) {
		return Math.max(string.lastIndexOf(searchString, fromIndex), string.lastIndexOf(searchString.toLowerCase(Locale.FRENCH), fromIndex));
	}
}