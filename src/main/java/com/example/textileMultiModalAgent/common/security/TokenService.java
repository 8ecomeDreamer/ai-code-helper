package com.example.textileMultiModalAgent.common.security;

import com.example.textileMultiModalAgent.common.core.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * JWT 令牌服务：签发与解析
 */
@Component
public class TokenService {

    private static final String CLAIM_USER_ID = "user_id";

    /** EventSource（GET SSE）无法携带自定义请求头时的令牌兜底参数名 */
    private static final String TOKEN_QUERY_PARAM = "token";

    @Value("${token.header:Authorization}")
    private String header;

    @Value("${token.prefix:Bearer }")
    private String prefix;

    @Value("${token.secret}")
    private String secret;

    @Value("${token.expire-minutes:720}")
    private long expireMinutes;

    /**
     * 为登录用户签发令牌
     */
    public String createToken(LoginUser loginUser) {
        Date now = new Date();
        loginUser.setLoginTime(now.getTime());
        loginUser.setExpireTime(now.getTime() + expireMinutes * 60 * 1000);
        return Jwts.builder()
                .subject(loginUser.getUserName())
                .claim(CLAIM_USER_ID, loginUser.getUserId())
                .issuedAt(now)
                .expiration(new Date(loginUser.getExpireTime()))
                .signWith(secretKey())
                .compact();
    }

    /**
     * 从请求中解析令牌携带的用户ID，令牌缺失或非法时返回 null
     */
    public Long getUserIdFromRequest(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token == null) {
            return null;
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Number userId = claims.get(CLAIM_USER_ID, Number.class);
            return userId == null ? null : userId.longValue();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 语义化别名：令牌可来自请求头或 token 查询参数（见 getTokenFromRequest），解析用户ID。
     * 供 /ai/** 可选鉴权拦截器调用，与 getUserIdFromRequest 行为一致。
     */
    public Long getUserIdFromRequestOrParam(HttpServletRequest request) {
        return getUserIdFromRequest(request);
    }

    /**
     * 提取令牌（去掉 Bearer 前缀）：优先请求头，缺失时回退查询参数 token。
     * 浏览器 EventSource 发起的 GET SSE 无法设置 Authorization 头，前端改用 ?token= 传递。
     */
    public String getTokenFromRequest(HttpServletRequest request) {
        String value = request.getHeader(header);
        if (!StringUtils.hasText(value)) {
            value = request.getParameter(TOKEN_QUERY_PARAM);
        }
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmedPrefix = prefix.trim();
        if (value.startsWith(trimmedPrefix)) {
            value = value.substring(trimmedPrefix.length());
        }
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private SecretKey secretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
