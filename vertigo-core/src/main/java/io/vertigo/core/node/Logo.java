/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2020, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.node;

import java.io.PrintStream;

/**
 * Displays logo.
 *
 * @author pchretien
 */
final class Logo {
	private Logo() {
		//constructor is protected
	}

	public static void main(final String[] args) {
		printCredits(System.out);
	}

	/**
	 * Displays logo in console.
	 * @param out Out
	 */
	static void printCredits(final PrintStream out) {
		out.println();
		out.println(" ___      ___                                     \n" +
				" \\%%\\    /%%/       _   _                 _       \n" +
				"  \\%%\\  /~~/__ _ __| |_(_) __ _  ___     (_) ___  \n" +
				"   \\%~\\/~~/ _ \\ '__| __| |/ _` |/ _ \\    | |/ _ \\ \n" +
				"    \\~%~~/  __/ |  | |_| | (_| | (_) |   | | (_) |\n" +
				"     \\%%/ \\___|_|   \\__|_|\\__, |\\___/ (@)|_|\\___/ \n" +
				"      \\/                   __/ |                  \n" +
				"          v3.0.0 - 2020   |___/  \n");
	}
}
