package com.penta.cinetics.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.web.filter.OncePerRequestFilter;

/** Prevents stale roles/disabled accounts from retaining access until session expiration. */
final class ControlSesionFilter extends OncePerRequestFilter {
    private final UserDetailsService users;
    private final LimiteAcceso limite;
    ControlSesionFilter(UserDetailsService users,int maximo) { this.users=users;this.limite=new LimiteAcceso(java.time.Clock.systemUTC(),maximo); }
    @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain) throws ServletException,IOException {
        if(req.getMethod().equals("POST") && (req.getRequestURI().equals("/api/auth/login") || req.getRequestURI().endsWith("/register"))) {
            if(!limite.permitir(req.getRemoteAddr())) {
                res.setHeader("Retry-After","60");SeguridadWeb.json(res,429,"{\"code\":\"TOO_MANY_ATTEMPTS\"}");return;
            }
        }
        if(req.getRequestURI().equals("/api/auth/login") && req.getMethod().equals("POST")) {
            String password=req.getParameter("password");
            if(password==null || password.getBytes(StandardCharsets.UTF_8).length>72) {
                SeguridadWeb.json(res,401,"{\"code\":\"INVALID_CREDENTIALS\"}"); return;
            }
        }
        var auth=SecurityContextHolder.getContext().getAuthentication();
        if(auth!=null && auth.isAuthenticated() && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            try {
                var current=users.loadUserByUsername(auth.getName());
                if(!current.isEnabled() || !java.util.Set.copyOf(current.getAuthorities()).equals(auth.getAuthorities().stream().filter(a->a.getAuthority().startsWith("ROLE_")).collect(java.util.stream.Collectors.toSet()))) {
                    invalidar(req,res);return;
                }
            } catch(UsernameNotFoundException invalid) { invalidar(req,res);return; }
              catch(InternalAuthenticationServiceException unavailable) { SeguridadWeb.json(res,503,"{\"code\":\"SERVICE_UNAVAILABLE\"}");return; }
        }
        chain.doFilter(req,res);
    }
    private void invalidar(HttpServletRequest req,HttpServletResponse res) throws IOException {
        var session=req.getSession(false);if(session!=null)session.invalidate();
        SecurityContextHolder.clearContext();SeguridadWeb.json(res,401,"{\"code\":\"SESSION_INVALID\"}");
    }
}
