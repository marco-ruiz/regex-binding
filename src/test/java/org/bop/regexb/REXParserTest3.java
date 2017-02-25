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

import org.bop.regexb.inspect.REXInspector4Class;
import org.bop.regexb.inspect.config.REXConfig4Class;
import org.bop.regexb.inspect.config.REXConfig4Field;
import org.bop.regexb.inspect.config.REXConfig4String;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Marco Ruiz
 * @since Feb 22, 2017
 */
public class REXParserTest3 {

	private String text;
	private MySimpleURI model;

	@Before
	public void setUp() throws Exception {
		text = "https://www.the.host.com:80/path1/path2/path3#top";
		model = new REXParser<MySimpleURI>(MySimpleURI.class).populateModel(text);
	}

	@Test
	public void testSimpleURI() throws REXException {
		System.out.println(new ModelToString(model));
		System.out.println(REXInspector4Class.getConfig(model.getClass()).getPattern());
	}
}


//=========
// MODELS
//=========

@REXConfig4Class(rexPieces={"scheme", "host", "port", "path", "fragment"})
class MySimpleURI {

	@REXConfig4String(pattern=@REXConfig4Field(value="[^:]*", suffix="://"))
	private String scheme;

	@REXConfig4String(pattern=@REXConfig4Field(value="[^:]*", suffix=":"))
	private String host;

	@REXConfig4String(pattern=@REXConfig4Field(value="[^/]*", suffix="/"))
	private String port;

	@REXConfig4String(pattern=@REXConfig4Field(value="[^#]*", suffix="#"))
	private String path;

	@REXConfig4String
	private String fragment;
}


