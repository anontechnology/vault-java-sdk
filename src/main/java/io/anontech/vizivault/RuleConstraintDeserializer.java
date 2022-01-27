package io.anontech.vizivault;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import io.anontech.vizivault.rules.*;

class RuleConstraintDeserializer implements JsonDeserializer<RuleConstraint>, JsonSerializer<RuleConstraint> {

  @Override
  public RuleConstraint deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    String ruleType = json.getAsJsonObject().get("type").getAsString();

    switch (ruleType) {
      case "all":
        return context.deserialize(json, ConjunctiveConstraint.class);
      case "any":
        return context.deserialize(json, DisjunctiveConstraint.class);
      case "tag":
        return context.deserialize(json, TagConstraint.class);
      case "attribute":
        return context.deserialize(json, AttributeConstraint.class);
      case "value":
        return context.deserialize(json, ValueConstraint.class);
      case "regulation":
        return context.deserialize(json, RegulationConstraint.class);
      case "geo":
        return context.deserialize(json, GeolocationConstraint.class);
      default:
        throw new JsonParseException(String.format("Unknown rule constraint type %s", ruleType));
    }
  }

  @Override
  public JsonElement serialize(RuleConstraint src, Type typeOfSrc, JsonSerializationContext context) {
    return context.serialize(src, src.getClass());
  }
  
}
