package sn.ticket.api;
import sn.ticket.service.*; import sn.ticket.model.*; import org.springframework.web.bind.annotation.*; import jakarta.validation.constraints.*; import jakarta.validation.Valid; import java.util.*;
@RestController @RequestMapping("/api") public class ApiController {
 private final TicketService service;private final AuthService auth;public ApiController(TicketService s,AuthService a){service=s;auth=a;}
 record Buy(@NotBlank String fullName,@NotBlank String phone,@Email @NotBlank String email,@NotNull Integer quantity){} record Login(@Email @NotBlank String email,@NotBlank String password){} record Verify(@NotBlank String code){} record StatusUpdate(@NotNull Status status){}
 @GetMapping("/sales-status") Map<String,Object> sales(){return Map.of("open",service.salesOpen(),"testingMode",service.testingMode(),"nextBoundary",service.nextBoundary(),"timezone","Africa/Dakar");}
 @PostMapping("/tickets/acheter") List<TicketService.TicketData> buy(@Valid @RequestBody Buy b){return service.buy(new TicketService.Purchase(b.fullName,b.phone,b.email,b.quantity));}
 @GetMapping("/tickets/{code}") TicketService.TicketData ticket(@PathVariable String code){return service.get(code);}
 @PostMapping("/auth/login") Map<String,String> login(@Valid @RequestBody Login l){return Map.of("token",auth.login(l.email,l.password));}
 @PostMapping("/tickets/verifier") TicketService.TicketData verify(@RequestHeader(value="Authorization",required=false) String h,@Valid @RequestBody Verify v){return service.verifyAndUse(v.code,auth.require(h));}
 @GetMapping("/admin/dashboard") Map<String,Object> dashboard(@RequestHeader(value="Authorization",required=false)String h){auth.require(h);return service.dashboard();}
 @GetMapping("/admin/tickets") List<TicketService.TicketData> tickets(@RequestHeader(value="Authorization",required=false)String h){auth.require(h);return service.all();}
 @PatchMapping("/admin/tickets/{code}") void status(@RequestHeader(value="Authorization",required=false)String h,@PathVariable String code,@RequestBody StatusUpdate s){auth.require(h);service.setStatus(code,s.status);}
}
