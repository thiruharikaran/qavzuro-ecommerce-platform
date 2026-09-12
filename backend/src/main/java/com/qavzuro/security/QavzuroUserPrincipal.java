package com.qavzuro.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class QavzuroUserPrincipal extends User {

    private final String userId;
    private final Set<String> permissions;
    private final Set<String> roleCodes;

    public QavzuroUserPrincipal(String userId, String email, Set<String> roleCodes, Set<String> permissions) {
        super(email, "", buildAuthorities(roleCodes, permissions));
        this.userId = userId;
        this.roleCodes = roleCodes;
        this.permissions = permissions;
    }

    private static Collection<? extends GrantedAuthority> buildAuthorities(Set<String> roleCodes, Set<String> permissions) {
        return java.util.stream.Stream.concat(
                roleCodes.stream().map(r -> "ROLE_" + r),
                permissions.stream()
        ).map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }

    public boolean hasPermission(String permissionCode) {
        return permissions.contains(permissionCode);
    }
}
