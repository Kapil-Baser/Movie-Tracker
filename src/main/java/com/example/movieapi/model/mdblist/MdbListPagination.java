package com.example.movieapi.model.mdblist;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MdbListPagination {
    private int limit;
    private int offset;
    private int total;
    @JsonProperty("has_more")
    private boolean hasMore;
    @JsonProperty("next_cursor")
    private String nextCursor;
}
