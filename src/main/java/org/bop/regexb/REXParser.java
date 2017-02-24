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

package org.bop.regexb;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import org.bop.regexb.inspect.REXField;
import org.bop.regexb.inspect.REXInspector4Class;
import org.bop.regexb.inspect.REXInspector4String;

/**
 * @author Marco Ruiz
 * @since Jul 23, 2008
 */
public class REXParser<MODEL_T> {

	//============
	// API UTILS
	//============
	public static <MODEL_TYPE> MODEL_TYPE createModel(Class<MODEL_TYPE> modelClass, String src) throws REXException {
		return new REXParser<>(modelClass).populateModel(src);
	}

	public static <MODEL_TYPE> MODEL_TYPE createModel(Class<MODEL_TYPE> modelClass, String src, boolean preCleanUpSpaces) throws REXException {
		return new REXParser<>(modelClass).populateModel(src, preCleanUpSpaces);
	}

	public static <MODEL_TYPE> MODEL_TYPE populateModel(MODEL_TYPE model, String src) throws REXException {
		return new REXParser<>(model).populateModel(src);
	}

	public static <MODEL_TYPE> MODEL_TYPE populateModel(MODEL_TYPE model, String src, boolean preCleanUpSpaces) throws REXException {
		return new REXParser<>(model).populateModel(src, preCleanUpSpaces);
	}

	//===============================
	// CREATE FIELD VALUE UTILITIES
	//===============================
	private static <MODEL_TYPE> Object createFieldValue(String src, Field field) throws Exception {
		REXInspector4String inspector = new REXInspector4String(field);
		String matchSource = REXUtils.findSequentialMatches(src, inspector.getPatterns()).get(1);
		return (matchSource == null) ? null :
			inspector.isListField() ? buildListProperty(matchSource, field, inspector) : buildSimpleProperty(matchSource, field.getType());
	}

	private static <ELE_TYPE> List<ELE_TYPE> buildListProperty(String matchSource, Field field, REXInspector4String config) throws REXException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Class<ELE_TYPE> eleClass = (Class<ELE_TYPE>) ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
	    List<ELE_TYPE> result = new ArrayList<ELE_TYPE>();
	    if (!matchSource.equals("")) {
		    List<List<String>> fieldSources = REXUtils.findRepeats(matchSource, config.getEleInspector().getPatterns(), true);
		    for (List<String> fldSource : fieldSources)
		    	result.add(buildSimpleProperty(fldSource.get(1), eleClass));
	    }
	    return result;
    }

	private static <ELE_T> ELE_T buildSimpleProperty(String matchSource, Class<ELE_T> fieldClass) throws REXException {
	    return (fieldClass.equals(String.class)) ? (ELE_T) matchSource : new REXParser<ELE_T>(fieldClass).populateModel(matchSource, false);
    }

	//=======
	// CLASS
	//=======
	private MODEL_T model;

	public REXParser(MODEL_T model) {
		this.model = model;
	}

	public REXParser(Class<MODEL_T> modelClass) throws REXException {
		try {
			this.model = modelClass.newInstance();
        } catch (Exception e) {
			throw new REXException("Could not create an instance of class '" + modelClass + "'", e);
        }
	}

	public MODEL_T populateModel(String src) throws REXException {
		return populateModel(src, false);
	}

	public MODEL_T populateModel(String src, boolean preCleanUpSpaces) throws REXException {
		if (preCleanUpSpaces) src = src.replaceAll("\\s{1,}", " ").trim();
		try {
			List<REXField> patternFields = REXInspector4Class.getConfig(model.getClass()).getPatternFields();
			String[] patternArray = patternFields.stream().map(REXField::getPattern).toArray(String[]::new);
			List<String> matches = REXUtils.findSequentialMatches(src, patternArray);

			for (int idx = 0; idx < patternFields.size(); idx++) {
				REXField patField = patternFields.get(idx);
				for (Field field : patField.getFields()) {
					Object fieldValue = createFieldValue(matches.get(idx), field);
					if (fieldValue != null) {
						setProperty(field, fieldValue);
						break;
					}
				}
	        }

			return model;
		} catch (Exception e) {
			throw new REXException("REX parser encountered a problem while creating a '" + model.getClass() + "' using source '" + src + "'", e);
		}
	}

	private void setProperty(Field field, Object fieldValue) throws IllegalArgumentException, IllegalAccessException {
	    // Introspect setter
		String setterName = "set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
		try {
	        Method setter = field.getDeclaringClass().getDeclaredMethod(setterName, new Class[]{field.getType()});
	        setter.invoke(model, new Object[]{fieldValue});
		} catch (Exception e) {
	        field.setAccessible(true);
	        field.set(model, fieldValue);
		}
    }
}

