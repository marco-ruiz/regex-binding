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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Marco Ruiz
 * @since Jul 26, 2008
 */
public class REXUtils {

	public static String repeat(String expr, int min) {
		return repeat(expr, min, -1);
	}

	public static String repeat(String expr, int min, int max) {
		String maxStr = max < 0 ? "" : "" + max;
		return group(expr) + "{" + min + "," + maxStr + "}";
	}

	public static String options(String... options) {
		return group(Arrays.stream(options).collect(Collectors.joining("|")));
	}

	public static String group(String content) {
		return "(" + content + ")";
	}

	public static List<String> extractGroups(String text, String regExp, int... groupIds) {
        Matcher matcher = Pattern.compile(regExp).matcher(text);
    	return (!matcher.find()) ? new ArrayList<>() :
    		Arrays.stream(groupIds).collect(ArrayList::new, (list, id) -> list.add(matcher.group(id)), List::addAll);
	}

	public static List<List<String>> findRepeats(String text, String[] regExps, boolean trim) {
		return findRepeats(REXUtils::findSequentialMatches, text, regExps, trim);
	}

	public static List<List<String>> findRepeatsStrong(String text, String[] regExps, boolean trim) {
		return findRepeats(REXUtils::findSequentialMatchesStrong, text, regExps, trim);
	}

	public static List<List<String>> findRepeats(BiFunction<String, String[], List<String>> sequentialMatcher, String text, String[] regExps, boolean trim) {
		String wholeRegExp = Arrays.stream(regExps).collect(Collectors.joining());
		return findRepeats(text, wholeRegExp, trim).stream()
				.map(match -> sequentialMatcher.apply(match, regExps))
				.collect(Collectors.toList());
	}

	public static List<String> findRepeats(String text, String regExp, boolean trim) {
		List<String> result = new ArrayList<String>();
        Matcher matcher = Pattern.compile(regExp).matcher(text);
	    while (matcher.find()) {
	        String match = matcher.group();
	        if (match != null && !match.equals("")) result.add(trim ? match.trim() : match);
        }
		return result;
	}

	public static List<String> findSequentialMatches(String text, String... regExps) {
		List<String> result = new ArrayList<String>();
		for (String regExp : regExps) {
			Matcher matcher = Pattern.compile(regExp).matcher(text);
			String match = matcher.find() ? matcher.group() : null;
			if (match != null) text = text.substring(matcher.end());
			result.add(match);
        }
		return result;
	}

	public static List<String> findSequentialMatchesStrong(String text, String... regExps) {
		List<String> result = new ArrayList<String>();

		for (int index = 0; index < regExps.length; index++) {
			String wholeRegExp = createRegExp(index, regExps);
			Matcher matcher = Pattern.compile(wholeRegExp).matcher(text);
			if (matcher.find()) {
				result.add(matcher.group(1));
				text = text.substring(matcher.end(1));
			}
		}

		return result;
	}

	private static String createRegExp(int fromIndex, String[] regExps) {
		String result = "(" + regExps[fromIndex] + ")";
		for (fromIndex++; fromIndex < regExps.length; fromIndex++)
	        result += regExps[fromIndex];

		return result;
	}
}

