package sn.ticket.service;

import sn.ticket.model.*; import sn.ticket.repo.Repos; import org.springframework.stereotype.*; import org.springframework.transaction.annotation.Transactional; import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; import org.springframework.beans.factory.annotation.Value; import java.time.*; import java.time.temporal.*; import java.math.*; import java.security.*; import java.util.*;

@Service public class TicketService {
 private final Repos.Users users; private final Repos.Types types; private final Repos.Tickets tickets; private final Repos.Payments payments; private final Repos.Scans scans; private final ZoneId zone=ZoneId.of("Africa/Dakar"); private final SecureRandom random=new SecureRandom(); @Value("${app.testing-mode:false}") private boolean testingMode;
 public TicketService(Repos.Users u,Repos.Types ty,Repos.Tickets t,Repos.Payments p,Repos.Scans s){users=u;types=ty;tickets=t;payments=p;scans=s;}
 public record Purchase(String fullName,String phone,String email,Integer quantity){} public record TicketData(String code,String qrToken,String customer,BigDecimal price,Status status,LocalDateTime purchasedAt,LocalDateTime expiresAt){}
 public boolean salesOpen(){ if(testingMode)return true; LocalDateTime n=LocalDateTime.now(zone); return n.getDayOfWeek()==DayOfWeek.THURSDAY || (n.getDayOfWeek()==DayOfWeek.FRIDAY && n.toLocalTime().isBefore(LocalTime.of(10,0))); } public boolean testingMode(){return testingMode;}
 public LocalDateTime nextBoundary(){ LocalDateTime n=LocalDateTime.now(zone); if(salesOpen()) return n.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY)).withHour(10).withMinute(0).withSecond(0).withNano(0); return n.with(TemporalAdjusters.next(DayOfWeek.THURSDAY)).toLocalDate().atStartOfDay(); }
 @Transactional public List<TicketData> buy(Purchase in){
  if(!salesOpen()) throw new IllegalStateException("Les ventes sont actuellement fermées. Elles ouvrent chaque jeudi à 00h00 et ferment chaque vendredi à 10h00.");
  if(in.quantity()==null||in.quantity()<1||in.quantity()>10) throw new IllegalArgumentException("La quantité doit être comprise entre 1 et 10.");
  TicketType type=types.findByActiveTrue().stream().findFirst().orElseThrow(()->new NoSuchElementException("Tarif de ticket indisponible."));
  User customer=users.findByEmail(in.email().trim().toLowerCase()).orElseGet(User::new); customer.fullName=in.fullName().trim(); customer.phone=in.phone().trim(); customer.email=in.email().trim().toLowerCase(); if(customer.id==null){customer.passwordHash="CUSTOMER";customer.role="CUSTOMER";} customer=users.save(customer);
  Payment pay=new Payment();pay.customer=customer;pay.amount=type.price.multiply(BigDecimal.valueOf(in.quantity()));pay.reference="PAY-"+token(12);pay=payments.save(pay); LocalDateTime now=LocalDateTime.now(zone); List<TicketData> out=new ArrayList<>();
  for(int i=0;i<in.quantity();i++){Ticket t=new Ticket();t.customer=customer;t.ticketType=type;t.payment=pay;t.price=type.price;t.code="TKT-"+token(14);t.qrToken=token(32);t.purchasedAt=now;t.expiresAt=now.plusMonths(1); tickets.save(t);out.add(data(t));} return out;
 }
 @Transactional public TicketData get(String key){ Ticket t=find(key); refresh(t);return data(t); }
 @Transactional public TicketData verifyAndUse(String key,User controller){ Ticket t=find(key);refresh(t);TicketScan scan=new TicketScan();scan.ticket=t;scan.scannedBy=controller;if(t.status==Status.VALID){t.status=Status.USED;scan.result="VALID";tickets.save(t);}else scan.result=t.status.name(); scans.save(scan);return data(t); }
 @Transactional public void setStatus(String code,Status s){ Ticket t=find(code);t.status=s;tickets.save(t); }
 public List<TicketData> all(){return tickets.findTop100ByOrderByPurchasedAtDesc().stream().map(this::refreshAndData).toList();}
 public Map<String,Object> dashboard(){Map<String,Object> m=new LinkedHashMap<>();m.put("sold",tickets.count());m.put("valid",tickets.countByStatus(Status.VALID));m.put("used",tickets.countByStatus(Status.USED));m.put("expired",tickets.countByStatus(Status.EXPIRED));m.put("revenue",payments.revenue());return m;}
 private TicketData refreshAndData(Ticket t){refresh(t);return data(t);} private void refresh(Ticket t){if(t.status==Status.VALID&&LocalDateTime.now(zone).isAfter(t.expiresAt)){t.status=Status.EXPIRED;tickets.save(t);}} private Ticket find(String k){return tickets.findByCode(k).or(()->tickets.findByQrToken(k)).orElseThrow(()->new NoSuchElementException("Ticket inexistant."));} private TicketData data(Ticket t){return new TicketData(t.code,t.qrToken,t.customer.fullName,t.price,t.status,t.purchasedAt,t.expiresAt);} private String token(int len){byte[] b=new byte[len];random.nextBytes(b);String s=Base64.getUrlEncoder().withoutPadding().encodeToString(b).toUpperCase(Locale.ROOT).replace("_","X").replace("-","Y");return s.substring(0,len);}
}
