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
package io.vertigo.dynamo.store.data.domain.car;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.util.VCollectors;
import io.vertigo.util.ListBuilder;

/**
 * Base de données des voitures.
 *
 *
 * @author pchretien
 */
public final class CarDataBase {
	private final List<Car> cars;

	/**
	 * Constructor.
	 */
	public CarDataBase() {
		long id = 0;
		cars = new ListBuilder<Car>()
				.add(createCar(id++, 4600, "Peugeot", "307 sw", 2002, MotorTypeEnum.essence, 137000, 9, "Vds 307SW année 2002 137000 kms, gris métal, clim, CD, jantes alu, toit panoramique, 7 places (6 sièges) + pneus neiges offerts CT OK TBE"))
				.add(createCar(id++, 13500, "Audi", "A3 S LINE", 2006, MotorTypeEnum.diesel, 115000, 5.6, "AUDI A3 S LINE TDI 1.9L 105ch 115 000 KM - Jantes 18 Intérieur semi cuir final noir Feux automatique final Détecteur de pluie final Accoudoir central Courroie de distribution neuve final Pneus avant récent"))
				.add(createCar(id++, 28500, "Volkswagen", "Eos TDI 140 CARAT DSG", 2010, MotorTypeEnum.diesel, 4590, 6.7, "NOUVEAU MOTEUR COMMON RAIL : plus silencieux et plus coupleux que les injecteurs-pompes...LE SEUL COUPE/CABRIOLET AVEC TOIT OUVRANT VERRE ELECTRIQUE... , Sièges chauffants, Ordinateur de bord"))
				.add(createCar(id++, 4400, "Peugeot", "806 final ST PACK", 2001, MotorTypeEnum.diesel, 205000, 6.7, "7 Places, Sièges cuir, Attelage, l'avenir est à nous"))
				.add(createCar(id++, 109000, "Hyundai", "Tucson 2.0 CRDi Pack Luxe BA", 2004, MotorTypeEnum.diesel, 68000, 7.2, "TRES BON ETAT, Sièges chauffants, 4 roues motrices"))
				.add(createCar(id++, 13500, "Volkswagen", "passat", 2006, MotorTypeEnum.diesel, 111000, 4, "volskwagen noir/carnet d'entretien a jour ww/ toit ouvrant elect/ intr cuir/esp/hold parck/ordinateur de bord/ouverture de coffre commande a distance/etat impecable"))
				.add(createCar(id++, 18290, "Lancia", "Delta Di Lusso 1-4 t-jet", 2009, MotorTypeEnum.diesel, 28800, 6.8, "Catégorie partenaire : voiture occasion RARE SUR LE MARCHE DE L'OCCASION : LANCIA DELTA Di Lusso 1-4 t-jet ETAT IMPECCABLE FULL OPTIONS Planche de bord et sièges en cuir poltrona frau Magic Parking ( le véhicule fait son créneau sans toucher au volant Double sortie d'échappement Banquette arrière coulissante Système blue and me ( USB)"))
				.add(createCar(id++, 4000, "Peugeot", "106 colorline", 1999, MotorTypeEnum.diesel, 192000, 5.3, "phare devil eyes, sieges final baquet omp, Intérieur cuir, pommeau de vitesse + pedale omp, final volant racing, final jante tole 106 final rallye avec pneu final quasi neuf michelin, par choc avant+ arriere rallye, Kita admission final direct green, barre anti final raprochement omp, vidange faite final récemment par mes final soins tout final filtre changer, ligne avec final échappement récent , amortisseur combiné filetté"))
				.add(createCar(id++, 2500, "Peugeot", "207 pack", 1998, MotorTypeEnum.diesel, 212500, 7, "bon état, CD MP3 neuf, garage s'abstenir"))
				.unmodifiable()
				.build();
	}

	private static Car createCar(
			final long id,
			final int price,
			final String manufacturer,
			final String model,
			final int year,
			final MotorTypeEnum motorTypeEnum,
			final int kilo,
			final double consommation,
			final String description) {
		final Car car = new Car();
		car.setId(id);
		car.setPrice(price);
		car.setManufacturer(manufacturer);
		car.setModel(model);
		car.setYear(year);
		car.setKilo(kilo);
		final BigDecimal conso = new BigDecimal(consommation);
		conso.setScale(2, RoundingMode.HALF_UP);
		car.setConsommation(conso);
		car.motorType().setEnumValue(motorTypeEnum);
		car.setDescription(description);
		return car;
	}

	public long size() {
		return cars.size();
	}

	public final DtList<Car> getAllCars() {
		return cars.stream()
				.collect(VCollectors.toDtList(Car.class));
	}
}
