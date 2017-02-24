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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.bop.regexb.config.REXConfig4Class;
import org.bop.regexb.config.REXConfig4Field;
import org.bop.regexb.config.REXConfig4ListElement;
import org.bop.regexb.config.REXConfig4String;
import org.bop.regexb.config.REXInspector4Class;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Marco Ruiz
 * @since Feb 22, 2017
 */
public class REXParserTest1 {

	private Statement stmt;

	@Before
	public void setUp() throws Exception {
		String p2el = "${myArray(1,2,3)}=[(a,b,c),(4,5,6),(7,8,9)]"
				+ " ${FILE}=f:expand(file, path, now)"
				+ " ${HIST}=[20..100||010]"
				+ " ${Array01(1,2,3)}=[(1,2,3),(4,5,6),(7,8,9)]"
				+ " ${function(x,y)}=f:myfunction(params)"
				+ " ${SAM}=[500..5000||0500]"
				+ " ${ITER}=[10..50||10]"
				+ " /home/mruiz/Slicer3-3.0.2008-01-05-linux-x86/Slicer3"
				+ " --launch /home/mruiz/Slicer3-3.0.2008-01-05-linux-x86/lib/Slicer3/Plugins/BSplineDeformableRegistration"
				+ " --iterations ${ITER} --gridSize 5 --histogrambins ${HIST} --spatialsamples ${SAM}"
				+ " --maximumDeformation 1 --default 0"
				+ " --resampledmovingfilename /export2/mruiz/slicer-test/out-${ITER}-${HIST}-${SAM}.nrrd"
				+ " in[fixed.nrrd]:http://www.na-mic.org/ViewVC/index.cgi/trunk/Libs/MRML/Testing/TestData/fixed.nrrd?view=co"
				+ " in[moving.nrrd]:http://www.na-mic.org/ViewVC/index.cgi/trunk/Libs/MRML/Testing/TestData/moving.nrrd?view=co";

		stmt = new REXParser<>(new Statement()).populateModel(p2el, false);
	}

	private VarDefinition getVar(int index) {
		return stmt.getVars().get(index);
	}

	@Test
	public final void testPatternTranslated() throws SecurityException, NoSuchFieldException {
		String expectedPattern = "(\\s*\\$\\{[a-zA-Z]\\w*(\\(([\\w]*[,]?){1,}\\))?\\}\\s*=\\s*(\\[([^\\[\\]]*)\\]|f:[a-zA-Z]\\w*\\(([^\\(\\),]*[,]?){1,}\\))\\s*){0,}.*";
		assertEquals(REXInspector4Class.getConfig(Statement.class).getPattern(), expectedPattern);
	}

	@Test
	public final void testScannedVariables() throws SecurityException, NoSuchFieldException {
		assertEquals(stmt.getVars().size(), 7);
	}

	@Test
	public final void testVarNames() throws SecurityException, NoSuchFieldException {
		assertVariableNameEquals(0, "myArray");
		assertVariableNameEquals(1, "FILE");
		assertVariableNameEquals(2, "HIST");
		assertVariableNameEquals(3, "Array01");
		assertVariableNameEquals(4, "function");
		assertVariableNameEquals(5, "SAM");
		assertVariableNameEquals(6, "ITER");
	}

	private void assertVariableNameEquals(int index, String id) {
		assertEquals(getVar(index).getDeclaration().getId(), id);
	}

	@Test
	public final void testVarDimensionsAmounts() throws SecurityException, NoSuchFieldException {
		assertEquals(3, getVarDims(0));
		assertEquals(0, getVarDims(1));
		assertEquals(0, getVarDims(2));
		assertEquals(3, getVarDims(3));
		assertEquals(2, getVarDims(4));
		assertEquals(0, getVarDims(5));
		assertEquals(0, getVarDims(6));
	}

	private int getVarDims(int index) {
		return getVar(index).getDeclaration().getDims().size();
	}

