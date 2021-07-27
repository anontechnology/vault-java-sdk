package io.anontech.vizivault.rules;

import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class UserConstraint extends RuleConstraint {
  public static enum UserValuePredicate {
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

  public UserConstraint(){
    super("user");
  }

  public UserConstraint(String attribute, UserValuePredicate predicate, String value) {
    super("user");
    this.attribute = attribute;
    this.value = value;
    this.predicate = predicate;
  }

  private String attribute;
  private String value;
  private UserValuePredicate predicate;
}
