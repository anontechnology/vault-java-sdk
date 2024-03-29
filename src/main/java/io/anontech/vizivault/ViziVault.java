package io.anontech.vizivault;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import io.anontech.vizivault.rules.RuleConstraint;
import lombok.Data;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ViziVault {

  private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

  private URL baseUrl;
  private String apiKey;
  private String encryptionKey;
  private String decryptionKey;

  private OkHttpClient httpClient;
  private Gson gson;

  public ViziVault(URL url) {
    httpClient = new OkHttpClient();
    gson = new GsonBuilder().registerTypeAdapter(RuleConstraint.class, new RuleConstraintDeserializer()).create();
    this.baseUrl = url;
  }

  /**
   * Specifies the API key to use to connect to the ViziVault API server.
   * @param apiKey the API key to use for all HTTP requests
   * @return this
   */
  public ViziVault withApiKey(String apiKey) {
    this.apiKey = String.format("Bearer %s", apiKey);
    return this;
  }

  /**
   * Specifies the encryption key that will be used to store data in the vault.
   * @param encryptionKey the RSA public encryption key to use for storage requests
   * @return this
   */
  public ViziVault withEncryptionKey(String encryptionKey) {
    this.encryptionKey = encryptionKey;
    return this;
  }

  /**
   * Specifies the decryption key that will be used to store data in the vault.
   * @param decryptionKey the RSA private decryption key to use for retrieval requests
   * @return this
   */
  public ViziVault withDecryptionKey(String decryptionKey) {
    this.decryptionKey = decryptionKey;
    return this;
  }

  private JsonElement post(String url, Object body, Headers headers) {
    try {
      Response response = httpClient.newCall(
          new Request.Builder()
            .url(new URL(baseUrl, url))
            .headers(new Headers.Builder().addAll(headers).add("Authorization", apiKey).build())
            .post(RequestBody.create(gson.toJson(body), JSON))
            .build()
          ).execute();

      JsonObject responseData = gson.fromJson(response.body().string(), JsonElement.class).getAsJsonObject();

      if(! response.isSuccessful()){
        throw new VaultResponseException(responseData.get("message").getAsString(), response.code());
      }

      return responseData.get("data");
    } catch(IOException e) {
      throw new VaultCommunicationException(e);
    }
  }

  private JsonElement get(String url, Headers headers) {
    try {
      Response response = httpClient.newCall(
          new Request.Builder()
            .url(new URL(baseUrl, url))
            .headers(new Headers.Builder().addAll(headers).add("Authorization", apiKey).build())
            .get()
            .build()
          ).execute();

      JsonObject responseData = gson.fromJson(response.body().string(), JsonElement.class).getAsJsonObject();

      if(! response.isSuccessful()){
        throw new VaultResponseException(responseData.get("message").getAsString(), response.code());
      }

      return responseData.get("data");
    } catch(IOException e) {
      throw new VaultCommunicationException(e);
    }
  }

  private JsonElement delete(String url) {
    try {
      Response response = httpClient.newCall(
          new Request.Builder()
            .url(new URL(baseUrl, url))
            .headers(new Headers.Builder().add("Authorization", apiKey).build())
            .delete()
            .build()
          ).execute();

      JsonElement responseData = gson.fromJson(response.body().string(), JsonElement.class);

      if(! response.isSuccessful()){
        String errorMessage = responseData == null ? "No message provided" : responseData.getAsJsonObject().get("message").getAsString();
        throw new VaultResponseException(errorMessage, response.code());
      }

      return responseData.getAsJsonObject().get("data");
    } catch(IOException e) {
      throw new VaultCommunicationException(e);
    }
  }

  private JsonElement get(String url) {
    return get(url, new Headers.Builder().build());
  }

  private JsonElement post(String url, Object body) {
    return post(url, body, new Headers.Builder().build());
  }

  private JsonElement getWithDecryptionKey(String url) {
    if(decryptionKey == null) throw new MissingKeyException("Cannot read data from the vault, as no decryption key was provided");
    return get(url, new Headers.Builder().add("X-Decryption-Key", decryptionKey).build());
  }

  private JsonElement postWithEncryptionKey(String url, Object body) {
    if(encryptionKey == null) throw new MissingKeyException("Cannot write data to the vault, as no encryption key was provided");
    return post(url, body, new Headers.Builder().add("X-Encryption-Key", encryptionKey).build());
  }

  /**
   * Retrieves all attributes for a data subject with the specified ID, as well as data-subject-level metadata.
   * @param subjectId The ID of the data subject to retrieve
   * @return The data subject with the specified ID
   */
  public DataSubject findByDataSubject(String subjectId) {
    List<Attribute> data = gson.fromJson(getWithDecryptionKey(String.format("/datasubjects/%s/attributes", subjectId)), new TypeToken<List<Attribute>>(){}.getType());
    DataSubject subject = gson.fromJson(get(String.format("/datasubjects/%s", subjectId)), DataSubject.class);
    for(Attribute attr : data) subject.addAttributeWithoutPendingChange(attr);
    return subject;
  }

  /**
   * Retrieves all values of the specified attribute that the data subject with the specified ID has.
   * @param subjectId The ID of the data subject to retrieve
   * @param attribute The attribute to retrieve
   * @return A list of matching attributes
   */
  public List<Attribute> getDataSubjectAttribute(String subjectId, String attribute) {
    return gson.fromJson(getWithDecryptionKey(String.format("/datasubjects/%s/attributes/%s", subjectId, attribute)), new TypeToken<List<Attribute>>(){}.getType());
  }

  @Data
  private static class EntityDefinitionDTO {
    private String id;
    private List<String> tags;

    EntityDefinitionDTO(DataSubject entity) {
      this.id = entity.getId();
      this.tags = entity.getTags();
    }
  }

  /**
   * Updates a data subject to match changes that have been made client-side, by deleting or creating attributes in the vault as necessary.
   * @param entity The data subject to save
   */
  public void save(DataSubject entity) {
    
    for(String attribute : entity.getDeletedAttributes()) {
      delete(String.format("/datasubjects/%s/attributes/%s", entity.getId(), attribute));
    }
    entity.getDeletedAttributes().clear();

    post("/datasubjects", new EntityDefinitionDTO(entity));

    if(!entity.getChangedAttributes().isEmpty()) {
      JsonObject storageRequest = new JsonObject();
      JsonArray pointsList = new JsonArray();
      for(Attribute attribute : entity.getChangedAttributes()) {
        pointsList.add(gson.toJsonTree(attribute));
      }
      storageRequest.add("data", pointsList);

      postWithEncryptionKey(String.format("/datasubjects/%s/attributes", entity.getId()), storageRequest);
    }
    entity.getChangedAttributes().clear();

  }

  /**
   * Deletes all attributes of a data subject.
   * @param subjectId The id of the data subject to purge.
   */
  public void purge(String subjectId) {
    delete(String.format("/datasubjects/%s/data", subjectId));
  }

  /**
   * Creates or updates an attribute definition.
   * @param attribute The updated attribute definition
   */
  public void storeAttributeDefinition(AttributeDefinition attribute) {
    post("/attributes", attribute);
  }

  /**
   * Gets an attribute definition with the specified name.
   * @param attributeKey The name of the attribute to retrieve
   * @return The attribute definition with the specified name
   */
  public AttributeDefinition getAttributeDefinition(String attributeKey) {
    return gson.fromJson(get(String.format("/attributes/%s", attributeKey)), AttributeDefinition.class);
  }

  /**
   * Lists all attribute definitions in the vault.
   * @return A list containing all attribute definitions in the vault
   */
  public List<AttributeDefinition> getAttributeDefinitions() {
    return gson.fromJson(get("/attributes/"), new TypeToken<List<AttributeDefinition>>(){}.getType());
  }

  /**
   * Deletes an attribute definition with the specified key.
   * @param attributeKey The key of the attribute to delete
   */
  public void deleteAttributeDefinition(String attributeKey) {
    delete(String.format("/attributes/%s", attributeKey));
  }

  /**
   * Creates or updates a tag.
   * @param tag The tag to store in the vault
   */
  public void storeTag(Tag tag) {
    post("/tags", tag);
  }

  /**
   * Gets a tag with the specified name.
   * @param tag The text of the tag to retrieve
   * @return A tag with the specified text
   */
  public Tag getTag(String tag) {
    return gson.fromJson(get(String.format("/tags/%s", tag)), Tag.class);
  }

  /**
   * Lists all tags in the vault.
   * @return A list of all tags in the vault
   */
  public List<Tag> getTags() {
    return gson.fromJson(get("/tags/"), new TypeToken<List<Tag>>(){}.getType());
  }

  /**
   * Deletes a tag. This will remove the tag from all attributes in the vault.
   * @param tag The text of a tag to remove
   * @return True if a tag with the specified tag was deleted
   */
  public boolean deleteTag(String tag) {
    try {
      delete(String.format("/tags/%s", tag));
      return true;
    } catch(VaultResponseException e) {
      // Throwing and then immediately catching an exception is kind of hacky - might want to make the api return boolean instead
      return false;
    }
  }

  /**
   * Creates or updates a regulation.
   * @param regulation The regulation object to store in the vault
   */
  public void storeRegulation(Regulation regulation) {
    post("/regulations", regulation);
  }

  /**
   * Lists all regulations in the vault.
   * @return A list of all regulations in the vault
   */
  public List<Regulation> getRegulations() {
    return gson.fromJson(get("/regulations/"), new TypeToken<List<Regulation>>(){}.getType());
  }

  /**
   * Gets a regulation with the specified key.
   * @param key The key of the regulation to retrieve.
   * @return A regulation with the specified key
   */
  public Regulation getRegulation(String key) {
    return gson.fromJson(get(String.format("/regulations/%s", key)), new TypeToken<Regulation>(){}.getType());
  }

  /**
   * Deletes a regulation. This will remove the regulation from all attributes in the vault.
   * @param regulation The key of the regulation to delete
   * @return True if a regulation with the specified key was deleted
   */
  public boolean deleteRegulation(String regulation) {
    try {
      delete(String.format("/regulations/%s", regulation));
      return true;
    } catch(VaultResponseException e) {
      // Throwing and then immediately catching an exception is kind of hacky - might want to make the api return boolean instead
      return false;
    }
  }

  /**
   * Creates or updates a rule.
   * @param rule The rule object to store in the vault
   */
  public void storeRule(Rule rule) {
    post("/rules", rule);
  }

  /**
   * Lists all rules in the vault.
   * @return A list of all rules in the vault
   */
  public List<Regulation> getRules() {
    return gson.fromJson(get("/rules/"), new TypeToken<List<Rule>>(){}.getType());
  }

  /**
   * Gets a rule with the specified name.
   * @param name The name of the rule to retrieve.
   * @return A rule with the specified name
   */
  public Rule getRule(String name) {
    return gson.fromJson(get(String.format("/rules/%s", name)), new TypeToken<Rule>(){}.getType());
  }

  /**
   * Deletes a rule.
   * @param rule The name of the rule to delete
   * @return True if a rule with the specified name was deleted
   */
  public boolean deleteRule(String rule) {
    try {
      delete(String.format("/rules/%s", rule));
      return true;
    } catch(VaultResponseException e) {
      // Throwing and then immediately catching an exception is kind of hacky - might want to make the api return boolean instead
      return false;
    }
  }

  /**
   * Searches for attributes in the vault that match specified criteria. Attributes that are indexed can be searched by value.
   * @param searchRequest The search query to execute
   * @param page The page offset of the results to return
   * @param count How many results should be in a page
   * @return One page of attributes that match the query
   */
  public List<Attribute> search(SearchRequest searchRequest, int page, int count) {
    JsonObject paginatedSearchRequest = new JsonObject();
    paginatedSearchRequest.add("query", gson.toJsonTree(searchRequest));
    paginatedSearchRequest.addProperty("page", page);
    paginatedSearchRequest.addProperty("count", count);
    return gson.fromJson(post("/search", paginatedSearchRequest), new TypeToken<List<Attribute>>(){}.getType());
  }

  /**
   * Gets an attribute with the specified datapoint ID.
   * @param dataPointId The datapoint ID of the attribute to retrieve
   * @return A single attribute with the specified datapoint ID
   */
  public Attribute getDataPoint(String dataPointId) {
    return gson.fromJson(getWithDecryptionKey(String.format("/data/%s", dataPointId)), Attribute.class);
  }

  /**
   * Deletes an attribute with the specified datapoint ID.
   * @param dataPointId The datapoint ID of the attribute to delete
   */
  public void deleteDataPoint(String dataPointId) {
    delete(String.format("/data/%s", dataPointId));
  }
}
