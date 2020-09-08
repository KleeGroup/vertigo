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
package io.vertigo.core.node.component;

/**
 * An amplifier is a special core-component.
 * It's defined by a simple API (aka a java interface).
 * The "real" component is automatically from annotations that explain 
 * how to deal with their decalarations
 * 
 * Technically, the created core-component is a java proxy.
 * ----------------------------------------------------------------
 * The perfect use case of the amplifier is when you have to request a remote server, 
 * that's to say when you need a simple client. (sql, webServices, redis...)
 * 
 * The amplifier tanslates the particular request (defined in a anotation) in a java code.      
 * @author pchretien
 */
public interface Amplifier extends CoreComponent {
	//
}
