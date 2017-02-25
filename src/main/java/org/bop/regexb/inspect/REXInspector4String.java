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
import java.util.List;

import org.bop.regexb.inspect.config.REXConfig4Field;
import org.bop.regexb.inspect.config.REXConfig4String;

/**
 * @author Marco Ruiz
 * @since Jul 26, 2008
 */
public class REXInspector4String extends BaseREXInspector {

	public static String getValueFrom(REXConfig4Field fieldConfig) {
		String result = fieldConfig.value();
//		if (result.endsWith("*")) result += "?";
		return result;
	}

	private Field field;
	private REXConfig4String cfg;
	private REXInspector4ListElement eleInspector;
	private boolean listField;

	public REXInspector4String(Field field) {
		this.field        = field;
		this.listField    = List.class.isAssignableFrom(field.getType());
		this.cfg          = field.getAnnotation(REXConfig4String.class);
		this.eleInspector = new REXInspector4ListElement(field);

		String pattern = (listField) ? eleInspector.getFullPattern() : buildPattern(field);
		String cardinality = (cfg != null && cfg.optional()) ? "?" : "";
		init(cfg != null ? cfg.pattern() : null, pattern, cardinality);
	}

	private String buildPattern(Field field) {
		return (cfg != null) ? getValueFrom(cfg.pattern()) : REXInspector4Class.getConfigPattern(field.getType());
	}

	public Field getField() {
    	return field;
    }

	public boolean isListField() {
    	return listField;
    }

	public REXInspector4ListElement getEleInspector() {
	    return eleInspector;
    }
}
