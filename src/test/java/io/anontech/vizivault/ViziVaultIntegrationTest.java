package io.anontech.vizivault;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.anontech.vizivault.rules.*;
import io.anontech.vizivault.rules.AttributeConstraint.AttributeListOperator;
import io.anontech.vizivault.rules.ValueConstraint.ValuePredicate;

public class ViziVaultIntegrationTest {

  private static String encryptionKey;
  private static String decryptionKey;
  private static String apiKey;

  @BeforeAll
  public static void setup() throws Exception{
    FileInputStream apiKeyFile = new FileInputStream(new File("src" + File.separator + "test" + File.separator + "resources" + File.separator + "apiKey.txt"));
    apiKey = new String( apiKeyFile.readAllBytes() );
    apiKeyFile.close();


    FileInputStream decKeyFile = new FileInputStream(new File("src" + File.separator + "test" + File.separator + "resources" + File.separator + "decryptionKey.txt"));
    decryptionKey = new String(decKeyFile.readAllBytes());
    decKeyFile.close();

    FileInputStream encKeyFile = new FileInputStream(new File("src" + File.separator + "test" + File.separator + "resources" + File.separator + "encryptionKey.txt"));
    encryptionKey = new String(encKeyFile.readAllBytes());
    encKeyFile.close();
  }

  @Test
  public void roundTripData() throws Exception {
    ViziVault vault = new ViziVault(new URL("http://localhost:8083")).withApiKey(apiKey).withDecryptionKey(decryptionKey).withEncryptionKey(encryptionKey);

    // Create two attributes
    AttributeDefinition attributeDef1 = new AttributeDefinition("TestAttribute1");
    AttributeDefinition attributeDef2 = new AttributeDefinition("TestAttribute2");
    attributeDef2.setRepeatable(true);
    vault.storeAttributeDefinition(attributeDef1);
    vault.storeAttributeDefinition(attributeDef2);

    // Add values of both attributes
    DataSubject sentSubject = new DataSubject("exampleUser");
    try {
      Attribute attribute1 = new Attribute(attributeDef1.getName());
      attribute1.setValue("exampleUser's first name");
      sentSubject.addAttribute(attribute1);
      sentSubject.addAttribute(attributeDef2.getName(), "exampleUser's last name");
      sentSubject.addAttribute(attributeDef2.getName(), "exampleUser's other last name");
      vault.save(sentSubject);

      DataSubject receivedSubject = vault.findByDataSubject("exampleUser");
      assertEquals(attribute1.getValueAs(String.class), receivedSubject.getAttribute(attributeDef1.getName()).getValue());
      assertEquals(3, receivedSubject.getAttributes().size());
      assertEquals(2, receivedSubject.getAttributes(attributeDef2.getName()).size());

      // Remove one attribute
      receivedSubject.clearAttribute(attributeDef1.getName());
      vault.save(receivedSubject);
      
      DataSubject receivedSubjectAfterDeletion = vault.findByDataSubject("exampleUser");
      assertEquals(null, receivedSubjectAfterDeletion.getAttribute(attributeDef1.getName()));

    } finally {
      vault.purge(sentSubject.getId());
    }
  }

  @Test
  public void testSearch() throws Exception {
    ViziVault vault = new ViziVault(new URL("http://localhost:8083")).withApiKey("aaa").withDecryptionKey(decryptionKey).withEncryptionKey(encryptionKey);

    AttributeDefinition attributeDef1 = new AttributeDefinition("TestAttribute1");
    attributeDef1.setIndexed(true);
    AttributeDefinition attributeDef2 = new AttributeDefinition("TestAttribute2");
    vault.storeAttributeDefinition(attributeDef1);
    vault.storeAttributeDefinition(attributeDef2);

    DataSubject subject1 = new DataSubject("subject1");
    subject1.addAttribute(attributeDef1.getName(), "common first name");
    vault.save(subject1);

    DataSubject subject2 = new DataSubject("subject2");
    subject2.addAttribute(attributeDef1.getName(), "common first name");
    subject2.addAttribute(attributeDef2.getName(), "subject2's last name");
    vault.save(subject2);

    try {
      SearchRequest searchRequest = new SearchRequest();
      searchRequest.addValueQuery(attributeDef1.getName(), "common first name");
      searchRequest.setAttributes(List.of(attributeDef2.getName()));

      List<Attribute> results = vault.search(searchRequest, 0, 10);
      assertEquals(3, results.size());
      assertTrue(results.stream().anyMatch(result -> result.getAttribute().equals(attributeDef1.getName()) && result.getSubjectId().equals(subject1.getId())));
      assertTrue(results.stream().anyMatch(result -> result.getAttribute().equals(attributeDef1.getName()) && result.getSubjectId().equals(subject2.getId())));
      assertTrue(results.stream().anyMatch(result -> result.getAttribute().equals(attributeDef2.getName()) && result.getSubjectId().equals(subject2.getId())));
    } finally {
      vault.purge(subject1.getId());
      vault.purge(subject2.getId());
    }
  }

  @Test
  public void getAttributeByDatapointId() throws Exception {
    ViziVault vault = new ViziVault(new URL("http://localhost:8083")).withApiKey("aaa").withDecryptionKey(decryptionKey).withEncryptionKey(encryptionKey);

    AttributeDefinition attributeDef = new AttributeDefinition("TestAttribute1");
    vault.storeAttributeDefinition(attributeDef);

    DataSubject sentSubject = new DataSubject("exampleUser");
    sentSubject.addAttribute(attributeDef.getName(), "some data");
    vault.save(sentSubject);

    DataSubject receivedSubject = vault.findByDataSubject(sentSubject.getId());
    assertEquals(receivedSubject.getAttribute(attributeDef.getName()), vault.getDataPoint(receivedSubject.getAttribute(attributeDef.getName()).getDataPointId()));
  }

