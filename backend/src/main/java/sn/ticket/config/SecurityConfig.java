package sn.ticket.config;
import org.springframework.context.annotation.*; import org.springframework.security.config.annotation.web.builders.*; import org.springframework.security.web.*; import org.springframework.security.config.*;
@Configuration public class SecurityConfig { @Bean SecurityFilterChain filterChain(HttpSecurity h)throws Exception{return h.csrf(c->c.disable()).httpBasic(b->b.disable()).formLogin(f->f.disable()).authorizeHttpRequests(a->a.anyRequest().permitAll()).build();} }
