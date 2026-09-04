package sn.ticket.model;
import jakarta.persistence.*; import java.math.*; import java.time.*;
@Entity @Table(name="payments") public class Payment { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id; @ManyToOne(optional=false) public User customer; @Column(nullable=false,precision=12,scale=2) public BigDecimal amount; @Column(nullable=false,length=30) public String status="PAID"; @Column(nullable=false,unique=true) public String reference; public LocalDateTime paidAt=LocalDateTime.now(ZoneId.of("Africa/Dakar")); }
