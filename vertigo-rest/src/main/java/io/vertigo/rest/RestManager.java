package io.vertigo.rest;

import io.vertigo.kernel.Home;
import io.vertigo.kernel.component.Manager;
import io.vertigo.kernel.lang.Activeable;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.StringUtil;
import io.vertigo.rest.EndPointDefinition.Verb;
import io.vertigo.rest.RestfulService.AnonymousAccessAllowed;
import io.vertigo.rest.RestfulService.DELETE;
import io.vertigo.rest.RestfulService.GET;
import io.vertigo.rest.RestfulService.POST;
import io.vertigo.rest.RestfulService.PUT;
import io.vertigo.rest.RestfulService.PathParam;
import io.vertigo.rest.RestfulService.QueryParam;
import io.vertigo.rest.RestfulService.SessionLess;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Restfull webservice manager.
 * @author npiedeloup
 */
public class RestManager implements Manager, Activeable {

	public RestManager() {
		Home.getDefinitionSpace().register(EndPointDefinition.class);
	}

	public void start() {
		for (final String componentId : Home.getComponentSpace().keySet()) {
			final Object component = Home.getComponentSpace().resolve(componentId, Object.class);
			if (component instanceof RestfulService) {
				instrospectEndPoints(((RestfulService) component).getClass());
			}
		}
	}

	public void stop() {
		//nothing
	}

	private <C extends RestfulService> void instrospectEndPoints(final Class<C> restFullServiceClass) {
		for (final Method method : restFullServiceClass.getMethods()) {
			EndPointDefinition endPointDefinition;
			Verb verb = null;
			String path = null;
			boolean needSession = true;
			boolean needAuthentication = true;
			for (final Annotation annotation : method.getAnnotations()) {
				if (annotation instanceof GET) {
					verb = Verb.GET;
					path = ((GET) annotation).value();
				} else if (annotation instanceof POST) {
					verb = Verb.POST;
					path = ((POST) annotation).value();
				} else if (annotation instanceof PUT) {
					verb = Verb.PUT;
					path = ((PUT) annotation).value();
				} else if (annotation instanceof DELETE) {
					verb = Verb.DELETE;
					path = ((DELETE) annotation).value();
				} else if (annotation instanceof AnonymousAccessAllowed) {
					needAuthentication = false;
				} else if (annotation instanceof SessionLess) {
					needSession = false;
				}
			}
			if (verb != null) {
				Assertion.checkState(verb != null, "Verb must be specified on {0}", method.getName());
				Assertion.checkArgNotEmpty(path, "Route path must be specified on {0}", method.getName());

				endPointDefinition = new EndPointDefinition("EP_" + StringUtil.camelToConstCase(restFullServiceClass.getSimpleName()) + "_" + StringUtil.camelToConstCase(method.getName()), verb, path, method, needSession, needAuthentication);
				final Class[] paramType = method.getParameterTypes();
				final Annotation[][] parameterAnnotation = method.getParameterAnnotations();

				for (int i = 0; i < paramType.length; i++) {
					final String paramName = getParamName(parameterAnnotation[i]);
					//Assertion.checkArgNotEmpty(paramName, "Le paramName n'a pas été précisé sur {0}", method.getName());

					endPointDefinition.addParam(paramName, paramType[i]);
				}
				Home.getDefinitionSpace().put(endPointDefinition, EndPointDefinition.class);
			}
		}
	}

	private final String getParamName(final Annotation[] annotations) {
		for (final Annotation annotation : annotations) {
			if (annotation instanceof PathParam) {
				return ":path:" + ((PathParam) annotation).value();
			} else if (annotation instanceof QueryParam) {
				return ":query:" + ((QueryParam) annotation).value();
			}
			//			else if (annotation instanceof BodyParam) {
			//				return ":body:";
			//			}
		}
		return ":body:";//if no annotation : take request body
	}
}