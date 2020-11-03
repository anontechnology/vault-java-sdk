package io.anontech.vizivault;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import io.anontech.vizivault.schema.InvalidSchemaException;
import io.anontech.vizivault.schema.PrimitiveSchema;
import io.anontech.vizivault.schema.SchemaIgnore;
import io.anontech.vizivault.schema.SchemaOverride;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AttributeDefinitionUnitTest {

  public static class ClassForTestingSchemaGeneration {
    @SchemaIgnore
    public String transientString;

    @SchemaOverride(PrimitiveSchema.FILE)
    public String veryLongString;

    public int exampleInt;
    public String exampleString;

    public String[] exampleStringArray;
    public List<String> exampleStringList;

    public int[] exampleIntArray;
    public List<Integer> exampleIntList;

    public static class InnerClass {
      public String exampleString;
      public int[] exampleIntArray;
    }

    public InnerClass nested;

    public List<InnerClass> nestedList;
  }
  
  @Test
  public void testAttributeSchemaFromClass() throws Exception {
    FileInputStream expectedSchemaFile = new FileInputStream(new File("src" + File.separator + "test" + File.separator + "resources" + File.separator + "expectedSchema.json"));
    String expectedSchema = new String(expectedSchemaFile.readAllBytes());
    expectedSchemaFile.close();

    AttributeDefinition def = new AttributeDefinition();
    def.schemaFromClass(ClassForTestingSchemaGeneration.class);

    JSONAssert.assertEquals(expectedSchema, def.getSchema().toString(), false);
  }

  @Test
  public void testAttributeSchemaFromJson() throws Exception {
    Gson gson = new Gson();

    // test a valid schema
    AttributeDefinition def = new AttributeDefinition();

    JsonObject validJson = gson.fromJson("{\"A\": \"string\", \"B\": {\"A\": \"int\", \"[C]\": \"string\"}}", JsonObject.class);
    def.setSchema(validJson);
    assertEquals(validJson, def.getSchema());

    assertThrows(InvalidSchemaException.class, () -> def.setSchema(gson.fromJson("{\"not alphanumeric?!\": \"string\"}", JsonObject.class)));
    assertThrows(InvalidSchemaException.class, () -> def.setSchema(gson.fromJson("{\"array\": [\"string\"]}", JsonObject.class)));
    assertThrows(InvalidSchemaException.class, () -> def.setSchema(gson.fromJson("{\"null\": null}", JsonObject.class)));
    assertThrows(InvalidSchemaException.class, () -> def.setSchema(gson.fromJson("{\"nestedInvalid\": {\"???\": [\"string\"]}}", JsonObject.class)));
  }
}
