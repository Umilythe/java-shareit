package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentDtoRequest {

    private String text;

    @JsonCreator
    public CommentDtoRequest(
            @JsonProperty("text") String text) {
        this.text = text;
    }
}
