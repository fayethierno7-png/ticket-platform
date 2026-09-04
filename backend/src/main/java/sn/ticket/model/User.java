package sn.ticket.model;
import jakarta.persistence.*; import java.time.*;
@Entity @Table(name="users") public class User {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id; @Column(nullable=false,length=120) public String fullName; @Column(nullable=false,unique=true,length=190) public String email; @Column(name="phone",length=30) public String phone; @Column(nullable=false) public String passwordHash; @Column(nullable=false) public String role="ADMIN"; public LocalDateTime createdAt=LocalDateTime.now(ZoneId.of("Africa/Dakar"));
}
