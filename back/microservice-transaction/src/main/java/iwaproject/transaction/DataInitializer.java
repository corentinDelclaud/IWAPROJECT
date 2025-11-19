package iwaproject.transaction;

import iwaproject.transaction.repository.ConversationRepository;
import iwaproject.transaction.repository.TransactionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private final ConversationRepository conversationRepository;
    private final TransactionRepository transactionRepository;
    
    public DataInitializer(ConversationRepository conversationRepository,
                          TransactionRepository transactionRepository) {
        this.conversationRepository = conversationRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void run(String... args) {
        // Clear existing data
        transactionRepository.deleteAll();
        conversationRepository.deleteAll();
        // No more fake data initialization - using real services now
    }
}