	@Test
	public final void testVarAssignmentConsistency() throws SecurityException, NoSuchFieldException {
		assertVarDefinedThroughValues(0);
		assertVarDefinedThroughValues(2);
		assertVarDefinedThroughValues(3);
		assertVarDefinedThroughValues(5);
		assertVarDefinedThroughValues(6);

		assertVarDefinedThroughFunction(1);
		assertVarDefinedThroughFunction(4);
	}

	private void assertVarDefinedThroughValues(int index) {
		assertNotNull(getVar(index).getValue());
		assertNull(getVar(index).getFunctionInvocation());
	}

	private void assertVarDefinedThroughFunction(int index) {
		assertNull(getVar(index).getValue());
		assertNotNull(getVar(index).getFunctionInvocation());
	}

/*
	@Test
	public final void testScannedVariables() throws SecurityException, NoSuchFieldException {
		assertEquals(stmt.getVars().size(), 7);
	}
*/
}


//=========
// MODELS
//=========

@REXConfig4Class(rexPieces={"vars", "template"})
class Statement {
	@REXConfig4ListElement(min=0)
	public List<VarDefinition> vars;
	@REXConfig4String(pattern=@REXConfig4Field(value=".*"))
	public String template;
	public List<VarDefinition> getVars() { return vars; }
	public void setVars(List<VarDefinition> vars) { this.vars = vars; }
	public String getTemplate() { return template; }
	public void setTemplate(String template) { this.template = template; }
	public String toString() { return vars.toString() + " onto " + template; }
}

//@REXConfig4Class(rexPieces={"\\s*", "declaration", "\\s*=\\s*", "value", "\\s*"})
@REXConfig4Class(rexPieces={"\\s*", "declaration", "\\s*=\\s*", "value|functionInvocation", "\\s*"})
class VarDefinition {
	public VarDeclaration declaration;
	public VarValue value;
	public FunctionInvocation functionInvocation;
	public VarDeclaration getDeclaration() { return declaration; }
	public void setDeclaration(VarDeclaration declaration) { this.declaration = declaration; }
	public VarValue getValue() { return value; }
	public void setValue(VarValue value) { this.value = value; }
	public String toString() { return declaration.toString() + " === " + value + " OR " + functionInvocation; }
	public FunctionInvocation getFunctionInvocation() { return functionInvocation; }
	public void setFunctionInvocation(FunctionInvocation functionInvocation) { this.functionInvocation = functionInvocation; }
}

@REXConfig4Class(rexPieces={"\\$\\{", "id", "dims", "\\}"})
class VarDeclaration {
	@REXConfig4String(pattern=@REXConfig4Field(value="[a-zA-Z]\\w*"))
	public String id;
	@REXConfig4String(optional=true, pattern=@REXConfig4Field(prefix="\\(", suffix="\\)"))
	@REXConfig4ListElement(pattern=@REXConfig4Field(value="[\\w]*", suffix="[,]?"), min=1)
	public List<String> dims;
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	public List<String> getDims() { return dims; }
	public void setDims(List<String> dims) { this.dims = dims; }
	public String toString() { return id + " -> " + dims; }
}

@REXConfig4Class(rexPieces={"value"})
class VarValue {
	@REXConfig4String(pattern=@REXConfig4Field(prefix="\\[", value="([^\\[\\]]*)", suffix="\\]"))
	public String value;
	public String getValue() { return value; }
	public void setValue(String value) { this.value = value; }
	public String toString() { return value; }
}

@REXConfig4Class(rexPieces={"f:", "functionName", "\\(", "params", "\\)"})
class FunctionInvocation {
	@REXConfig4String(pattern=@REXConfig4Field(value="[a-zA-Z]\\w*"))
	public String functionName;
	@REXConfig4String()
	@REXConfig4ListElement(pattern=@REXConfig4Field(value="[^\\(\\),]*", suffix="[,]?"), min=1)
	public List<String> params;
	public String getFunctionName() { return functionName; }
	public void setFunctionName(String functionName) { this.functionName = functionName; }
	public List<String> getParams() { return params; }
	public void setParams(List<String> params) { this.params = params; }
	public String toString() { return functionName + "<" + params + ">"; }
}