  @Test
  public void testTags() throws Exception {
    ViziVault vault = new ViziVault(new URL("http://localhost:8083")).withApiKey("aaa").withDecryptionKey(decryptionKey).withEncryptionKey(encryptionKey);

    AttributeDefinition attributeDef1 = new AttributeDefinition("TestAttribute1");
    attributeDef1.setTags(List.of("tag1"));
    vault.storeAttributeDefinition(attributeDef1);

    DataSubject sentSubject = new DataSubject("exampleUser");
    sentSubject.setTags(List.of("tag2"));

    Attribute attribute1 = new Attribute(attributeDef1.getName());
    attribute1.setValue("exampleUser's first name");
    attribute1.setTags(List.of("tag3"));
    sentSubject.addAttribute(attribute1);

    try {
      vault.save(sentSubject);

      Attribute receivedAttribute = vault.findByDataSubject("exampleUser").getAttribute(attributeDef1.getName());
      assertEquals(3, receivedAttribute.getTags().size());
      assertTrue(receivedAttribute.getTags().stream().anyMatch(tag -> tag.equals("tag1")));
      assertTrue(receivedAttribute.getTags().stream().anyMatch(tag -> tag.equals("tag2")));
      assertTrue(receivedAttribute.getTags().stream().anyMatch(tag -> tag.equals("tag3")));

      Tag tag4 = new Tag("tag4");
      vault.storeTag(tag4);

      List<Tag> allTags = vault.getTags();
      assertTrue(allTags.stream().anyMatch(tag -> tag.getName().equals("tag1")));
      assertTrue(allTags.stream().anyMatch(tag -> tag.getName().equals("tag2")));
      assertTrue(allTags.stream().anyMatch(tag -> tag.getName().equals("tag3")));
      assertTrue(allTags.stream().anyMatch(tag -> tag.getName().equals("tag4")));

      vault.deleteTag("tag1");
      vault.deleteTag("tag2");
      vault.deleteTag("tag3");
      vault.deleteTag("tag4");

      assertThrows(VaultResponseException.class, () -> vault.getTag("tag5"));
      assertFalse(vault.deleteTag("tag5"));

      allTags = vault.getTags();
      assertTrue(allTags.stream().noneMatch(tag -> tag.getName().equals("tag1")));
      assertTrue(allTags.stream().noneMatch(tag -> tag.getName().equals("tag2")));
      assertTrue(allTags.stream().noneMatch(tag -> tag.getName().equals("tag3")));
      assertTrue(allTags.stream().noneMatch(tag -> tag.getName().equals("tag4")));

    } finally {
      vault.purge(sentSubject.getId());
    }

  }

  @Test
  public void testRegulations() throws Exception {
    ViziVault vault = new ViziVault(new URL("http://localhost:8083")).withApiKey("aaa").withDecryptionKey(decryptionKey).withEncryptionKey(encryptionKey);

    Regulation regulation = new Regulation();
    regulation.setName("Regulation Name");
    regulation.setKey("RegulationKey");

    AttributeDefinition attributeDef = new AttributeDefinition("TestAttribute1");
    vault.storeAttributeDefinition(attributeDef);

    ConjunctiveConstraint rootRule = new ConjunctiveConstraint();
    rootRule.addRule(new AttributeConstraint(List.of(attributeDef.getName()), AttributeListOperator.ANY));
    rootRule.addRule(new ValueConstraint(attributeDef.getName(), ValuePredicate.EQUALS, "Test Attribute Value"));

    Rule rule = new Rule();
    rule.setConstraint(rootRule);
    rule.setAction(new RegulationAction(regulation.getKey()));

    vault.storeRegulation(regulation);

    Regulation receivedRegulation = vault.getRegulation(regulation.getKey());

    assertEquals(regulation.getName(), receivedRegulation.getName());

    assertTrue(vault.getRegulations().stream().anyMatch(r -> r.getName().equals(regulation.getName())));

    vault.deleteRegulation(regulation.getKey());

    assertTrue(vault.getRegulations().stream().noneMatch(r -> r.getName().equals(regulation.getName())));
  }

  @Test
  public void testAttributeDefinitions() throws Exception {
    ViziVault vault = new ViziVault(new URL("http://localhost:8083")).withApiKey("aaa").withDecryptionKey(decryptionKey).withEncryptionKey(encryptionKey);

    AttributeDefinition attributeDef = new AttributeDefinition("TestAttribute1");
    attributeDef.setIndexed(true);
    vault.storeAttributeDefinition(attributeDef);

    AttributeDefinition received = vault.getAttributeDefinition(attributeDef.getName());
    assertTrue(received.isIndexed());

    assertTrue(vault.getAttributeDefinitions().stream().anyMatch(a -> a.equals(received)));
  }

  @Test
  public void testErrorHandling() throws Exception {
    ViziVault vault = new ViziVault(new URL("http://localhost:8083")).withApiKey("aaa").withDecryptionKey(decryptionKey).withEncryptionKey(encryptionKey);

    AttributeDefinition attributeDef = new AttributeDefinition("InvalidAttribute???");

    assertThrows(VaultResponseException.class, () -> vault.storeAttributeDefinition(attributeDef));
  }
}
