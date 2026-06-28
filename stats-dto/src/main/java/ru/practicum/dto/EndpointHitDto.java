package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndpointHitDto {
    private Long id;

    @NotBlank(message = "App name is required")
    private String app;

    @NotBlank(message = "URI is required")
    private String uri;

    @NotBlank(message = "IP address is required")
    @Pattern(regexp = "^([0-9]{1,3}\\.){3}[0-9]{1,3}$",
            message = "IP address must be valid")
    private String ip;

    @NotNull(message = "Timestamp is required")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}