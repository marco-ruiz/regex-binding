# Regular Expression Binding

This project is a framework that allows to bind structured data from text (strings) onto data model trees; and equally construct structured text (strings) from data model trees. The only requirement is to annotate the data model with the structure of the text is going to translate from and to. For example:


```java


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


```




```java

	[MODEL_CLASS] model = REXParser.createModel([MODEL_CLASS].class, "[STRUCTURED_DATA]");

```


