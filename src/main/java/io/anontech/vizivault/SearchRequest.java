package io.anontech.vizivault;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class SearchRequest {
  private List<String> regulations;

  @Data
  @AllArgsConstructor
  public class ValueSearchRequest {
    private String attribute;
    private String value;
  }

  private List<ValueSearchRequest> values;
  private List<String> attributes;

  private String sensitivity;

  private String userId;

  private String country;
  private String subdivision;
  private String city;

  private Date minCreatedDate;
  private Date maxCreatedDate;
  private Date minModifiedDate;
  private Date maxModifiedDate;

  public SearchRequest(){
    regulations = new ArrayList<>();
    values = new ArrayList<>();
    attributes = new ArrayList<>();
  }

  public SearchRequest(String attribute, String value) {
    this();
    addValueQuery(attribute, value);
  }

  public void addValueQuery(String attribute, String value) {
    values.add(new ValueSearchRequest(attribute, value));
  }
}
