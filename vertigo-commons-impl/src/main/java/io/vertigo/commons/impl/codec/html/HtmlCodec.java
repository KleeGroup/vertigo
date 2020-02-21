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
package io.vertigo.commons.impl.codec.html;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Codage pour encoder des chaines au format HTML.
 *
 * http://www.w3.org/TR/html401/sgml/entities.html
 *
 * @author npiedeloup, pchretien
 */
public final class HtmlCodec extends AbstractCodec {
	private static final String ESCAPE_PATTERN_STRING = "&#[0-9]{2,4};";
	private final Pattern pattern;

	/**
	 * Constructor.
	 */
	public HtmlCodec() {
		super('&', ';', getCharacters());
		pattern = Pattern.compile(ESCAPE_PATTERN_STRING);
	}

	/**
	 * Mapping des caractères spéciaux vers leur code HTML.
	 * Respecter l'espace entre le caractere et son code
	 * Les caracteres ont été remplacés par des caractères
	 * unicode UTF-16
	 * @return  tableau des éléments à remplacer
	 */
	private static String[] getCharacters() {
		//basé sur Character entity references in HTML 4 : http://www.w3.org/TR/html4/sgml/entities.html#iso-88591
		//replace UltraEdit (regExp):
		// '\p    [ ]+'  -> ' '
		// puis '^\<\!ENTITY ([a-z0-9]+).*\#([0-9]+);.*-- (.*) U\+.*$' -> '(char) \2 + " &amp;\1;", //&\1;  : \3'
		// puis '<!--(.*)-->' -> '/** \1 */'
		// et enfin '\p' -> '\p<br>'
		// on copie colle depuis un browser web (pour les caractères spéciaux, ont les enlèves pour bien laisser le fichier en charset Standard

		return new String[] {
				/**********************************************************/
				/** Character entity references for ISO 8859-1 characters */
				/**********************************************************/
				(char) 160 + " &nbsp;", //  : no-break space = non-breaking space,
				(char) 161 + " &iexcl;", //à : inverted exclamation mark,
				(char) 162 + " &cent;", //à : cent sign,
				(char) 163 + " &pound;", //à : pound sign,
				(char) 164 + " &curren;", //à : currency sign,
				(char) 165 + " &yen;", //à : yen sign = yuan sign,
				(char) 166 + " &brvbar;", //à : broken bar = broken vertical bar,
				(char) 167 + " &sect;", //à : section sign,
				(char) 168 + " &uml;", //à : diaeresis = spacing diaeresis,
				(char) 169 + " &copy;", //à : copyright sign,
				(char) 170 + " &ordf;", //à : feminine ordinal indicator,
				(char) 171 + " &laquo;", //à : left-pointing double angle quotation mark = left pointing guillemet,
				(char) 172 + " &not;", //à : not sign,
				(char) 173 + " &shy;", //à : soft hyphen = discretionary hyphen,
				(char) 174 + " &reg;", //à : registered sign = registered trade mark sign,
				(char) 175 + " &macr;", //à : macron = spacing macron = overline = APL overbar,
				(char) 176 + " &deg;", //à : degree sign,
				(char) 177 + " &plusmn;", //à : plus-minus sign = plus-or-minus sign,
				(char) 178 + " &sup2;", //à : superscript two = superscript digit two = squared,
				(char) 179 + " &sup3;", //à : superscript three = superscript digit three = cubed,
				(char) 180 + " &acute;", //à : acute accent = spacing acute,
				(char) 181 + " &micro;", //à : micro sign,
				(char) 182 + " &para;", //à : pilcrow sign = paragraph sign,
				(char) 183 + " &middot;", //à : middle dot = Georgian comma = Greek middle dot,
				(char) 184 + " &cedil;", //à : cedilla = spacing cedilla,
				(char) 185 + " &sup1;", //à : superscript one = superscript digit one,
				(char) 186 + " &ordm;", //à : masculine ordinal indicator,
				(char) 187 + " &raquo;", //à : right-pointing double angle quotation mark = right pointing guillemet,
				(char) 188 + " &frac14;", //à : vulgar fraction one quarter = fraction one quarter,
				(char) 189 + " &frac12;", //à : vulgar fraction one half = fraction one half,
				(char) 190 + " &frac34;", //à : vulgar fraction three quarters = fraction three quarters,
				(char) 191 + " &iquest;", //à : inverted question mark = turned question mark,
				(char) 192 + " &Agrave;", //à : latin capital letter A with grave = latin capital letter A grave,
				(char) 193 + " &Aacute;", //à : latin capital letter A with acute,
				(char) 194 + " &Acirc;", //à : latin capital letter A with circumflex,
				(char) 195 + " &Atilde;", //à : latin capital letter A with tilde,
				(char) 196 + " &Auml;", //à : latin capital letter A with diaeresis,
				(char) 197 + " &Aring;", //à : latin capital letter A with ring above = latin capital letter A ring,
				(char) 198 + " &AElig;", //à : latin capital letter AE = latin capital ligature AE,
				(char) 199 + " &Ccedil;", //à : latin capital letter C with cedilla,
				(char) 200 + " &Egrave;", //à : latin capital letter E with grave,
				(char) 201 + " &Eacute;", //à : latin capital letter E with acute,
				(char) 202 + " &Ecirc;", //à : latin capital letter E with circumflex,
				(char) 203 + " &Euml;", //à : latin capital letter E with diaeresis,
				(char) 204 + " &Igrave;", //à : latin capital letter I with grave,
				(char) 205 + " &Iacute;", //à : latin capital letter I with acute,
				(char) 206 + " &Icirc;", //à : latin capital letter I with circumflex,
				(char) 207 + " &Iuml;", //à : latin capital letter I with diaeresis,
				(char) 208 + " &ETH;", //à : latin capital letter ETH,
				(char) 209 + " &Ntilde;", //à : latin capital letter N with tilde,
				(char) 210 + " &Ograve;", //à : latin capital letter O with grave,
				(char) 211 + " &Oacute;", //à : latin capital letter O with acute,
				(char) 212 + " &Ocirc;", //à : latin capital letter O with circumflex,
				(char) 213 + " &Otilde;", //à : latin capital letter O with tilde,
				(char) 214 + " &Ouml;", //à : latin capital letter O with diaeresis,
				(char) 215 + " &times;", //à : multiplication sign,
				(char) 216 + " &Oslash;", //à : latin capital letter O with stroke = latin capital letter O slash,
				(char) 217 + " &Ugrave;", //à : latin capital letter U with grave,
				(char) 218 + " &Uacute;", //à : latin capital letter U with acute,
				(char) 219 + " &Ucirc;", //à : latin capital letter U with circumflex,
				(char) 220 + " &Uuml;", //à : latin capital letter U with diaeresis,
				(char) 221 + " &Yacute;", //à : latin capital letter Y with acute,
				(char) 222 + " &THORN;", //à : latin capital letter THORN,
				(char) 223 + " &szlig;", //à : latin small letter sharp s = ess-zed,
				(char) 224 + " &agrave;", //à : latin small letter a with grave = latin small letter a grave,
				(char) 225 + " &aacute;", //à : latin small letter a with acute,
				(char) 226 + " &acirc;", //à : latin small letter a with circumflex,
				(char) 227 + " &atilde;", //à : latin small letter a with tilde,
				(char) 228 + " &auml;", //à : latin small letter a with diaeresis,
				(char) 229 + " &aring;", //à : latin small letter a with ring above = latin small letter a ring,
				(char) 230 + " &aelig;", //à : latin small letter ae = latin small ligature ae,
				(char) 231 + " &ccedil;", //à : latin small letter c with cedilla,
				(char) 232 + " &egrave;", //à : latin small letter e with grave,
				(char) 233 + " &eacute;", //à : latin small letter e with acute,
				(char) 234 + " &ecirc;", //à : latin small letter e with circumflex,
				(char) 235 + " &euml;", //à : latin small letter e with diaeresis,
				(char) 236 + " &igrave;", //à : latin small letter i with grave,
				(char) 237 + " &iacute;", //à : latin small letter i with acute,
				(char) 238 + " &icirc;", //à : latin small letter i with circumflex,
				(char) 239 + " &iuml;", //à : latin small letter i with diaeresis,
				(char) 240 + " &eth;", //à : latin small letter eth,
				(char) 241 + " &ntilde;", //à : latin small letter n with tilde,
				(char) 242 + " &ograve;", //à : latin small letter o with grave,
				(char) 243 + " &oacute;", //à : latin small letter o with acute,
				(char) 244 + " &ocirc;", //à : latin small letter o with circumflex,
				(char) 245 + " &otilde;", //à : latin small letter o with tilde,
				(char) 246 + " &ouml;", //à : latin small letter o with diaeresis,
				(char) 247 + " &divide;", //à : division sign,
				(char) 248 + " &oslash;", //à : latin small letter o with stroke, = latin small letter o slash,
				(char) 249 + " &ugrave;", //à : latin small letter u with grave,
				(char) 250 + " &uacute;", //à : latin small letter u with acute,
				(char) 251 + " &ucirc;", //à : latin small letter u with circumflex,
				(char) 252 + " &uuml;", //à : latin small letter u with diaeresis,
				(char) 253 + " &yacute;", //à : latin small letter y with acute,
				(char) 254 + " &thorn;", //à : latin small letter thorn,
				(char) 255 + " &yuml;", //à : latin small letter y with diaeresis,

				/*************************************************************************************/
				/** Character entity references for symbols, mathematical symbols, and Greek letters */
				/*************************************************************************************/
				/** Latin Extended-B */
				(char) 402 + " &fnof;", //latin small f with hook = function = florin,

				/** Greek */
				(char) 913 + " &Alpha;", //greek capital letter alpha,
				(char) 914 + " &Beta;", //greek capital letter beta,
				(char) 915 + " &Gamma;", //greek capital letter gamma,
				(char) 916 + " &Delta;", //greek capital letter delta,
				(char) 917 + " &Epsilon;", //greek capital letter epsilon,
				(char) 918 + " &Zeta;", //greek capital letter zeta,
				(char) 919 + " &Eta;", //greek capital letter eta,
				(char) 920 + " &Theta;", //greek capital letter theta,
				(char) 921 + " &Iota;", //greek capital letter iota,
				(char) 922 + " &Kappa;", //greek capital letter kappa,
				(char) 923 + " &Lambda;", //greek capital letter lambda,
				(char) 924 + " &Mu;", //greek capital letter mu,
				(char) 925 + " &Nu;", //greek capital letter nu,
				(char) 926 + " &Xi;", //greek capital letter xi,
				(char) 927 + " &Omicron;", //greek capital letter omicron,
				(char) 928 + " &Pi;", //greek capital letter pi,
				(char) 929 + " &Rho;", //greek capital letter rho,
				/** there is no Sigmaf, and no U+03A2 character either */
				(char) 931 + " &Sigma;", //greek capital letter sigma,
				(char) 932 + " &Tau;", //greek capital letter tau,
				(char) 933 + " &Upsilon;", //greek capital letter upsilon,
				(char) 934 + " &Phi;", //greek capital letter phi,
				(char) 935 + " &Chi;", //greek capital letter chi,
				(char) 936 + " &Psi;", //greek capital letter psi,
				(char) 937 + " &Omega;", //greek capital letter omega,

				(char) 945 + " &alpha;", //greek small letter alpha,
				(char) 946 + " &beta;", //greek small letter beta,
				(char) 947 + " &gamma;", //greek small letter gamma,
				(char) 948 + " &delta;", //greek small letter delta,
				(char) 949 + " &epsilon;", //greek small letter epsilon,
				(char) 950 + " &zeta;", //greek small letter zeta,
				(char) 951 + " &eta;", //greek small letter eta,
				(char) 952 + " &theta;", //greek small letter theta,
				(char) 953 + " &iota;", //greek small letter iota,
				(char) 954 + " &kappa;", //greek small letter kappa,
				(char) 955 + " &lambda;", //greek small letter lambda,
				(char) 956 + " &mu;", //greek small letter mu,
				(char) 957 + " &nu;", //greek small letter nu,
				(char) 958 + " &xi;", //greek small letter xi,
				(char) 959 + " &omicron;", //greek small letter omicron,
				(char) 960 + " &pi;", //greek small letter pi,
				(char) 961 + " &rho;", //greek small letter rho,
				(char) 962 + " &sigmaf;", //greek small letter final sigma,
				(char) 963 + " &sigma;", //greek small letter sigma,
				(char) 964 + " &tau;", //greek small letter tau,
				(char) 965 + " &upsilon;", //greek small letter upsilon,
				(char) 966 + " &phi;", //greek small letter phi,
				(char) 967 + " &chi;", //greek small letter chi,
				(char) 968 + " &psi;", //greek small letter psi,
				(char) 969 + " &omega;", //greek small letter omega,
				(char) 977 + " &thetasym;", //greek small letter theta symbol,
				(char) 978 + " &upsih;", //greek upsilon with hook symbol,
				(char) 982 + " &piv;", //greek pi symbol,

				/** General Punctuation */
				(char) 8226 + " &bull;", //bullet = black small circle,
				/** bullet is NOT the same as bullet operator, U+2219 */
				(char) 8230 + " &hellip;", //horizontal ellipsis = three dot leader,
				(char) 8242 + " &prime;", //prime = minutes = feet,
				(char) 8243 + " &Prime;", //double prime = seconds = inches,
				(char) 8254 + " &oline;", //overline = spacing overscore,
				(char) 8260 + " &frasl;", //fraction slash,

				/** Letterlike Symbols */
				(char) 8472 + " &weierp;", //script capital P = power set = Weierstrass p,
				(char) 8465 + " &image;", //blackletter capital I = imaginary part,
				(char) 8476 + " &real;", //blackletter capital R = real part symbol,
				(char) 8482 + " &trade;", //trade mark sign,
				(char) 8501 + " &alefsym;", //alef symbol = first transfinite cardinal,
				/** alef symbol is NOT the same as hebrew letter alef, U+05D0 although the same glyph could be used to depict both characters */

				/** Arrows */
				(char) 8592 + " &larr;", //leftwards arrow,
				(char) 8593 + " &uarr;", //upwards arrow,
				(char) 8594 + " &rarr;", //rightwards arrow,
				(char) 8595 + " &darr;", //downwards arrow,
				(char) 8596 + " &harr;", //left right arrow,
				(char) 8629 + " &crarr;", //downwards arrow with corner leftwards = carriage return,
				(char) 8656 + " &lArr;", //leftwards double arrow,
				(char) 8657 + " &uArr;", //upwards double arrow,
				(char) 8658 + " &rArr;", //rightwards double arrow,
				/** ISO 10646 does not say this is the 'implies' character but does not have another character with this function so ? rArr can be used for 'implies' as ISOtech suggests */
				(char) 8659 + " &dArr;", //downwards double arrow,
				(char) 8660 + " &hArr;", //left right double arrow,

				/** Mathematical Operators */
				(char) 8704 + " &forall;", //for all,
				(char) 8706 + " &part;", //partial differential,
				(char) 8707 + " &exist;", //there exists,
				(char) 8709 + " &empty;", //empty set = null set = diameter,
				(char) 8711 + " &nabla;", //nabla = backward difference,
				(char) 8712 + " &isin;", //element of,
				(char) 8713 + " &notin;", //not an element of,
				(char) 8715 + " &ni;", //contains as member,
				/** should there be a more memorable name than 'ni'? */
				(char) 8719 + " &prod;", //n-ary product = product sign,
				/** prod is NOT the same character as U+03A0 'greek capital letter pi' though the same glyph might be used for both */
				(char) 8721 + " &sum;", //n-ary sumation,
				/** sum is NOT the same character as U+03A3 'greek capital letter sigma' though the same glyph might be used for both */
				(char) 8722 + " &minus;", //minus sign,
				(char) 8727 + " &lowast;", //asterisk operator,
				(char) 8730 + " &radic;", //square root = radical sign,
				(char) 8733 + " &prop;", //proportional to,
				(char) 8734 + " &infin;", //infinity,
				(char) 8736 + " &ang;", //angle,
				(char) 8743 + " &and;", //logical and = wedge,
				(char) 8744 + " &or;", //logical or = vee,
				(char) 8745 + " &cap;", //intersection = cap,
				(char) 8746 + " &cup;", //union = cup,
				(char) 8747 + " &int;", //integral,
				(char) 8756 + " &there4;", //therefore,
				(char) 8764 + " &sim;", //tilde operator = varies with = similar to,
				/** tilde operator is NOT the same character as the tilde, U+007E, although the same glyph might be used to represent both */
				(char) 8773 + " &cong;", //approximately equal to,
				(char) 8776 + " &asymp;", //almost equal to = asymptotic to,
				(char) 8800 + " &ne;", //not equal to,
				(char) 8801 + " &equiv;", //identical to,
				(char) 8804 + " &le;", //less-than or equal to,
				(char) 8805 + " &ge;", //greater-than or equal to,
				(char) 8834 + " &sub;", //subset of,
				(char) 8835 + " &sup;", //superset of,
				/** note that nsup, 'not a superset of, U+2283' is not covered by the Symbol font encoding and is not included. Should it be, for symmetry? It is in ISOamsn */
				(char) 8836 + " &nsub;", //not a subset of,
				(char) 8838 + " &sube;", //subset of or equal to,
				(char) 8839 + " &supe;", //superset of or equal to,
				(char) 8853 + " &oplus;", //circled plus = direct sum,
				(char) 8855 + " &otimes;", //circled times = vector product,
				(char) 8869 + " &perp;", //up tack = orthogonal to = perpendicular,
				(char) 8901 + " &sdot;", //dot operator,
				/** dot operator is NOT the same character as U+00B7 middle dot */

				/** Miscellaneous Technical */
				(char) 8968 + " &lceil;", //left ceiling = apl upstile,
				(char) 8969 + " &rceil;", //right ceiling,
				(char) 8970 + " &lfloor;", //left floor = apl downstile,
				(char) 8971 + " &rfloor;", //right floor,
				(char) 9001 + " &lang;", //left-pointing angle bracket = bra,
				/** lang is NOT the same character as U+003C 'less than' or U+2039 'single left-pointing angle quotation mark' */
				(char) 9002 + " &rang;", //right-pointing angle bracket = ket,
				/** rang is NOT the same character as U+003E 'greater than' or U+203A 'single right-pointing angle quotation mark' */

				/** Geometric Shapes */
				(char) 9674 + " &loz;", //lozenge,

				/** Miscellaneous Symbols */
				(char) 9824 + " &spades;", //black spade suit,
				/** black here seems to mean filled as opposed to hollow */
				(char) 9827 + " &clubs;", //black club suit = shamrock,
				(char) 9829 + " &hearts;", //black heart suit = valentine,
				(char) 9830 + " &diams;", //black diamond suit,

				/*******************************************************************************************/
				/** Character entity references for markup-significant and internationalization characters */
				/*******************************************************************************************/
				/** C0 Controls and Basic Latin */
				(char) 34 + " &quot;", //quotation mark = APL quote,
				(char) 38 + " &amp;", //ampersand,
				(char) 60 + " &lt;", //less-than sign,
				(char) 62 + " &gt;", //greater-than sign,

				/** Latin Extended-A */
				(char) 338 + " &OElig;", //latin capital ligature OE,
				(char) 339 + " &oelig;", //latin small ligature oe,
				/** ligature is a misnomer, this is a separate character in some languages */
				(char) 352 + " &Scaron;", //latin capital letter S with caron,
				(char) 353 + " &scaron;", //latin small letter s with caron,
				(char) 376 + " &Yuml;", //latin capital letter Y with diaeresis,

				/** Spacing Modifier Letters */
				(char) 710 + " &circ;", //modifier letter circumflex accent,
				(char) 732 + " &tilde;", //small tilde,

				/** General Punctuation */
				(char) 8194 + " &ensp;", //en space,
				(char) 8195 + " &emsp;", //em space,
				(char) 8201 + " &thinsp;", //thin space,
				(char) 8204 + " &zwnj;", //zero width non-joiner,
				(char) 8205 + " &zwj;", //zero width joiner,
				(char) 8206 + " &lrm;", //left-to-right mark,
				(char) 8207 + " &rlm;", //right-to-left mark,
				(char) 8211 + " &ndash;", //en dash,
				(char) 8212 + " &mdash;", //em dash,
				(char) 8216 + " &lsquo;", //left single quotation mark,
				(char) 8217 + " &rsquo;", //right single quotation mark,
				(char) 8218 + " &sbquo;", //single low-9 quotation mark,
				(char) 8220 + " &ldquo;", //left double quotation mark,
				(char) 8221 + " &rdquo;", //right double quotation mark,
				(char) 8222 + " &bdquo;", //double low-9 quotation mark,
				(char) 8224 + " &dagger;", //dagger,
				(char) 8225 + " &Dagger;", //double dagger,
				(char) 8240 + " &permil;", //per mille sign,
				(char) 8249 + " &lsaquo;", //single left-pointing angle quotation mark,
				/** lsaquo is proposed but not yet ISO standardized */
				(char) 8250 + " &rsaquo;", //single right-pointing angle quotation mark,
				/** rsaquo is proposed but not yet ISO standardized */
				(char) 8364 + " &euro;", //euro sign,

				/*************************************************************/
				/** Complément KLEE                                          */
				/*************************************************************/
				(char) 39 + " &#39;", //caractere en collision avec l'apostrophe de word

				/** Ces caractères correspondent à un second encodage de caractère HTML
				 * (NB : le code ascii 128 pour l'euro est prioritaire sur 8364 car il y a un sort
				 * le décodage n'est donc pas forcément bijectif avec l'encodage). */
				(char) 146 + " &#39;", // apostrophe de Word
				(char) 128 + " &euro;", //euro sign
				(char) 8217 + " &#8217;", // apostrophe de Word
				//Ci-dessous des encodages supplémentaires mais non compatibles avec le contrat du codec (start with & et end with ;)
				//'\n' + " <br/>", // retour chariot
				//'\t' + " &nbsp; &nbsp; ", // tabulation sécable
		};
	}

	/** {@inheritDoc} */
	@Override
	public String decode(final String encoded) {
		if (encoded == null) {
			return null;
		}
		return doDecode(encoded);
	}

	/** {@inheritDoc} */
	@Override
	public String encode(final String toEncode) {
		if (toEncode == null) {
			return "";
		}
		return doEncode(toEncode);
	}

	/** {@inheritDoc} */
	@Override
	protected boolean shouldEncode(final char c, final int index, final String stringToEncode) {
		if (c == '&') {
			//cas particulier pour html, le navigateur envoi les caractères incompatible sous la forme : &#xxxx; il n faut pas encoder le & dans ce cas
			final Matcher matcher = pattern.matcher(stringToEncode);
			matcher.region(index, Math.min(index + 7, stringToEncode.length()));
			if (matcher.find()) {
				//si l'index match avec la regExp on n'encode pas
				return matcher.start() != index;
			}
		}
		return true; //on encode
	}
}
