package sn.ticket.repo;
import sn.ticket.model.*; import org.springframework.data.jpa.repository.*; import java.util.*; import java.math.*;
public class Repos {
 public interface Users extends JpaRepository<User,Long>{ Optional<User> findByEmail(String email); }
 public interface Types extends JpaRepository<TicketType,Long>{ List<TicketType> findByActiveTrue(); }
 public interface Payments extends JpaRepository<Payment,Long>{ @Query("select coalesce(sum(p.amount),0) from Payment p where p.status='PAID'") BigDecimal revenue(); }
 public interface Tickets extends JpaRepository<Ticket,Long>{ Optional<Ticket> findByCode(String code); Optional<Ticket> findByQrToken(String qrToken); long countByStatus(Status status); List<Ticket> findTop100ByOrderByPurchasedAtDesc(); }
 public interface Scans extends JpaRepository<TicketScan,Long>{ }
}
