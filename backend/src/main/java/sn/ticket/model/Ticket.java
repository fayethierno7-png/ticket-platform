package sn.ticket.model;
import jakarta.persistence.*; import java.math.*; import java.time.*;
@Entity @Table(name="tickets",indexes={@Index(name="idx_ticket_code",columnList="code"),@Index(name="idx_ticket_status",columnList="status")}) public class Ticket {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id; @Column(nullable=false,unique=true,length=48) public String code; @Column(nullable=false,unique=true,length=80) public String qrToken; @ManyToOne(optional=false) public User customer; @ManyToOne(optional=false) public TicketType ticketType; @ManyToOne(optional=false) public Payment payment; @Column(nullable=false,precision=12,scale=2) public BigDecimal price; @Enumerated(EnumType.STRING) @Column(nullable=false,length=16) public Status status=Status.VALID; @Column(nullable=false) public LocalDateTime purchasedAt; @Column(nullable=false) public LocalDateTime expiresAt;
}
