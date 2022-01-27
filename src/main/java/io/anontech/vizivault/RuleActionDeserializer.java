package io.anontech.vizivault;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import io.anontech.vizivault.rules.*;

class RuleActionDeserializer implements JsonDeserializer<RuleAction>, JsonSerializer<RuleAction> {

  private static final Map<String, Class<? extends RuleAction>> classes = Map.of(
    "tag", TagAction.class,
    "regulation", RegulationAction.class,
    "alert", AlertAction.class,
    "block", BlockAction.class,
    "entityTag", EntityTagAction.class,
    "legalHold", LegalHoldAction.class,
    "retention", RetentionPolicyAction.class
  );

  @Override
  public RuleAction deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    String ruleType = json.getAsJsonObject().get("type").getAsString();
    Class<? extends RuleAction> actionClass = classes.get(ruleType);
    if (actionClass == null) {
      throw new JsonParseException(String.format("Unknown rule action type %s", ruleType));
    } else {
      return context.deserialize(json, actionClass);
    }
  }

  @Override
  public JsonElement serialize(RuleAction src, Type typeOfSrc, JsonSerializationContext context) {
    return context.serialize(src, src.getClass());
  }
  
}
