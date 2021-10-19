package io.anontech.vizivault;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import io.anontech.vizivault.rules.*;

class RuleActionDeserializer implements JsonDeserializer<RuleAction>, JsonSerializer<RuleAction> {

  @Override
  public RuleAction deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    String ruleType = json.getAsJsonObject().get("type").getAsString();

    switch (ruleType) {
      case "tag":
        return context.deserialize(json, TagAction.class);
      case "regulation":
        return context.deserialize(json, RegulationAction.class);
      case "alert":
        return context.deserialize(json, AlertAction.class);
      case "block":
        return context.deserialize(json, BlockAction.class);
      default:
        throw new JsonParseException(String.format("Unknown rule action type %s", ruleType));
    }
  }

  @Override
  public JsonElement serialize(RuleAction src, Type typeOfSrc, JsonSerializationContext context) {
    return context.serialize(src, src.getClass());
  }
  
}
