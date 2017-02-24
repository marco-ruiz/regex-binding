/*
 * Copyright 2002-2017 the original author or authors.
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
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.bop.regexb.config.REXField;
import org.bop.regexb.config.REXInspector4Class;

/**
 * @author Marco Ruiz
 * @since Feb 23, 2017
 */
public class ModelToString {

	private Object model;
	private StringMarks marks;

	public ModelToString(Object model) {
		this(model, ",");
	}

	public ModelToString(Object model, String delimiter) {
		this(model, delimiter, "[", "]");
	}

	public ModelToString(Object model, String delimiter, String prefix, String suffix) {
		this(model, delimiter, prefix, suffix, "-", "?");
	}

	public ModelToString(Object model, String delimiter, String prefix, String suffix, String missField, String missValue) {
		this(model, new StringMarks(delimiter, prefix, suffix, missField, missValue));
	}

	private ModelToString(Object model, StringMarks marks) {
		this.model = model;
		this.marks = marks;
	}

	public String toString() {
		Class<? extends Object> modelClazz = model.getClass();
		if (String.class.isAssignableFrom(modelClazz))
			return (String)model;

		if (List.class.isAssignableFrom(modelClazz))
			return ((List<?>)model).stream()
							.map(this::toModelString)
							.collect(marks.joiner);

		if ((!REXInspector4Class.isREXConfig(model) || ModelToString.class.isAssignableFrom(modelClazz)))
			return model.toString();

		// Return collection of fields
		return REXInspector4Class.getConfig(model.getClass()).getPatternFields().stream()
						.map(this::toOrString)
						.collect(marks.joiner);
	}

	private String toModelString(Object subModel) {
		return new ModelToString(subModel, marks).toString();
	}

	private String toOrString(REXField rexField) {
		List<Field> patFields = rexField.getFields();
		if (patFields == null || patFields.size() == 0) return marks.missField;
		return patFields.stream()
					.map(this::getFieldValue)
					.map(this::toModelString)
					.filter(str -> !str.equals(marks.missValue))
					.findFirst().orElse(marks.missValue);
	}

	private Object getFieldValue(Field field) {
		try {
			field.setAccessible(true);
			return field.get(model);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			return marks.missField;
		}
	}
}

class StringMarks {
	public final String delimiter, prefix, suffix, missField, missValue;
	public final Collector<CharSequence, ?, String> joiner;

	public StringMarks(String delimiter, String prefix, String suffix, String missField, String missValue) {
		this.delimiter = delimiter;
		this.prefix = prefix;
		this.suffix = suffix;
		this.missField = prefix + missField + suffix;
		this.missValue = prefix + missValue + suffix;
		this.joiner = Collectors.joining(delimiter, prefix, suffix);
	}
}



