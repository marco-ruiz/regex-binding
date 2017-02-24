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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.bop.regexb.inspect.config.REXConfig4Class;
import org.bop.regexb.inspect.config.REXConfig4Field;
import org.bop.regexb.inspect.config.REXConfig4ListElement;
import org.bop.regexb.inspect.config.REXConfig4String;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Marco Ruiz
 * @since Feb 22, 2017
 */
public class REXParserTest2 {

	private String text;
	private MyURI model;

	@Before
	public void setUp() throws Exception {
		text = "http://myUser:myPassw@theHost.com:9090/path1/path2/path3/path4?par1=val1&par2=val2&par3=val3#fragment";
		model = new REXParser<>(MyURI.class).populateModel(text);
	}

	@Test
	public final void testStrings() {
		assertEquals("http", model.getScheme());
		assertEquals("theHost.com", model.getHost());
		assertEquals("9090", model.getPort());
		assertEquals("fragment", model.getFragment());
	}

	@Test
	public void testNestedObject() {
		assertEquals("myUser", model.getAuth().getUser());
		assertEquals("myPassw", model.getAuth().getPassword());
	}

	@Test
	public void testListOfStrings() {
		List<String> path = model.getPath();
		assertEquals(4, path.size());
		assertEquals("path1", path.get(0));
		assertEquals("path2", path.get(1));
		assertEquals("path3", path.get(2));
		assertEquals("path4", path.get(3));
	}

	@Test
	public void testListOfNestedObjects() {
		assertEquals(3, model.getParams().size());
		testParam(0, "par1", "val1");
		testParam(1, "par2", "val2");
		testParam(2, "par3", "val3");
	}

	public final void testParam(int index, String name, String value) {
		MyURIParameter param = model.getParams().get(index);
		assertEquals(name, param.getName());
		assertEquals(value, param.getValue());
	}

	@Test
	public void testModelToString() {
		System.out.println(new ModelToString(model).toString());
		assertEquals("[http,[-],[myUser,[-],myPassw],theHost.com,9090,[-],[path1,path2,path3,path4],[-],[[par1,val1],[par2,val2],[par3,val3]],fragment]",
				new ModelToString(model).toString());
		System.out.println(new ModelToString(model.getParams()));
		assertEquals("[[par1,val1],[par2,val2],[par3,val3]]", new ModelToString(model.getParams()).toString());
		assertEquals("{myUser;{---};myPassw}", new ModelToString(model.getAuth(), ";", "{", "}", "---", "???").toString());
		assertEquals(new ModelToString("abc123").toString(), "abc123");
	}
}


//=========
// MODELS
//=========

@REXConfig4Class(rexPieces={"scheme", "://", "auth", "host", "port", "/", "path", "\\?", "params", "fragment"})
class MyURI {

	@REXConfig4String(pattern=@REXConfig4Field(value="[^:]*"))
	private String scheme;

	@REXConfig4String(optional=true, pattern=@REXConfig4Field(suffix="@"))
	private MyURIAuth auth;

	@REXConfig4String(pattern=@REXConfig4Field(value="[^/:]*"))
	private String host;

	@REXConfig4String(optional=true, pattern=@REXConfig4Field(prefix=":", value="[^/]*"))
	private String port;

	@REXConfig4ListElement(pattern=@REXConfig4Field(value="[^/?]*", suffix="(/|\\?)"), min=0)
	private List<String> path;

	@REXConfig4ListElement(pattern=@REXConfig4Field(value="[^&]*"), min=0)
	private List<MyURIParameter> params;

	@REXConfig4String(optional=true)
	private String fragment;

	public String getScheme() { return scheme; }
//	public void setScheme(String scheme) { this.scheme = scheme; }
	public MyURIAuth getAuth() { return auth; }
	public void setAuth(MyURIAuth auth) { this.auth = auth; }
	public String getHost() { return host; }
	public void setHost(String host) { this.host = host; }
	public List<String> getPath() { return path; }
	public void setPath(List<String> path) { this.path = path; }
	public List<MyURIParameter> getParams() { return params; }
	public void setParams(List<MyURIParameter> params) { this.params = params; }
	public String getPort() { return port; }
	public void setPort(String port) { this.port = port; }
	public String getFragment() { return fragment; }
	public void setFragment(String fragment) { this.fragment = fragment; }
}

@REXConfig4Class(rexPieces={"user", ":", "password"})
class MyURIAuth {

	@REXConfig4String(pattern=@REXConfig4Field(value="[^:]*"))
	private String user;

	@REXConfig4String(pattern=@REXConfig4Field(value="[^@]*"))
	private String password;

	public String getUser() { return user; }
	public void setUser(String user) { this.user = user; }
	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }
	public String toString() { return user + ":" + password; }
}

@REXConfig4Class(rexPieces={"name", "value"})
class MyURIParameter {

	@REXConfig4String(pattern=@REXConfig4Field(value="[^=]*", suffix="="))
	private String name;

	@REXConfig4String(pattern=@REXConfig4Field(value="[^&#]*", suffix="(&|#|$)"))
	private String value;

	public String getName() { return name; }
	public void setName(String var) { this.name = var; }
	public String getValue() { return value; }
	public void setValue(String value) { this.value = value; }
	public String toString() { return name + "=" + value; }
}

