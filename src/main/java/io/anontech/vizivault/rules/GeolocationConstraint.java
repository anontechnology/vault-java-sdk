package io.anontech.vizivault.rules;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class GeolocationConstraint extends RuleConstraint {

  public static enum GeolocationListOperator {
    @SerializedName("any")
    ANY,
    
    @SerializedName("none")
    NONE
  }

  private GeolocationListOperator operator;
  private List<String> countries;
  private List<String> subdivisions;
  public GeolocationConstraint(){
    super("geolocation");
    countries = new ArrayList<>();
    subdivisions = new ArrayList<>();
  }
  
  public GeolocationConstraint(List<String> countries, List<String> subdivisions, GeolocationListOperator operator) {
    super("geolocation");
    this.countries = countries;
    this.subdivisions = subdivisions;
    this.operator = operator;
  }
}
