package com.staysure.auth.service;

import com.staysure.auth.security.RefreshToken;
import com.staysure.auth.security.RefreshTokenRepository;
import com.staysure.common.exception.ApiException;
import com.staysure.common.util.TokenHashUtil;
import com.staysure.config.JwtProperties;
import com.staysure.user.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenGenerator tokenGenerator;
    private final JwtProperties jwtProperties;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               TokenGenerator tokenGenerator,
                               JwtProperties jwtProperties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenGenerator = tokenGenerator;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public String create(User user, String ipAddress, String userAgent) {
        String rawToken = tokenGenerator.generate();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(TokenHashUtil.sha256(rawToken));
        refreshToken.setExpiresAt(LocalDateTime.now().plusNanos(jwtProperties.refreshExpirationMs() * 1_000_000));
        refreshToken.setCreatedByIp(ipAddress);
        refreshToken.setUserAgent(userAgent);
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Transactional
    public RefreshToken consume(String rawToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(TokenHashUtil.sha256(rawToken))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token", "INVALID_REFRESH_TOKEN"));
        if (refreshToken.isRevoked()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token has been revoked", "REFRESH_TOKEN_REVOKED");
        }
        if (refreshToken.isExpired()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token has expired", "REFRESH_TOKEN_EXPIRED");
        }
        refreshToken.setRevokedAt(LocalDateTime.now());
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void revoke(String rawToken) {
        refreshTokenRepository.findByTokenHash(TokenHashUtil.sha256(rawToken)).ifPresent(token -> {
            if (token.getRevokedAt() == null) {
                token.setRevokedAt(LocalDateTime.now());
                refreshTokenRepository.save(token);
            }
        });
    }
}
