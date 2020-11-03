package io.anontech.vizivault;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import io.anontech.vizivault.schema.InvalidSchemaException;
import io.anontech.vizivault.schema.PrimitiveSchema;
import io.anontech.vizivault.schema.SchemaIgnore;
import io.anontech.vizivault.schema.SchemaOverride;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class AttributeDefinition {

  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private String key;

  private String name;

  private String hint;
  private boolean repeatable;
  private boolean indexed;
  private Date createdDate;
  private Date modifiedDate;

  private List<String> tags;

  @Setter(AccessLevel.NONE)
  private Object schema;

  public AttributeDefinition(String name) {
    setName(name);
    setSchema(PrimitiveSchema.STRING);
  }

  public void setSchema(PrimitiveSchema schema) {
    this.schema = schema;
  }

  public void setName(String name) {
    this.name = name;
    this.key = name;
  }

  private void addFieldSchema(JsonObject schemaObject, Field f) {
    Annotation[] annotations = f.getAnnotations();
    for(Annotation a : annotations) {
      if(a instanceof SchemaIgnore) {
        return;
      } else if(a instanceof SchemaOverride) {
        schemaObject.addProperty(f.getName(), ((SchemaOverride)a).value().toString());
        return;
      }
    }
    if(f.getType().isArray()){
      schemaObject.add('['+f.getName()+']', constructSchema(f.getType().getComponentType()));
    } else {
      Type fieldType = f.getGenericType();
      if(fieldType instanceof ParameterizedType && ((ParameterizedType) fieldType).getRawType().equals(List.class)) {
        Type listElementType = ((ParameterizedType) fieldType).getActualTypeArguments()[0];
        schemaObject.add('['+f.getName()+']', constructSchema(listElementType));
      } else {
        schemaObject.add(f.getName(), constructSchema(f.getType()));
      }
    }
  }

  private JsonElement constructSchema(Type type) {
    if(type.equals(String.class)) return new JsonPrimitive("string");
    else if(type.equals(Date.class)) return new JsonPrimitive("date");
    else if(type.equals(Integer.class)) return new JsonPrimitive("int"); // TODO the rest of the integer types
    else if(type.equals(Double.class) || type.equals(Float.class)) return new JsonPrimitive("float");
    else if(type.equals(Boolean.class)) return new JsonPrimitive("boolean");
    else if(!(type instanceof Class)) return new JsonPrimitive(type.getTypeName());
    
    Class<?> typeClass = (Class<?>) type;

    if(typeClass.isPrimitive()) {
      if(type.equals(float.class) || type.equals(double.class)) return new JsonPrimitive("float");
      else if(type.equals(boolean.class)) return new JsonPrimitive("boolean");
      else return new JsonPrimitive("int");
    } else if(typeClass.isEnum()) return new JsonPrimitive("string");

    JsonObject schemaObject = new JsonObject();

    while(!typeClass.equals(Object.class)) {
      for(Field f : typeClass.getDeclaredFields()){

        addFieldSchema(schemaObject, f);

      }
      typeClass = typeClass.getSuperclass();
    }
    
    return schemaObject;
  }

  public void schemaFromClass(Class<?> schemaClass) {
    this.schema = constructSchema(schemaClass);
  }

  private void validateSchema(JsonObject jsonSchema) {
    for(Entry<String, JsonElement> entry : jsonSchema.entrySet()) {
      if(!entry.getKey().matches("\\w+") && !entry.getKey().matches("\\[\\w+\\]")) throw new InvalidSchemaException("Sub-attribute key contains unusable characters: "+key);
      if(entry.getValue().isJsonArray() || entry.getValue().isJsonNull()) throw new InvalidSchemaException(String.format("Schema for sub-attribute %s must be an object or string", entry.getKey()));
      else if(entry.getValue().isJsonObject()) validateSchema(entry.getValue().getAsJsonObject());
    }
  }

  public void setSchema(JsonObject jsonSchema) {
    validateSchema(jsonSchema);
    this.schema = jsonSchema;
  }
  
}
