package com.exam.security;

import com.exam.entity.SysUser;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
public class UserPrincipal implements UserDetails {
    private Long userId;
    private String username;
    private String password;
    private String realName;
    private String roleCode;
    private Integer status;
    private Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(SysUser user, String roleCode) {
        UserPrincipal principal = new UserPrincipal();
        principal.userId = user.getId();
        principal.username = user.getUsername();
        principal.password = user.getPassword();
        principal.realName = user.getRealName();
        principal.roleCode = roleCode;
        principal.status = user.getStatus();
        principal.authorities = List.of(new SimpleGrantedAuthority(roleCode));
        return principal;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return status == 1; }
}
