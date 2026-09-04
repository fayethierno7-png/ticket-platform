package sn.ticket.model;
import jakarta.persistence.*; import java.math.BigDecimal;
@Entity @Table(name="ticket_types") public class TicketType { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id; @Column(nullable=false,unique=true,length=80) public String name; @Column(nullable=false,precision=12,scale=2) public BigDecimal price; public String description; public boolean active=true; }
