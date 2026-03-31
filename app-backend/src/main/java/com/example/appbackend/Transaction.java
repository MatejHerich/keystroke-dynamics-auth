import com.example.appbackend.Account;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    private String recipientIban;
    private Double amount;
    private String description;
    private LocalDateTime transactionDate = LocalDateTime.now();