package io.anontech.vizivault.rules;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class AttributeRule extends RuleConstraint {

  public static enum AttributeListOperator {
    @SerializedName("any")
    ANY,
    
    @SerializedName("none")
    NONE;
  }

  private AttributeListOperator operator;
  private List<String> attributes;
  public AttributeRule(){
    super("attribute");
    attributes = new ArrayList<>();
  }

  public AttributeRule(List<String> attributes, AttributeListOperator operator) {
    super("attribute");
    this.attributes = attributes;
    this.operator = operator;
  }
}
