package sn.ticket.config;
import org.springframework.context.annotation.*; import org.springframework.web.servlet.config.annotation.*;
@Configuration public class WebConfig implements WebMvcConfigurer { public void addCorsMappings(CorsRegistry r){r.addMapping("/api/**").allowedOriginPatterns("http://localhost:*","http://127.0.0.1:*").allowedMethods("GET","POST","PATCH","OPTIONS");} }
