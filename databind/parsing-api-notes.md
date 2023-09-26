# Notes on Metaschema-aware parsing API

## XML Parsing

### Deserializer

The main entry point is `DefaultXmlDeserializer.deserialize(Reader)`. This determines the root class and delegates to the `ClassBinding.readItem(PropertyCollector, Object, StartElement, XmlParsingContext)` method to parse the contents of the assembly using a `SingletonPropertyCollector` collector for the instance read.

### Assembly or Field Bound Classes

The method `AbstractClassBinding.readInternal(Object, Object, StartElement, XmlParsingContext)` is the main entry point for bound Assembly or Field classes.

This method will first read the flag properties via a call to `Property.read(Object, StartElement, XmlParsingContext)`. It will then call `AbstractClassBinding.readBody(Object, StartElement, XmlParsingContext)` which is implemented by the `AssemblyClassBinding` and `FieldClassBinding` implementations to handled the model contents.


### Assembly (DefaultAssemblyClassBinding)

The main entry points for a bound assembly class are:

- For a root assembly `DefaultAssemblyClassBinding.readRoot(XmlParsingContext)`. In this case the outer wrapper elements are read and then `AbstractClassBinding.readInternal(Object, Object, StartElement, XmlParsingContext)` is called to read the contents.
- For any child assembly `AbstractClassBinding.readItem(PropertyCollector, Object, StartElement, XmlParsingContext)`. Then `AbstractClassBinding.readInternal(Object, Object, StartElement, XmlParsingContext)` is called to read the contents.

After flags are read by `AbstractClassBinding.readInternal(Object, Object, StartElement, XmlParsingContext)`, the method `DefaultAssemblyClassBinding.readBody(Object, StartElement, XmlParsingContext)` is called from to parse the model contents.

The assembly model contents are read iteratively through calls to `Property.read(Object, StartElement, XmlParsingContext)`.

Call tree:

- DefaultAssemblyClassBinding.readRoot(XmlParsingContext)
  - AbstractClassBinding.readInternal(Object, Object, StartElement, XmlParsingContext)
    - DefaultAssemblyClassBinding.readBody(Object, StartElement, XmlParsingContext)
      - Property.read(Object, StartElement, XmlParsingContext)
- AbstractClassBinding{as DefaultAssemblyClassBinding}.readItem(PropertyCollector, Object, StartElement, XmlParsingContext)
  - AbstractClassBinding{as DefaultAssemblyClassBinding}.readInternal(Object, Object, StartElement, XmlParsingContext)
    - DefaultAssemblyClassBinding.readBody(Object, StartElement, XmlParsingContext)
      - Property.read(Object, StartElement, XmlParsingContext)

### Field (DefaultFieldClassBinding)

The main entry point for any bound field class is `AbstractClassBinding.readItem(PropertyCollector, Object, StartElement, XmlParsingContext)`. Then `AbstractClassBinding.readInternal(Object, Object, StartElement, XmlParsingContext)` is called to read the contents.

The field value is read by calling `Property.read(Object, StartElement, XmlParsingContext)`.

### Field Value Property (DefaultFieldValueProperty)

The main entry point for a field value is `DefaultFieldValueProperty.read(Object, StartElement, XmlParsingContext)`, which delegates to the type adapter to read the value using `JavaTypeAdapter.parse(XMLEventReader2)`.

Call tree:

```
- DefaultFieldValueProperty.read(Object, StartElement, XmlParsingContext)
  - JavaTypeAdapter.parse(XMLEventReader2)
```

### Flag Property (DefaultFlagProperty)

The main entry point for a flag value is `DefaultFlagProperty.read(Object, StartElement, XmlParsingContext)`, which delegates to the type adapter to read the value using `JavaTypeAdapter.parse(String)`, since the attribute value is already parsed as a string value.

Call tree:

```
- DefaultFlagProperty.read(Object, StartElement, XmlParsingContext)
  - JavaTypeAdapter.parse(String)
```

### Assembly Instance Property (DefaultAssemblyProperty)

The main entry point for an assembly instance value is `AbstractNamedModelProperty.read(Object, StartElement, XmlParsingContext)`. This method parses the grouping element if specified by the model. The parsing of the value(s) is delegated to the `ModelPropertyInfo.readValue(PropertyCollector, Object, StartElement, XmlParsingContext)` method, which is specific to the occurrence model of the property (i.e., SingletonPropertyInfo, ListPropertyInfo, MapPropertyInfo). A call to `PropertyInfo.readValue(PropertyCollector, Object, StartElement, XmlParsingContext)` then delegates to the `DefaultAssemblyProperty.readItem(PropertyCollector, Object, StartElement, XmlParsingContext)` to read each value item.

