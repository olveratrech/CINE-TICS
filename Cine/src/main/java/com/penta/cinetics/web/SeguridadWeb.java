package com.penta.cinetics.web;

import com.penta.cinetics.identidad.IdentidadesOracle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SeguridadWeb {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }
    @Bean UserDetailsService users(ConexionesPorCadena pools,PasswordEncoder encoder) {
        return name -> {
            IdentidadSesion identity;
            try { identity=IdentidadSesion.parsear(name); }
            catch(IllegalArgumentException invalid) { throw new UsernameNotFoundException("Credenciales inválidas."); }
            try {
                var user=new IdentidadesOracle(()->pools.abrir(identity.cadena()),encoder).buscar(identity.cliente());
                if(user==null) throw new UsernameNotFoundException("Credenciales inválidas.");
                return User.withUsername(name).password(user.hash()).roles(user.rol().name()).disabled(!user.activo()).build();
            } catch(java.sql.SQLException error) { throw new InternalAuthenticationServiceException("Autenticación no disponible."); }
        };
    }
    @Bean SecurityFilterChain security(HttpSecurity http,UserDetailsService users,
            @org.springframework.beans.factory.annotation.Value("${cine.auth.max-attempts:30}") int maximo) throws Exception {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.GET,"/","/web/**","/api/auth/csrf","/api/public/*/shows","/api/public/*/shows/*/seats").permitAll()
            .requestMatchers(HttpMethod.POST,"/api/auth/login","/api/public/*/register").permitAll()
            .requestMatchers("/api/client/**").hasRole("CLIENTE")
            .requestMatchers("/api/staff/**").hasAnyRole("EMPLEADO","ADMIN")
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .requestMatchers("/api/me","/api/auth/logout").authenticated()
            .anyRequest().denyAll());
        http.requestCache(cache -> cache.disable());
        http.formLogin(form -> form.loginProcessingUrl("/api/auth/login")
            .successHandler((req,res,auth)->json(res,200,"{\"authenticated\":true}"))
            .failureHandler((req,res,error)->json(res,401,"{\"code\":\"INVALID_CREDENTIALS\"}")));
        http.logout(logout -> logout.logoutUrl("/api/auth/logout").deleteCookies("CINE_SESSION")
            .logoutSuccessHandler((req,res,auth)->json(res,200,"{\"authenticated\":false}")));
        http.exceptionHandling(errors -> errors
            .authenticationEntryPoint((req,res,error)->json(res,401,"{\"code\":\"AUTHENTICATION_REQUIRED\"}"))
            .accessDeniedHandler((req,res,error)->json(res,403,"{\"code\":\"ACCESS_DENIED\"}")));
        // Keep the default session-backed CSRF repository and masked token handler.
        http.addFilterBefore(new ControlSesionFilter(users,maximo),UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    static void json(jakarta.servlet.http.HttpServletResponse res,int status,String body) throws java.io.IOException {
        res.setStatus(status);res.setContentType("application/json");res.setCharacterEncoding("UTF-8");res.getWriter().write(body);
    }
}
