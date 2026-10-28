package edu.classroom.board;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Date;
import java.util.HexFormat;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  // JJWT 의존성은 pom.xml에 포함되어 있습니다. app.jwt.* 설정은 application.properties 참조.
  private final SecretKey key;
  private final String issuer;
  private final String audience;
  private final long ttl;
  private final Users users;

  public AuthService(
          @Value("${app.jwt.secret}") String secret,
          @Value("${app.jwt.issuer}") String issuer,
          @Value("${app.jwt.audience}") String audience,
          @Value("${app.jwt.ttl}") long ttl,
          Users users) {
    if (secret == null || secret.isBlank() || secret.startsWith("REPLACE_WITH"))
      throw new IllegalStateException("JWT_SECRET을 새 난수 키로 설정하세요.");
    if (!secret.matches("[0-9a-fA-F]{64}"))
      throw new IllegalStateException("JWT_SECRET은 64자리 16진수여야 합니다.");
    this.key = Keys.hmacShaKeyFor(HexFormat.of().parseHex(secret)); // 64자리 hex → 32바이트 키
    this.issuer = issuer;
    this.audience = audience;
    this.ttl = ttl;
    this.users = users;
  }

  public String issueToken(Users.PublicUser user) {
    // TODO BE-01: 서버 키로 HS256 JWT 발급. sub, exp, iss, aud 필수.
    Instant now = Instant.now();
    return Jwts.builder()
            .subject(String.valueOf(user.id()))
            .issuer(issuer)
            .audience().add(audience).and()
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(ttl)))
            .signWith(key, Jwts.SIG.HS256)
            .compact();
  }

  public long currentUserId(HttpServletRequest request) {
    // TODO BE-02: Bearer 추출 → 서명/알고리즘/만료/클레임 검증 → 실제 사용자 확인.
    // 완성 후 아래 데모 ID 반환을 제거하세요. 토큰 없음/변조/만료는 401입니다.
    String header = request.getHeader("Authorization");
    if (header == null || !header.startsWith("Bearer ") || header.substring(7).isBlank())
      throw new ApiError(401, "TOKEN_REQUIRED", "로그인이 필요합니다.");
    String token = header.substring(7).trim();

    Claims claims;
    try {
      Jws<Claims> jws =
              Jwts.parser()
                      .verifyWith(key)
                      .requireIssuer(issuer)
                      .requireAudience(audience)
                      .build()
                      .parseSignedClaims(token); // 서명 검증 후 만료 검사
      claims = jws.getPayload();
      if (!"HS256".equals(jws.getHeader().getAlgorithm()) || claims.getExpiration() == null)
        throw new ApiError(401, "INVALID_TOKEN", "유효하지 않은 토큰입니다.");
    } catch (ExpiredJwtException e) {
      throw new ApiError(401, "TOKEN_EXPIRED", "로그인 시간이 만료되었습니다. 다시 로그인하세요.");
    } catch (JwtException | IllegalArgumentException e) {
      throw new ApiError(401, "INVALID_TOKEN", "유효하지 않은 토큰입니다.");
    }

    String sub = claims.getSubject();
    if (sub == null || !sub.matches("[1-9][0-9]{0,15}") || users.find(Long.parseLong(sub)) == null)
      throw new ApiError(401, "INVALID_TOKEN", "토큰의 사용자를 확인할 수 없습니다.");
    return Long.parseLong(sub);
  }

  public void assertOwner(long userId, long authorId) {
    // TODO BE-03: 다른 작성자이면 403 FORBIDDEN. 현재는 검사하지 않습니다.
    if (userId != authorId)
      throw new ApiError(403, "FORBIDDEN", "본인이 작성한 글만 수정·삭제할 수 있습니다.");
  }
}
