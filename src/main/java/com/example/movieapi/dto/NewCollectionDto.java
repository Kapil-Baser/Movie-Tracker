package com.example.movieapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewCollectionDto {

    @NotBlank(message = "Name cannot be blank or empty.")
    @Size(min = 6, max = 255, message = "Collection name must be between 6 and 255 characters.")
    private String name;
}
