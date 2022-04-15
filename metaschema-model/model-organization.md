# Binding Model - original

Interface hierarchy:

- Property
  - NamedProperty
    - FieldValueProperty
    - FlagProperty
    - NamedModelProperty
      - AssemblyProperty
      - FieldProperty

Implementation hierarchy:

- AbstractProperty
  - AbstractNamedProperty
    - AbstractNamedModelProperty
      - **DefaultAssemblyProperty**
      - **DefaultFieldProperty**
    - **DefaultFlagProperty**
  - **DefaultFieldValueProperty**

# Metaschema Model - original

Interface hierarchy:

- InfoElement
  - InfoElementInstance
    - *DefinedInfoElementInstance*
      - **FlagInstance** (*Flag*)
      - *ObjectModelInstance*
    - X-ModelInstance
      - **ChoiceInstance**
      - ObjectModelInstance (*NamedInfoElement*, *DefinedInfoElementInstance*)
        - **AssemblyInstance** (*Assembly*)
        - **FieldInstance** (*Field*)
  - *NamedInfoElement*
    - *Assembly*
    - *Field*
    - *Flag*

Implementation hierarchy:

- AbstractInfoElementInstance
  - AbstractDefinedInfoElementInstance
    - **AbstractAssemblyInstance**
    - **AbstractFieldInstance**
    - **AbstractFlagInstance**
  - **AbstractChoiceInstance**
