package io.anontech.vizivault;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.Setter;

public class DataSubject {

  private Map<String, Attribute> attributes;
  private Map<String, List<Attribute>> repeatedAttributes;

  private Set<Attribute> changedAttributes;
  private Set<String> deletedAttributes;

  @Getter
  @Setter
  private String id;

  @Getter
  @Setter
  private List<String> tags;

  @Getter
  private Date created;

  @Getter
  private Date updated;

  DataSubject() {
    changedAttributes = new HashSet<>();
    attributes = new HashMap<>();
    repeatedAttributes = new HashMap<>();
    deletedAttributes = new HashSet<>();
    tags = new ArrayList<>();
  }

  public DataSubject(String id) {
    this();
    this.id = id;
  }

  Set<Attribute> getChangedAttributes() {
    return changedAttributes;
  }

  Set<String> getDeletedAttributes() {
    return deletedAttributes;
  }

  public void addAttribute(String attributeKey, Object value) {
    Attribute attribute = new Attribute();
    attribute.setAttribute(attributeKey);
    attribute.setValue(value);

    addAttribute(attribute);
  }

  public void addAttribute(Attribute attribute) {
    addAttributeWithoutPendingChange(attribute);
    changedAttributes.add(attribute);
  }

  void addAttributeWithoutPendingChange(Attribute attribute) {
    String attributeKey = attribute.getAttribute();
    if(repeatedAttributes.containsKey(attributeKey)){
      repeatedAttributes.get(attributeKey).add(attribute);
    } else if(attributes.containsKey(attributeKey)) {
      List<Attribute> repeatableValues = new ArrayList<>();
      repeatableValues.add(attributes.get(attributeKey));
      repeatableValues.add(attribute);
      attributes.remove(attributeKey);
      repeatedAttributes.put(attributeKey, repeatableValues);
    } else {
      attributes.put(attributeKey, attribute);
    }
  }

  public Attribute getAttribute(String attributeKey) {
    if(repeatedAttributes.containsKey(attributeKey)) {
      if(repeatedAttributes.get(attributeKey).size() == 1) {
        return repeatedAttributes.get(attributeKey).get(0);
      } else {
        throw new RuntimeException("Attribute has multiple values"); // TODO better exception
      }
    }
    return attributes.get(attributeKey);
  }

  public List<Attribute> getAttributes(String attributeKey) {
    if(attributes.containsKey(attributeKey)) {
      return List.of(attributes.get(attributeKey));
    } else {
      return repeatedAttributes.getOrDefault(attributeKey, List.of());
    }
  }

  public List<Attribute> getAttributes() {
    return Stream.concat(attributes.values().stream(), repeatedAttributes.values().stream().flatMap(List<Attribute>::stream)).collect(Collectors.toList());
  }

  public void clearAttribute(String attributeKey) {
    attributes.remove(attributeKey);
    repeatedAttributes.remove(attributeKey);
    deletedAttributes.add(attributeKey);
  }
}
