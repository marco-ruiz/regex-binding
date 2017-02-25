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

package org.bop.regexb.inspect;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.bop.regexb.inspect.config.REXConfig4Field;

/**
 * @author Marco Ruiz
 * @since Feb 24, 2017
 */
public abstract class BaseREXInspector {

	private String pattern;
	private String[] patterns;
	private String fullPattern;

	public void init(REXConfig4Field rexConfig, String pattern, String cardinality) {
		this.pattern = pattern;
		this.patterns = (rexConfig != null) ?
				new String[]{ rexConfig.prefix(), pattern, rexConfig.suffix() } :
				new String[]{ "", pattern, "" };
		this.fullPattern = "(" + Arrays.stream(patterns).collect(Collectors.joining()) + ")" + cardinality;
	}

	public String getPattern() {
    	return pattern;
    }

	public String getFullPattern() {
		return fullPattern;
    }

	public String[] getPatterns() {
		return patterns;
    }
}
