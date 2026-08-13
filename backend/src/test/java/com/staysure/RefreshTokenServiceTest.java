package com.staysure;

import com.staysure.auth.security.RefreshToken;
import com.staysure.auth.security.RefreshTokenRepository;
import com.staysure.auth.service.RefreshTokenService;
import com.staysure.auth.service.TokenGenerator;
import com.staysure.common.exception.ApiException;
import com.staysure.common.util.TokenHashUtil;
import com.staysure.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private TokenGenerator tokenGenerator;

    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(
                refreshTokenRepository,
                tokenGenerator,
                new JwtProperties("01234567890123456789012345678901", 900000, 604800000)
        );
    }

    @Test
    void invalidRefreshTokenIsRejected() {
        when(refreshTokenRepository.findByTokenHash(TokenHashUtil.sha256("raw"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.consume("raw"))
                .isInstanceOf(ApiException.class)
                .satisfies(error -> assertThat(((ApiException) error).getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED))
                .hasMessage("Invalid refresh token");
    }

    @Test
    void expiredRefreshTokenIsRejected() {
        RefreshToken token = new RefreshToken();
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(refreshTokenRepository.findByTokenHash(TokenHashUtil.sha256("raw"))).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> refreshTokenService.consume("raw"))
                .isInstanceOf(ApiException.class)
                .hasMessage("Refresh token has expired");
    }

    @Test
    void revokedRefreshTokenIsRejected() {
        RefreshToken token = new RefreshToken();
        token.setExpiresAt(LocalDateTime.now().plusMinutes(1));
        token.setRevokedAt(LocalDateTime.now());
        when(refreshTokenRepository.findByTokenHash(TokenHashUtil.sha256("raw"))).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> refreshTokenService.consume("raw"))
                .isInstanceOf(ApiException.class)
                .hasMessage("Refresh token has been revoked");
    }
}
