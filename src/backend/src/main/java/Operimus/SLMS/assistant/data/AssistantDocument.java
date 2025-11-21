package Operimus.SLMS.assistant.data;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "assistant_documents")
public class AssistantDocument {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    /**
     * Identifier of the vector stored in Pinecone.
     */
    @Column(nullable = false, unique = true)
    private String chunkId;

    @Column(nullable = false)
    private String title;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "assistant_document_roles", joinColumns = @JoinColumn(name = "document_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "assistant_document_frontends", joinColumns = @JoinColumn(name = "document_id"))
    @Column(name = "frontend")
    private Set<String> frontends = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "assistant_document_routes", joinColumns = @JoinColumn(name = "document_id"))
    @Column(name = "route")
    private Set<String> routes = new HashSet<>();

    @Column(nullable = false, length = 10)
    private String locale;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(columnDefinition = "TEXT")
    private String metadataJson;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public AssistantDocument() {
    }

    public AssistantDocument(UUID id,
                             String chunkId,
                             String title,
                             Set<String> roles,
                             Set<String> frontends,
                             Set<String> routes,
                             String locale,
                             String content,
                             String metadataJson,
                             Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.chunkId = Objects.requireNonNull(chunkId);
        this.title = Objects.requireNonNull(title);
        this.roles = roles == null ? new HashSet<>() : new HashSet<>(roles);
        this.frontends = frontends == null ? new HashSet<>() : new HashSet<>(frontends);
        this.routes = routes == null ? new HashSet<>() : new HashSet<>(routes);
        this.locale = Objects.requireNonNull(locale);
        this.content = Objects.requireNonNull(content);
        this.metadataJson = metadataJson;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getChunkId() {
        return chunkId;
    }

    public void setChunkId(String chunkId) {
        this.chunkId = chunkId;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public Set<String> getFrontends() {
        return frontends;
    }

    public void setFrontends(Set<String> frontends) {
        this.frontends = frontends;
    }

    public Set<String> getRoutes() {
        return routes;
    }

    public void setRoutes(Set<String> routes) {
        this.routes = routes;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
