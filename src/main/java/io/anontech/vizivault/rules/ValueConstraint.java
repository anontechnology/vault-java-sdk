package io.anontech.vizivault.rules;

import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class ValueConstraint extends RuleConstraint {
  public static enum ValuePredicate {
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
    AFTER,

    @SerializedName("in")
    IN,

    @SerializedName("nin")
    NOT_IN,

    @SerializedName("regex")
    REGEX_CONTAINS
  }

  public ValueConstraint(){
    super("user");
  }

  public ValueConstraint(String attribute, ValuePredicate predicate, String value) {
    super("user");
    this.attribute = attribute;
    this.value = value;
    this.predicate = predicate;
  }

  private String attribute;
  private String value;
  private ValuePredicate predicate;
}