The `DefaultAssemblyProperty.readItem(PropertyCollector, Object, StartElement, XmlParsingContext)` calls `AbstractNamedModelProperty.getBindingSupplier()` to retrieve the XML value parser (XmlBindingSupplier). Parsing is then delegated to `XmlBindingSupplier.get(PropertyCollector, Object, StartElement, XmlParsingContext)`.

Call tree:

```
- AbstractNamedModelProperty{as DefaultAssemblyProperty}.read(Object, StartElement, XmlParsingContext)
  - ModelPropertyInfo.readValue(PropertyCollector, Object, StartElement, XmlParsingContext)
    - PropertyInfo.readValue(PropertyCollector, Object, StartElement, XmlParsingContext)
      - NamedModelProperty{as DefaultAssemblyProperty}.readItem(PropertyCollector, Object, StartElement, XmlParsingContext)
        - AbstractNamedModelProperty{as DefaultAssemblyProperty}.getBindingSupplier()
          - XmlBindingSupplier.get(PropertyCollector, Object, StartElement, XmlParsingContext)
```

### Field Instance Property (DefaultFieldProperty)

The main entry point for a field instance value is `AbstractNamedModelProperty.read(Object, StartElement, XmlParsingContext)`. This method parses the grouping element if specified by the model. The parsing of the value(s) is delegated to the `ModelPropertyInfo.readValue(PropertyCollector, Object, StartElement, XmlParsingContext)` method, which is specific to the occurrence model of the property (i.e., SingletonPropertyInfo, ListPropertyInfo, MapPropertyInfo). A call to `PropertyInfo.readValue(PropertyCollector, Object, StartElement, XmlParsingContext)` then delegates to the `DefaultFieldProperty.readItem(PropertyCollector, Object, StartElement, XmlParsingContext)` to read each value item.

The `DefaultAssemblyProperty.readItem(PropertyCollector, Object, StartElement, XmlParsingContext)` calls `AbstractNamedModelProperty.getBindingSupplier()` to retrieve the XML value parser (XmlBindingSupplier). Parsing is then delegated to `XmlBindingSupplier.get(PropertyCollector, Object, StartElement, XmlParsingContext)`.

Call tree:

```
- AbstractNamedModelProperty{as DefaultFieldProperty}.read(Object, StartElement, XmlParsingContext)
  - ModelPropertyInfo.readValue(PropertyCollector, Object, StartElement, XmlParsingContext)
    - PropertyInfo.readValue(PropertyCollector, Object, StartElement, XmlParsingContext)
      - NamedModelProperty{as DefaultFieldProperty}.readItem(PropertyCollector, Object, StartElement, XmlParsingContext)
        - AbstractNamedModelProperty{as DefaultFieldProperty}.getBindingSupplier()
          - XmlBindingSupplier.get(PropertyCollector, Object, StartElement, XmlParsingContext)
```

### XML instance value parsing (XmlBindingSupplier)

The XML value parser delegates value parsing by calling `XmlBindingSupplier.get(PropertyCollector, Object, StartElement, XmlParsingContext)`.

There are two types of binding suppliers:

- For bound classes, `ClassDataTypeHandler` delegates parsing to a bound field (DefaultFieldClassBinding) or assembly (DefaultAssemblyClassBinding) class by calling the `AbstractClassBinding.readItem(PropertyCollector, Object, StartElement, XmlParsingContext)` method.
- For scalar values, `JavaTypeAdapterDataTypeHandler` delegates to the type adapter to read the value using the `JavaTypeAdapter.parse(XMLEventReader2)` method.

Call tree:

```
- XmlBindingSupplier{as ClassDataTypeHandler}.get(PropertyCollector, Object, StartElement, XmlParsingContext)
  - AbstractClassBinding(as DefaultFieldClassBinding or DefaultAssemblyClassBinding}.readItem(PropertyCollector, Object, StartElement, XmlParsingContext)
- XmlBindingSupplier{as JavaTypeAdapterDataTypeHandler}.get(PropertyCollector, Object, StartElement, XmlParsingContext)
  - JavaTypeAdapter.parse(XMLEventReader2)
```
