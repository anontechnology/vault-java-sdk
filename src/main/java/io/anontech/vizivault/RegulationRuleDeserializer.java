package io.anontech.vizivault;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import io.anontech.vizivault.tagging.*;

class RegulationRuleDeserializer implements JsonDeserializer<RegulationRule>, JsonSerializer<RegulationRule> {

  @Override
  public RegulationRule deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    String ruleType = json.getAsJsonObject().get("type").getAsString();

    switch (ruleType) {
      case "all":
        return context.deserialize(json, ConjunctiveRule.class);
      case "any":
        return context.deserialize(json, DisjunctiveRule.class);
      case "tag":
        return context.deserialize(json, TagRule.class);
      case "attribute":
        return context.deserialize(json, AttributeRule.class);
      case "user":
        return context.deserialize(json, UserRule.class);
      default:
        throw new JsonParseException(String.format("Unknown rule type %s", ruleType));
    }
  }

  @Override
  public JsonElement serialize(RegulationRule src, Type typeOfSrc, JsonSerializationContext context) {
    return context.serialize(src, src.getClass());
  }
  
}
