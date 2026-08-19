package com.example.movieapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CollectionRenameDto {

    @NotBlank(message = "Name cannot be blank or empty.")
    @Size(min = 1, max = 255, message = "Collection name must be between 1 and 255 characters.")
    private String name;
    private Long id;
}
