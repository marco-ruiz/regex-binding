# Regular Expression Binding

This framework allows to bind structured data from text (strings) onto data model trees. Also, it allows to
construct structured text (strings) from data model trees. The only requirement is to annotate the data model with the
structure of the text that is going to be used to bind (marshall/unmarshall) data to and from.

## Overview

Currently the framework supports populating the following types of fields in a model:

1. `Strings`.
2. Nested objects and its fields (recursively).
3. List of `Strings`.
4. List of nested objects and their fields (recursively).
5. Any combination of these scenarios recursively.

To instruct the framework how to populate a particular class one must use the `@REXConfig4Class` annotation (at the
class level) specifying the sequence of fields in the order that they will appear in the structure text.

```java

	@REXConfig4Class(rexPieces={"firstField", "secondField", "thirdField"})
	class MyClass {
		private String firstField;
		private OtherAnnotatedModelClass secondField;
		private List<YetAnotherModelAnnotatedClass> thirdField;
	}

```

Each of the nested objects that will be part of the **data-binding process**, need to be similarly annotated (recursion).
Also, each of the fields in this model class, that are referenced in the `@REXConfig4Class.rexPieces` argument
need to be annotated with the instructions on how to bind them to the text; for example:

```java

	@REXConfig4String(optional=[BOOLEAN], pattern=@REXConfig4Field(suffix="[REGEX]"))
	private String firstField;

	@REXConfig4String(pattern=@REXConfig4Field(value="[REGEX]"), min=[MIN_REPETITIONS], max=[MAX_REPETITIONS])
	private OtherAnnotatedModelClass secondField;

	@REXConfig4ListElement(pattern=@REXConfig4Field(value="[REGEX]", prefix="[REGEX]"))
	private List<YetAnotherModelAnnotatedClass> thirdField;

```

Then the rest of the data model tree needs to be similarly annotated (classes `OtherAnnotatedModelClass` and
`YetAnotherModelAnnotatedClass`). Finally, the model can be submitted to the framework for population through
different API utilities. Here are two of them:

```java

	// Method 1: Populate the data model object passed as argument and return it.
	MyClass obj1 = new MyClass();
	obj1 = REXParser.populateModel(obj, "[STRUCTURED_TEXT]");

	// Method 2: Create an empty data model given its Class, populate it and return it.
	MyClass obj2 = REXParser.createModel(MyClass.class, "[STRUCTURED_TEXT]");

```


## Tutorial

We will build a data model to hold the information of a well structured `URI` (structured text); annotated appropriately
to be populated by the framework with the values of any particular text representing a URI. Let's start with a trimmed
down version of a URI: `[scheme]://[host]:[port]/[path]#[fragment]`

To support this structure we would need a class such as the following:

```java

	@REXConfig4Class(rexPieces={"scheme", "host", "port", "path", "fragment"})
	class MyURI {

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

```

Given this class the framework will populate any text conforming to the structure `[scheme]://[host]:[port]/[path]#[fragment]`
onto this model. In order to do so, one must call one of the many utilities available in the `REXParser` class.
We can make the following invocation for example:

```java

	MyURI uri = REXParser.createModel(MyURI.class, "https://my.host.com:8080/path1/path2/path3#topFragment");

```

... and retrieve a `MyURI` object with its fields populated according to the rules set in the annotations and the data
(from the text passed as an argument) that matches those rules.
The framework will populate the values using setter methods if present, otherwise will go straight to the field and
do the data population directly onto the field; all of this using reflection.

This is the simplest of cases one can use with this framework since it only populates String fields.
Let's augment our `MyURI` class to explore the rest of the features supported.

### Nested Objects

Let's expand the structure of our uri text to include login information to `[scheme]://[LOGIN]@[host]:[port]/[path]#[fragment]`;
where `LOGIN` itself has the structure `[user]:[password]`. Then we would need a class to hold these values such as this:

```java

	@REXConfig4Class(rexPieces={"user", ":", "password"})
	class MyURILogin {

		@REXConfig4String(pattern=@REXConfig4Field(value="[^:]*"))
		private String user;

		@REXConfig4String(pattern=@REXConfig4Field(value="[^@]*"))
		private String password;
	}

```

Then we need to augment our original `MyURI` class with a new member object `MyURIAuth` (to hold this additional information)
and its respective annotations (to instruct the framework about its **binding rules**):

*Before*
```java

	@REXConfig4Class(rexPieces={"scheme", "host", "port", "path", "fragment"})
	class MyURI {
		// ... original members here ...
	}

```

*After*
```java

	@REXConfig4Class(rexPieces={"scheme", "login", "host", "port", "path", "fragment"})
	class MyURI {
		// ... original members here ...

		@REXConfig4String(optional=true, pattern=@REXConfig4Field(suffix="@"))
		private MyURILogin loginInfo;
	}

```

### List of Strings

Let's transform out path from a single `String` into a list of strings holding each of the *path branches*:

*Before*
```java

	@REXConfig4Class(rexPieces={"scheme", "host", "port", "path", "fragment"})
	class MyURI {
		// ... other members ...

		@REXConfig4String(pattern=@REXConfig4Field(value="[^#]*", suffix="#"))
		private String path;

		// ... other members ...
	}

```

*After*
```java

	@REXConfig4Class(rexPieces={"scheme", "host", "port", "path", "fragment"})
	class MyURI {
		// ... other members ...

		@REXConfig4ListElement(pattern=@REXConfig4Field(value="[^/?]*", suffix="(/|\\?)"), min=0)
		private List<String> path;

		// ... other members ...
	}

```

Since we only changed our minds how we wanted to hold the same data, we just needed to change our model and
its **binding rules** to instruct the framework of the new preferences.

### List of nested objects

Let's add parameters to the structure of the URI: `[scheme]://[LOGIN]@[host]:[port]/[path]?[PARAMETERS]#[fragment]`.
In this structure `PARAMETERS` will be a list of nested objects, each with the structure `[paramName]:[paramValue]`
each delimited with a `&`. Then we would need a class to hold these values such as this:

```java

	@REXConfig4Class(rexPieces={"name", "value"})
	class MyURIParameter {

		@REXConfig4String(pattern=@REXConfig4Field(value="[^=]*", suffix="="))
		private String name;

		@REXConfig4String(pattern=@REXConfig4Field(value="[^&#]*", suffix="(&|#|$)"))
		private String value;
	}

```

Then we need to augment out `MyURI` class with this extra field:

*Before*
```java

	@REXConfig4Class(rexPieces={"scheme", "host", "port", "path", "fragment"})
	class MyURI {
		// ... other members ...
	}

```

*After*
```java

	@REXConfig4Class(rexPieces={"scheme", "host", "port", "path", "params", "fragment"})
	class MyURI {
		// ... other members ...

		@REXConfig4ListElement(pattern=@REXConfig4Field(value="[^&]*"), min=0)
		private List<MyURIParameter> params;
	}

```

