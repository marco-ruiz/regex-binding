/*
 * Copyright 2007-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bop.regexb.inspect;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import org.bop.regexb.inspect.config.REXConfig4ListElement;

/**
 * @author Marco Ruiz
 * @since Jul 27, 2008
 */
public class REXInspector4ListElement {

	public static <T> Class<T> getListElementClass(Field field) {
	    ParameterizedType genericSuperclass = (ParameterizedType)field.getGenericType();
	    return (Class<T>) genericSuperclass.getActualTypeArguments()[0];
    }

	private REXConfig4ListElement cfg;
	private String pattern = "";
	private String fullPattern = "";
	private String[] elePatterns;

	public REXInspector4ListElement(Field field) {
		this.cfg = field.getAnnotation(REXConfig4ListElement.class);
		if (cfg == null) return;

		Class<Object> eleClass = getListElementClass(field);
		pattern = eleClass.equals(String.class) ?
			REXInspector4String.getValueFrom(cfg.pattern()) : REXInspector4Class.getConfigPattern(eleClass);

		String maxStr = (cfg.max() < 0) ? "" : "" + cfg.max();
		fullPattern = "(" + cfg.pattern().prefix() + pattern + cfg.pattern().suffix() + "){" + cfg.min() + "," + maxStr +"}";
	}

	public String getPattern() {
    	return pattern;
    }

	public String getFullPattern() {
		return fullPattern;
    }

	public String[] getPatterns() {
		return new String[]{ cfg.pattern().prefix(), pattern, cfg.pattern().suffix() };
    }
}
