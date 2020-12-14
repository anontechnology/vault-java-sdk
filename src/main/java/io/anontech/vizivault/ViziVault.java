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

import io.anontech.vizivault.tagging.RegulationRule;
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
    gson = new GsonBuilder().registerTypeAdapter(RegulationRule.class, new RegulationRuleDeserializer()).create();
    this.baseUrl = url;
  }

  /**
   * Specifies the API key to use to connect to the ViziVault API server.
   */
  public ViziVault withApiKey(String apiKey) {
    this.apiKey = String.format("Bearer %s", apiKey);
    return this;
  }

  /**
   * Specifies the encryption key that will be used to store data in the vault.
   */
  public ViziVault withEncryptionKey(String encryptionKey) {
    this.encryptionKey = encryptionKey;
    return this;
  }

  /**
   * Specifies the decryptioin key that will be used to store data in the vault.
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
    return get(url, new Headers.Builder().add("X-Decryption-Key", decryptionKey).build());
  }

  private JsonElement postWithEncryptionKey(String url, Object body) {
    return post(url, body, new Headers.Builder().add("X-Encryption-Key", encryptionKey).build());
  }

  /**
   * Retrieves all attributes for an entity with the specified ID, as well as entity-level metadata.
   */
  public Entity findByEntity(String entityId) {
    List<Attribute> data = gson.fromJson(getWithDecryptionKey(String.format("/entities/%s/attributes", entityId)), new TypeToken<List<Attribute>>(){}.getType());
    Entity entity = gson.fromJson(get(String.format("/entities/%s", entityId)), Entity.class);
    for(Attribute attr : data) entity.addAttributeWithoutPendingChange(attr);
    return entity;
  }

  /**
   * Retrieves all attributes for a user with the specified ID, as well as user-level metadata.
   */
  public User findByUser(String userId) {
    List<Attribute> data = gson.fromJson(getWithDecryptionKey(String.format("/users/%s/attributes", userId)), new TypeToken<List<Attribute>>(){}.getType());
    User user = gson.fromJson(get(String.format("/users/%s", userId)), User.class);
    for(Attribute attr : data) user.addAttributeWithoutPendingChange(attr);
    return user;
  }

  @Data
  private static class EntityDefinitionDTO {
    private String id;
    private List<String> tags;

    EntityDefinitionDTO(Entity entity) {
      this.id = entity.getId();
      this.tags = entity.getTags();
    }
  }

  /**
   * Updates a user or entity to match changes that have been made client-side, by deleting or creating attributes in the vault as necessary.
   */
  public void save(Entity entity) {
    
    for(String attribute : entity.getDeletedAttributes()) {
      delete(String.format("/users/%s/attributes/%s", entity.getId(), attribute));
    }
    entity.getDeletedAttributes().clear();

    post(entity instanceof User ? "/users" : "/entities", new EntityDefinitionDTO(entity));

    if(!entity.getChangedAttributes().isEmpty()) {
      JsonObject storageRequest = new JsonObject();
      JsonArray pointsList = new JsonArray();
      for(Attribute attribute : entity.getChangedAttributes()) {
        pointsList.add(gson.toJsonTree(attribute));
      }
      storageRequest.add("data", pointsList);

      postWithEncryptionKey(String.format("/users/%s/attributes", entity.getId()), storageRequest);
    }
    entity.getChangedAttributes().clear();

  }

  /**
   * Deletes all attributes of a user.
   */
  public void purge(String userid) {
    delete(String.format("/users/%s/data", userid));
  }

  /**
   * Creates or updates an attribute definition.
   */
  public void storeAttributeDefinition(AttributeDefinition attribute) {
    post("/attributes", attribute);
  }

  /**
   * Gets an attribute definition with the specified name.
   */
  public AttributeDefinition getAttributeDefinition(String attributeKey) {
    return gson.fromJson(get(String.format("/attributes/%s", attributeKey)), AttributeDefinition.class);
  }

  /**
   * Lists all attribute definitions in the vault.
   */
  public List<AttributeDefinition> getAttributeDefinitions() {
    return gson.fromJson(get("/attributes/"), new TypeToken<List<AttributeDefinition>>(){}.getType());
  }

  /**
   * Creates or updates a tag.
   */
  public void storeTag(Tag tag) {
    post("/tags", tag);
  }

  /**
   * Gets a tag with the specified name.
   */
  public Tag getTag(String tag) {
    return gson.fromJson(get(String.format("/tags/%s", tag)), Tag.class);
  }

  /**
   * Lists all tags in the vault.
   */
  public List<Tag> getTags() {
    return gson.fromJson(get("/tags/"), new TypeToken<List<Tag>>(){}.getType());
  }

  /**
   * Deletes a tag. This will remove the tag from all attributes in the vault.
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
   */
  public void storeRegulation(Regulation regulation) {
    post("/regulations", regulation);
  }

  /**
   * Lists all regulations in the vault.
   */
  public List<Regulation> getRegulations() {
    return gson.fromJson(get("/regulations/"), new TypeToken<List<Regulation>>(){}.getType());
  }

  /**
   * Gets a regulation with the specified key.
   */
  public Regulation getRegulation(String key) {
    return gson.fromJson(get(String.format("/regulations/%s", key)), new TypeToken<Regulation>(){}.getType());
  }

  /**
   * Deletes a regulation. This will remove the regulation from all attributes in the vault.
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
   * Searches for attributes in the vault that match specified criteria. Attributes that are indexed can be searched by value.
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
   */
  public Attribute getDataPoint(String dataPointId) {
    return gson.fromJson(getWithDecryptionKey(String.format("/data/%s", dataPointId)), Attribute.class);
  }
}