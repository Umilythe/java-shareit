package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemRequestDto {
    @NotBlank
    String description;

    @JsonCreator
    public ItemRequestDto(
            @JsonProperty("description") String description) {
        this.description = description;
    }
}
