package io.anontech.vizivault.rules;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class AttributeConstraint extends RuleConstraint {

  public static enum AttributeListOperator {
    @SerializedName("any")
    ANY,
    
    @SerializedName("none")
    NONE;
  }

  private AttributeListOperator operator;
  private List<String> attributes;
  public AttributeConstraint(){
    super("attribute");
    attributes = new ArrayList<>();
  }

  public AttributeConstraint(List<String> attributes, AttributeListOperator operator) {
    super("attribute");
    this.attributes = attributes;
    this.operator = operator;
  }
}
