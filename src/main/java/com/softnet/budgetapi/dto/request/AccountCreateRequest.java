package com.softnet.budgetapi.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record AccountCreateRequest(
        @NotBlank(message = "Account name cannot be blank")
        String name
) {}
