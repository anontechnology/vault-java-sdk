package io.anontech.vizivault;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;

import lombok.Data;

@Data
public class Attribute {

  private static Gson gson = new Gson();
  
  private String dataPointId;

  private String userId;

  private String attribute;

  private String sensitivity;

  private Object value;
  
  private List<String> regulations;

  private List<String> tags;

  private Date createdDate;

  private Date modifiedDate;

  private boolean reportOnly;

  Attribute() {
    regulations = new ArrayList<>();
    tags = new ArrayList<>();
  }

  public Attribute(String attributeDefinitionName) {
    this();
    attribute = attributeDefinitionName;
  }

  public <T> T getValueAs(Class<T> valueClass) {
    if(valueClass.isAssignableFrom(value.getClass())){
      return valueClass.cast(value);
    }
    return gson.fromJson(gson.toJsonTree(value), valueClass);
  }
  
}
