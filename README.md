# Anontech Java ViziVault Bindings

## Project Description
AnonTech's ViziVault system is designed to make the retrieval and storage of personal and sensitive information easy. Our multi-layer encryption/decryption system will make your data secure and accessible only to memebrs of your organization on a "need-to-use" basis. Data providers, individuals, end users, and even developers can rest safe knowing that their personal data is stored securely, access is monitored, and their most personal data is kept securely, seperate from day-to-day business operations. Personal data is there when you need it most to support business operations, and disappears back into the vault when it's not needed, so that your system can be safe and secure.


## Support
Please report bugs and issues to support@anontech.io

## Requirements

### Installaion
To add the ViziVault Java client to a Maven project, add the following to your pom.xml file:
```xml
    <dependency>
      <groupId>io.anontech.vizivault</groupId>
      <artifactId>vizivault-java-client</artifactId>
      <version>1.0.0</version>
    </dependency>
```

### Authentication
You must provide an application identifier or api key for all operations, to identify you and your application to the vault for authenticaion. For data insertion, a valid encryption key is necessary. For data retrieval, a valid decryption key is necessary.

We recommend at a minimum putting your encryption and decryption key locally in a secure location, such as a local file on disk.

# Quick start

## Connecting to your vault

```java
String encryptionKey = System.getenv("ENCRYPTIONKEY");
String decryptionKey = System.getenv("DECRYPTIONKEY");
ViziVault vault = new ViziVault(url)
  .withApiKey(apiKey)
  .withEncryptionKey(encryptionKey)
  .withDecryptionKey(decryptionKey);
```

## Attributes

