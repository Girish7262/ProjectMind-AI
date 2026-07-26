# JWT Filter Integration Specification

## Document Metadata

| Metadata Field | Value |
|---|---|
| Document Type | Security & Compliance / JWT Filter Integration |
| Version | 1.0.0 |
| Status | Published |
| Owner | Principal Security Architect |
| Reviewer | CISO / Security Architect |
| Last Updated | 2026-07-23 |

---

# Executive Summary

This JWT Filter Integration Specification defines how JSON Web Tokens are parsed, validated, and mapped to authenticated security sessions within the Spring Security request lifecycle. By implementing a customized `OncePerRequestFilter` and positioning it before `UsernamePasswordAuthenticationFilter`, the platform blocks unauthorized requests before they reach resource endpoints.

---

# Request Verification Flow

The diagram below models request intercepting, Bearer token validations, claims extracting, user details matching, and security context mapping:

```mermaid
sequenceDiagram
    actor Client
    participant Filter as JwtAuthenticationFilter
    participant Validator as JwtValidator
    participant Provider as JwtTokenProvider
    participant UserDetailsService as UserDetailsService
    participant Context as SecurityContextHolder

    Client->>Filter: Request protected resource (Header: Authorization)
    Filter->>Filter: Check header format (Bearer Prefix)
    alt Missing / Invalid Prefix
        Filter-->>Client: Continue Chain / Return 401
    else Valid Header
        Filter->>Validator: isValid(Token)
        alt Token Expired / Malformed / Signature Invalid
            Validator-->>Filter: Return False
            Filter-->>Client: Invoke EntryPoint (401 JSON Response)
        else Token Valid
            Validator-->>Filter: Return True
            Filter->>Provider: extractUsername(Token)
            Provider-->>Filter: Return Username (Email)
            Filter->>UserDetailsService: loadUserByUsername(Email)
            UserDetailsService-->>Filter: Return AuthUserDetails
            Filter->>Context: setAuthentication(UsernamePasswordAuthenticationToken)
            Filter->>Filter: Continue filterChain.doFilter()
        end
    end
```

---

# Filter Execution Lifecycle

The lifecycle diagram below maps filter lifecycle states and bypass paths:

```mermaid
graph TD
    Request["Request Ingress"] --> PathCheck{"Is Public Endpoint?"}
    PathCheck -- "Yes" --> Bypass["Skip JWT Check (shouldNotFilter = true)"]
    Bypass --> Continue["Continue Filter Chain"]
    PathCheck -- "No" --> HeaderCheck{"Authorization Header Present?"}
    HeaderCheck -- "No" --> Deny["Anonymous User Context / Block"]
    HeaderCheck -- "Yes" --> PrefixCheck{"Valid 'Bearer ' Prefix?"}
    PrefixCheck -- "No" --> Deny
    PrefixCheck -- "Yes" --> VerifyToken{"JwtValidator.isValid(Token)?"}
    VerifyToken -- "No" --> Exception["Throw JWT Exception / 401 Response"]
    VerifyToken -- "Yes" --> LoadUser["UserDetailsService.loadUserByUsername()"]
    LoadUser --> PopulateContext["SecurityContextHolder.setAuthentication()"]
    PopulateContext --> Continue
```

---

# Security Best Practices

The JWT filter enforces the following integration controls:

* **Positioning before UsernamePasswordAuthenticationFilter:** JWT authentication replaces standard username/password form logins. Positioning the filter before the default filter guarantees token validations occur first.
* **Skipping Public Endpoints:** Override `shouldNotFilter` using path matchers (e.g. `/api/v1/auth/login`) to bypass the token check, reducing latency for public routes.
* **Stateless Token Verification:** The filter verifies signatures locally without database hits. To validate user statuses (active, locked), it queries `UserDetailsService` (which can be optimized using Redis cache lookups).

---

# Conclusion

The JWT Filter Integration Specification defines how JSON Web Tokens are parsed, validated, and mapped to authenticated security sessions within the Spring Security request lifecycle. Placing the filter before `UsernamePasswordAuthenticationFilter` secures the ProjectMind AI Auth Service against unauthorized access.

---

# Revision History

| Version | Date | Author / Role | Summary of Changes |
|---|---|---|---|
| 0.1.0 | 2026-07-23 | Security Architect | Initial creation of the JWT Filter Integration Spec. |
