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

class RuleConstraintDeserializer implements JsonDeserializer<RuleConstraint>, JsonSerializer<RuleConstraint> {

  private static final Map<String, Class<? extends RuleConstraint>> classes = Map.of(
    "all", ConjunctiveConstraint.class,
    "any", DisjunctiveConstraint.class,
    "tag", TagConstraint.class,
    "attribute", AttributeConstraint.class,
    "value", ValueConstraint.class,
    "regulation", RegulationConstraint.class,
    "geo", GeolocationConstraint.class,
    "sentiment", ValueSentimentConstraint.class,
    "entityType", EntityTypeConstraint.class
  );

  @Override
  public RuleConstraint deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    String ruleType = json.getAsJsonObject().get("type").getAsString();
    Class<? extends RuleConstraint> constraintClass = classes.get(ruleType);
    if (constraintClass == null) {
      throw new JsonParseException(String.format("Unknown rule constraint type %s", ruleType));
    } else {
     return context.deserialize(json, constraintClass);
    }
  }

  @Override
  public JsonElement serialize(RuleConstraint src, Type typeOfSrc, JsonSerializationContext context) {
    return context.serialize(src, src.getClass());
  }
  
}
