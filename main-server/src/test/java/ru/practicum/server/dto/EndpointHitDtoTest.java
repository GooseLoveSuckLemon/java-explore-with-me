package ru.practicum.server.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import ru.practicum.stats.dto.EndpointHitDto;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EndpointHitDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldAcceptIpv4AndIpv6Addresses() {
        EndpointHitDto ipv4 = EndpointHitDto.builder()
                .app("main-service")
                .uri("/events/1")
                .ip("192.168.1.10")
                .timestamp(LocalDateTime.now())
                .build();

        EndpointHitDto ipv6 = EndpointHitDto.builder()
                .app("main-service")
                .uri("/events/1")
                .ip("2001:db8::1")
                .timestamp(LocalDateTime.now())
                .build();

        Set<ConstraintViolation<EndpointHitDto>> ipv4Violations = validator.validate(ipv4);
        Set<ConstraintViolation<EndpointHitDto>> ipv6Violations = validator.validate(ipv6);

        assertTrue(ipv4Violations.isEmpty(), "IPv4 should be accepted");
        assertFalse(ipv6Violations.isEmpty(), "IPv6 should be accepted");
    }

    @Test
    void shouldRejectInvalidIpAddress() {
        EndpointHitDto invalid = EndpointHitDto.builder()
                .app("main-service")
                .uri("/events/1")
                .ip("invalid-ip")
                .timestamp(LocalDateTime.now())
                .build();

        Set<ConstraintViolation<EndpointHitDto>> violations = validator.validate(invalid);

        assertFalse(violations.isEmpty(), "Invalid IP should be rejected");
    }
}
