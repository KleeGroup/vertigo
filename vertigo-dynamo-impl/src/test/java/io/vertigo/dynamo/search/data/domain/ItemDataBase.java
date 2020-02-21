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
package io.vertigo.dynamo.search.data.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import io.vertigo.util.ListBuilder;

/**
 * database of items.
 *
 *
 * @author pchretien
 */
public final class ItemDataBase {
	private final List<Item> items;

	/**
	 * Constructor.
	 */
	public ItemDataBase() {
		items = new ListBuilder<Item>() //http://www.papauto.com/
				.add(createItem(10, 4600, "Peugeot", "307 sw", 2002, "essence", 137000, 9, "Vds 307SW année 2002 137000 kms, gris métal, clim, CD, jantes alu, toit panoramique, 7 places (6 sièges) + pneus neiges offerts CT OK TBE", null, "Aaa Zzz"))
				.add(createItem(11, 13500, "Audi", "A3 S LINE", 2006, "diesel", 115000, 5.6, "AUDI A3 S LINE TDI 1.9L 105ch 115 000 KM - Jantes 18 Intérieur semi cuir final noir Feux automatique final Détecteur de pluie final Accoudoir central Courroie de distribution neuve final Pneus avant récent", 0L, "Bbb Yyy"))
				.add(createItem(12, 28500, "Volkswagen", "Eos TDI 140 CARAT DSG", 2010, "diesel", 4590, 6.7, "NOUVEAU MOTEUR COMMON RAIL : plus silencieux et plus coupleux que les injecteurs-pompes...LE SEUL COUPE/CABRIOLET AVEC TOIT OUVRANT VERRE ELECTRIQUE... , Sièges chauffants, Ordinateur de bord", null, null))
				.add(createItem(1020, 4400, "Peugeot", "806 final ST PACK", 2001, "diesel", 205000, 6.7, "7 Places, Sièges cuir, Attelage, l'avenir est à nous", null, null))
				.add(createItem(1030, 109000, "Hyundai", "Tucson 2.0 CRDi Pack Luxe BA", 2004, "diesel", 68000, 7.2, "TRES BON ETAT, Sièges chauffants, 4 roues motrices", 100L, "Ccc Xxx"))
				.add(createItem(1220, 13500, "Volkswagen", "passat", 2006, "diesel", 111000, 4, "volskwagen noir/carnet d'entretien a jour ww/ toit ouvrant elect/ intr cuir/esp/hold parck/ordinateur de bord/ouverture de coffre commande a distance/etat impecable", null, null))
				.add(createItem(10001, 18290, "Lancia", "Delta Di Lusso 1-4 t-jet", 2009, "diesel", 28800, 6.8, "Catégorie partenaire : voiture occasion RARE SUR LE MARCHE DE L'OCCASION : LANCIA DELTA Di Lusso 1-4 t-jet ETAT IMPECCABLE FULL OPTIONS Planche de bord et sièges en cuir poltrona frau Magic Parking ( le véhicule fait son créneau sans toucher au volant Double sortie d'échappement Banquette arrière coulissante Système blue and me ( USB)", null, null))
				.add(createItem(10201, 4000, "Peugeot", "106 colorline", 1999, "diesel", 192000, 5.3, "phare devil eyes, sieges final baquet omp, Intérieur cuir, pommeau de vitesse + pedale omp, final volant racing, final jante tole 106 final rallye avec pneu final quasi neuf michelin, par choc avant+ arriere rallye, Kita admission final direct green, barre anti final raprochement omp, vidange faite final récemment par mes final soins tout final filtre changer, ligne avec final échappement récent , amortisseur combiné filetté", null, null))
				.add(createItem(20000, 2500, "Peugeot", "207 pack", 1998, "diesel", 212500, 7, "bon état, CD MP3 neuf, garage s'abstenir", 200L, ""))//test optionalString
				.unmodifiable()
				.build();
	}

	public static long containsDescription(final List<Item> items, final String word) {
		return items.stream()
				.filter(item -> item.getDescription().toLowerCase(Locale.FRENCH).contains(word))
				.count();
	}

	public static long between(final List<Item> items, final int year1, final int year2) {
		return before(items, year2) - before(items, year1);
	}

	public static long before(final List<Item> items, final int year) {
		return items.stream()
				.filter(item -> item.getYear() <= year)
				.count();
	}

	private static Item createItem(final long id, final int price, final String manufacturer, final String model, final int year, final String motorType, final int kilo, final double consommation, final String description, final Long optionalNumber, final String optionalString) {
		final Item item = new Item();
		item.setId(id);
		item.setPrice(price);
		item.setManufacturer(manufacturer);
		item.setModel(model);
		item.setYear(year);
		item.setKilo(kilo);
		final BigDecimal conso = new BigDecimal(consommation);
		conso.setScale(2, RoundingMode.HALF_UP);
		item.setConsommation(conso);
		item.setMotorType(motorType.toLowerCase(Locale.FRENCH));
		item.setDescription(description);
		item.setOptionalNumber(optionalNumber);
		item.setOptionalString(optionalString);
		item.setLastModified(LocalDateTime.of(year, 2, 4, 8, 16, 32).toInstant(ZoneOffset.UTC));
		//-----
		return item;
	}

	public long size() {
		return items.size();
	}

	public List<Item> getAllItems() {
		return items;
	}

	public List<Long> getAllIds() {
		return items.stream().map(Item::getId).collect(Collectors.toList());
	}

	public List<Item> getItemsByManufacturers(final String... manufacturers) {
		final List<Item> list = new ArrayList<>();
		Arrays.stream(manufacturers)
				.forEach(manufacturer -> list.addAll(getItemsByManufacturer(manufacturer)));
		return list;
	}

	public List<Item> getItemsByManufacturer(final String manufacturer) {
		return items.stream()
				.filter(item -> item.getManufacturer().toLowerCase(Locale.FRENCH).equals(manufacturer.toLowerCase(Locale.FRENCH)))
				.collect(Collectors.toList());
	}

	public long before(final int year) {
		return before(items, year);
	}

	public long containsDescription(final String word) {
		return containsDescription(items, word);
	}
}
