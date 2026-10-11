package com.makemytrip.makemytrip.models;
import org.springframework.data.annotation.Id; import org.springframework.data.mongodb.core.index.Indexed; import org.springframework.data.mongodb.core.mapping.Document; import java.time.Instant; import java.util.Map;
@Document("travel_features") public class FeatureRecord { @Id public String id; @Indexed public String kind; @Indexed public String parentId; @Indexed public String ownerId; public Map<String,Object> data; public Instant createdAt=Instant.now(); public Instant updatedAt=Instant.now(); }
