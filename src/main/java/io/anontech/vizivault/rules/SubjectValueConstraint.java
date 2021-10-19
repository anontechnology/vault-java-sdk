package io.anontech.vizivault.rules;

import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class SubjectValueConstraint extends RuleConstraint {
  public static enum SubjectValuePredicate {
    @SerializedName("eq")
    EQUALS,
    
    @SerializedName("neq")
    NOT_EQUALS,
    
    @SerializedName("lt")
    LESS_THAN,
    
    @SerializedName("gt")
    GREATER_THAN,
    
    @SerializedName("leq")
    LESS_OR_EQUAL,

    @SerializedName("geq")
    GREATER_OR_EQUAL,

    @SerializedName("before")
    BEFORE,

    @SerializedName("after")
    AFTER;
  }

  public SubjectValueConstraint(){
    super("user");
  }

  public SubjectValueConstraint(String attribute, SubjectValuePredicate predicate, String value) {
    super("user");
    this.attribute = attribute;
    this.value = value;
    this.predicate = predicate;
  }

  private String attribute;
  private String value;
  private SubjectValuePredicate predicate;
}
