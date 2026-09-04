package sn.ticket.model;
import jakarta.persistence.*; import java.time.*;
@Entity @Table(name="ticket_scans") public class TicketScan { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id; @ManyToOne(optional=false) public Ticket ticket; @ManyToOne public User scannedBy; @Column(nullable=false) public LocalDateTime scannedAt=LocalDateTime.now(ZoneId.of("Africa/Dakar")); @Column(nullable=false,length=30) public String result; }
