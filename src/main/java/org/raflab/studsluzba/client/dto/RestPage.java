package org.raflab.studsluzba.client.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

// Ovo je klasa koja "glumi" Page objekat sa servera
public class RestPage<T> {
    private List<T> content;
    private int number;      // Trenutna stranica (0, 1, 2...)
    private int totalPages;  // Ukupno stranica
    private long totalElements; // Ukupno elemenata

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public RestPage(@JsonProperty("content") List<T> content,
                    @JsonProperty("number") int number,
                    @JsonProperty("totalPages") int totalPages,
                    @JsonProperty("totalElements") long totalElements,
                    @JsonProperty("pageable") JsonNode pageable,
                    @JsonProperty("last") boolean last,
                    @JsonProperty("first") boolean first,
                    @JsonProperty("sort") JsonNode sort,
                    @JsonProperty("numberOfElements") int numberOfElements,
                    @JsonProperty("size") int size,
                    @JsonProperty("empty") boolean empty) {
        this.content = content;
        this.number = number;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }

    public List<T> getContent() { return content; }
    public int getNumber() { return number; }
    public int getTotalPages() { return totalPages; }
    public long getTotalElements() { return totalElements; }
}