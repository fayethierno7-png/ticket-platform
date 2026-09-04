package sn.ticket.config;
import sn.ticket.repo.Repos; import sn.ticket.model.*; import org.springframework.boot.*; import org.springframework.context.annotation.*; import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
@Configuration public class Bootstrap { @Bean CommandLineRunner initialAdmin(Repos.Users users){return args->{users.findByEmail("admin@tickets.local").ifPresent(u->{if(!new BCryptPasswordEncoder().matches("Admin2026!",u.passwordHash)){u.passwordHash=new BCryptPasswordEncoder().encode("Admin2026!");users.save(u);}});};} }
