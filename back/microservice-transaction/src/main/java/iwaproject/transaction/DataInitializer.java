package iwaproject.transaction;

import iwaproject.transaction.enums.TransitionState;
import iwaproject.transaction.model.Conversation;
import iwaproject.transaction.model.Product;
import iwaproject.transaction.model.Transaction;
import iwaproject.transaction.repository.ConversationRepository;
import iwaproject.transaction.repository.ProductRepository;
import iwaproject.transaction.repository.TransactionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private final ProductRepository productRepository;
    private final ConversationRepository conversationRepository;
    private final TransactionRepository transactionRepository;
    
    public DataInitializer(ProductRepository productRepository,
                          ConversationRepository conversationRepository,
                          TransactionRepository transactionRepository) {
        this.productRepository = productRepository;
        this.conversationRepository = conversationRepository;
        this.transactionRepository = transactionRepository;
    }

    public void clearData() {
        transactionRepository.deleteAll();
        conversationRepository.deleteAll();
        productRepository.deleteAll();
    }

    public void fillProducts(){
        Product p1 = new Product("League of Legends", "Coaching", "1h coaching fer->or", new BigDecimal("25.00"), false, 101);
        Product p2 = new Product("Valorant", "Boost", "Boost de 2 rangs", new BigDecimal("40.00"), true, 102);
        Product p3 = new Product("CS2", "Skin", "AWP Dragon Lore FN", new BigDecimal("5000.00"), true, 103);
        Product p4 = new Product("Dota 2", "Coaching", "Coaching role support", new BigDecimal("30.00"), false, 101);
        Product p5 = new Product("Overwatch", "Boost", "Bronze vers Platine", new BigDecimal("60.00"), false, 102);
        productRepository.saveAll(java.util.List.of(p1, p2, p3, p4, p5));
    }

    public void fillConversations(){
        Conversation c1 = new Conversation(1, 101);
        Conversation c2 = new Conversation(2, 102);
        Conversation c3 = new Conversation(3, 103);
        Conversation c4 = new Conversation(4, 101);
        Conversation c5 = new Conversation(5, 102);
        conversationRepository.saveAll(java.util.List.of(c1, c2, c3, c4, c5));
    }
    
    @Override
    public void run(String... args) {
        // Clear existing data
        clearData();
        // Products
        fillProducts();
        // Conversations
        fillConversations();
        // Transactions
        // none for now
    }
}