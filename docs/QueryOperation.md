#Query Operation

##Filter

A document, query by example
Inherits attributes from the operation document

```
{
    cluster: "String",
    namespace: "database.collection",
    query: {},
    readPrefs: [],
    modifiers: [],
    projection: {}
}
```

##Modifiers
- comment type: String
- explain type: integer 1 for true
- hint type: doc, example: { "index name" : 1  }
- limit type: integer
- maxScan type: integer
- maxTimeMS type: integer
- max type: integer, example: { field: <number> }  description: requires index
- min type: integer, example: { field: <number> }  description: requires index
- natural type: +-1
- orderby type: doc. example: { field: orfer }
- returnKey type: boolean
- showDiskLoc type: boolean
- skip type: integer
- snapshot type: boolean