The ViziVault ecosystem organizes your data using the concept of [attributes](https://docs.anontech.io/glossary/attribute/). Every data point consists of three main components: a data subject id, which represents who the data is about; a value, which is some piece of information about the data subject; and an attribute, which expresses the relationship between the data subject and the value. For example, in an online retail application, there would be an attribute for shipping addresses, an attribute for billing addresses, an attribute for credit card information, and so on.

### Adding an attribute to a data subject

Attributes are stored as `key`/`value` pairs of strings. Some attributes are repeatable, such that multiple values can be stored for the same data subject; others are not repeatable, such that adding a new value to a data subject will overwrite any previous values. You can control whether an attribute is repeatable by modifying the associated [attribute definition](/glossary/attribute-definition).

```java
// Adding an attribute to a newly-created data subject
DataSubject subject = new DataSubject("exampleUser");
subject.addAttribute(FIRST_NAME, "Jane");
vault.save(subject);

// Adding an attribute to a data subject retrieved from the vault
DataSubject subject = vault.findByDataSubject("Client6789");
subject.addAttribute("FULL_ADDRESS", "1 Hacker Way, Beverly Hills, CA 90210");
vault.save(subject);

// Adding an attribute with additional metadata to a data subject
Attribute attribute = new Attribute("LAST_NAME");
attribute.setTags(List.of("ExampleTag"));
attribute.setValue("Smith");
subject.addAttribute(attribute);
vault.save(subject);
```

### Retrieving attributes of a data subject

Once a data subject object has been retrieved from the vault, it is possible to inspect some or all of its attributes.

```java
// Retrieving all attributes for a data subject
DataSubject subject = vault.findByDataSubject("User1234");
List<Attribute> subjectAttributes = subject.getAttributes();

// Retrieving a specific non-repeatable attribute for a data subject
Attribute attribute = subject.getAttribute("FIRST_NAME");

// Retrieving multiple values for a repeatable attribute
List<Attribute> attributes = subject.getAttributes("SHIPPING_ADDRESS");
```

### Searching

To search a vault for [attributes](https://docs.anontech.io/glossary/attribute/) , pass in a SearchRequest. A list of matching attributes will be returned. For more information, read about [ViziVault Search](https://docs.anontech.io/tutorials/search/).

```java
int pageIndex = 0;
int maxCount = 25;
List<Attribute> attributes = vault.search(new SearchRequest("LAST_NAME", "Doe"), pageIndex, maxCount);
```

### Deleting data subject attributes
```java
// Purging all data subject attributes
vault.purge("User1234");

// Removing specific attribute
DataSubject subject = vault.findByDataSubject("User1234");
subject.clearAttribute("LAST_NAME");
vault.save(subject);
```

## Attribute definitions

[Attribute definitions](https://docs.anontech.io/glossary/attribute-definition/) define an object that contains all relevant metadata for attributes with a given `key`. This is how tags and regulations become associated with attributes, and how the [schema](https://docs.anontech.io/tutorials/attribute-schemas) detailing the expected structure of the attribute's values is specified. Display names and hints can also be added to the attribute definition for ease of use and readability.

### Storing an attribute definition in the Vault

To store an attribute definition, create an AttributeDefinition object and save it to the Vault. The following code details the various properties of the AttributeDefinition object.

```java
AttributeDefinition attributeDef = new AttributeDefinition();
attributeDef.setName("Billing Address");
attributeDef.setTags(List.of("geographic_location", "financial"));
attributeDef.setHint("{ line_one: \"1 Hacker Way\", line_two: \"Apt. 53\"," +
                    "city: \"Menlo Park\", state: \"California\", " +
                    "postal_code: \"94025-1456\", country: \"USA\"" +
                  "}");
attributeDef.setSchema(PrimitiveSchema.STRING); // For simple, unstsructured data
attributeDef.schemaFromClass(YourModel.class); // Alternatively, creating a schema to store objects of a class
attributeDef.setRepeatable(false);
attributeDef.setIndexed(false);

vault.storeAttributeDefinition(attributeDef);
```

### Retrieving attribute definitions from the vault

``` java
// Retrieving all attribute definitions
List<AttributeDefinition> attributeDefs = vault.getAttributeDefinitions();

// Retrieving specific attribute definition
AttributeDefinition attributeDef = vault.getAttributeDefinition("Billing Address");
```

## Tags

[Tags](https://docs.anontech.io/api/tags/) are user-defined strings that can be applied to attributes to aid in classification and searching.

### Storing a tag in the vault

To store a new tag, create a Tag object and save it to the Vault.

```java
vault.storeTag(new Tag("Financial Data"));
```

### Retrieving tags from the vault

Tags can be retrieved as a list of tag objects or as a single tag.

```java
// Retrieving all tags
List<Tag> tags = vault.getTags();

// Retrieving specific tag
Tag tag = vault.getTag("Financial Data");
```

### Deleting tags from the vault

To remove a tag, specify the tag to be removed. A boolean denoting the status of the operation will be returned.

```java
// Removing a specific tag
boolean removed = vault.deleteTag("Financial Data");
```

## Regulations

A [regulation](https://docs.anontech.io/glossary/regulation/) object represents a governmental regulation that impacts how you can use the data in your vault. Each data point can have a number of regulations associated with it, which makes it easier to ensure your use of the data is compliant. You can tag data points with regulations when entering them into the system, or specify rules that the system will use to automatically tag regulations for you.

### Storing a regulation in the vault

To store a regulation to the vault, create a Regulation object, set its key and its display name along with a URL pointing to further information about it, and call `storeRegulation`. To automatically apply regulations to incoming data, [rules](https://docs.anontech.io/tutorials/regulation-rules) can be specified.


```java
// Storing a regulation
Regulation regulation = new Regulation();
regulation.setKey("GDPR");
regulation.setName("General Data Protection Regulation");
regulation.setUrl("https://gdpr.eu/");
regulation.setRule(new SubjectValueRule("GEOGRAPHIC_REGION", SubjectValueRule.SubjectValuePredicate.EQUALS, "EU"));
vault.storeRegulation(regulation);
```

### Retrieving regulations from the vault

Regulations can be retrieved as a list of Regulation objects or by requesting a single regulation by its key.

```java
// Retrieving all regulations
List<Regulation> regulations = vault.getRegulations();

// Retrieving specific regulation
Regulation regulation = vault.getRegulation("GDPR");
```

### Deleting regulations from the vault

To remove a regulation, specify the key of the regulation to be removed. A boolean denoting the status of the operation will be returned.

```java
// Removing a specific regulation
boolean removed = vault.deleteRegulation("GDPR");
```

